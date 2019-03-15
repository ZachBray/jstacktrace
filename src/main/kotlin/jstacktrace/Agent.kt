package jstacktrace

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.asm.Advice
import java.io.BufferedWriter
import java.io.File
import java.lang.instrument.Instrumentation
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.concurrent.getOrSet

fun premain(argument: String, instrumentation: Instrumentation) = main(argument, instrumentation)

fun agentmain(argument: String, instrumentation: Instrumentation) = main(argument, instrumentation)

private fun main(argument: String, instrumentation: Instrumentation) {
    val filterFile = File(argument.substringBefore('|'))
    if (!filterFile.exists()) {
        log("Filter file does not exist: $argument")
        return
    }
    val filterSpec = filterFile.readText()
    val outputDir = File(argument.substringAfter('|'))
    if (outputDir.exists() && outputDir.isFile) {
        log("Output directory must be a directory not a file: $outputDir")
        return
    } else if (!outputDir.exists()) {
        Files.createDirectories(outputDir.toPath())
    }

    attach(filterSpec, outputDir.toPath(), instrumentation)
}

fun attach(filterSpec: String, outputDir: Path, instrumentation: Instrumentation) {
    val methodsByType = getSelectedMethods(filterSpec)
    val writerCache = ThreadLocal<BufferedWriter>()
    Arguments.writerFactory = { thread ->
        writerCache.getOrSet {
            val path = outputDir.resolve("trace-${thread.id}.log")
            if (Files.exists(path)) {
                Files.delete(path)
            }
            val writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW)
            writer.write("Thread name: ${thread.name}")
            writer.newLine()
            writer
        }
    }

    println("Attaching jstacktrace...")
    val transformer = AgentBuilder.Default()
            .disableClassFormatChanges()
            .type { target -> methodsByType.containsKey(target.typeName) }
            .transform { builder, typeDescription, _, _ ->
                println("Transforming: ${typeDescription.name}")
                val methodsForType = methodsByType[typeDescription.typeName] ?: emptySet()
                val visitor = Advice.to(MethodInterceptor.javaClass)
                        .on { target -> target.isMethod && methodsForType.contains(target.name) }
                builder.visit(visitor)
            }
            .makeRaw()

    instrumentation.addTransformer(transformer, true)
    instrumentation.allLoadedClasses
            .filter { methodsByType.containsKey(it.typeName) }
            .filter { instrumentation.isModifiableClass(it) }
            .forEach {
                println("Registering: ${it.name}")
                instrumentation.retransformClasses(it)
            }

    println("jstacktrace attached.")
}

private fun getSelectedMethods(filterSpec: String): MutableMap<String, Set<String>> {
    val methodsByType = mutableMapOf<String, Set<String>>()
    filterSpec.lines().forEach { line ->
        if (line.isBlank()) {
            return@forEach
        }

        val typeName = line.substringBeforeLast("::")
        val methodName = line.substringAfter("::")
        val isFormatCorrect = typeName != line && methodName != line
        if (!isFormatCorrect) {
            log("[WARN] Incorrect filter format: $line")
            log("       Expected 'fully.qualified.type::method'")
            return@forEach
        }

        methodsByType.compute(typeName) { _, methods ->
            methods?.plus(methodName) ?: setOf(methodName)
        }
    }
    return methodsByType
}

private fun log(statement: String) {
    println(statement)
}
