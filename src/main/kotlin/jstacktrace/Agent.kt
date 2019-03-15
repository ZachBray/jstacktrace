package jstacktrace

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.asm.Advice
import java.io.File
import java.lang.instrument.Instrumentation

fun premain(argument: String, instrumentation: Instrumentation) = main(argument, instrumentation)

fun agentmain(argument: String, instrumentation: Instrumentation) = main(argument, instrumentation)

private fun main(argument: String, instrumentation: Instrumentation) {
    val filterFile = File(argument)
    if (!filterFile.exists()) {
        log("Filter file '$argument' does not exist.")
        return
    }
    val filterSpec = filterFile.readText()

    attach(filterSpec, instrumentation)
}

fun attach(filterSpec: String, instrumentation: Instrumentation) {
    val methodsByType = getSelectedMethods(filterSpec)

    val transformer = AgentBuilder.Default()
     //       .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
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
    /*
    instrumentation.allLoadedClasses
            .filter { methodsByType.containsKey(it.typeName) }
            .filter { instrumentation.isModifiableClass(it) }
            .forEach {
                println("Registering: ${it.name}")
                instrumentation.retransformClasses(it)
            }
            */
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
