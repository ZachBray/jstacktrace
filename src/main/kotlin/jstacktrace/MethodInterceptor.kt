package jstacktrace

import net.bytebuddy.asm.Advice
import java.io.BufferedWriter

class MutableInt(var value: Int = 0)

object Stats {
    val callStackDepth = ThreadLocal<MutableInt>()
}

object Arguments {
    var writerFactory: (Thread) -> BufferedWriter? = { null }
    var thresholdMillis = 10
    var time: () -> Long = { System.currentTimeMillis() }
}

object Utils {
    fun writeIndent(depth: ThreadLocal<MutableInt>, writer: BufferedWriter, symbol: Char) {
        writer.write("[ms: ")
        writer.write(Arguments.time().toString())
        writer.write("] ")
        for (i in 1..depth.get().value) {
            writer.write(" |")
            if (i == depth.get().value) {
                writer.write("-$symbol ")
            } else {
                writer.write("   ")
            }
        }
    }
}

object MethodInterceptor {
    @Advice.OnMethodEnter
    @JvmStatic
    fun onEnter(@Advice.Origin("#t::#m") method: String,
                @Advice.AllArguments arguments: Array<Any>): Long {

        val writer = Arguments.writerFactory(Thread.currentThread()) ?: return 0

        val depth = Stats.callStackDepth

        if (depth.get() == null) {
            depth.set(MutableInt())
        }

        Utils.writeIndent(depth, writer, '>')
        writer.write(method)
        writer.write("(")
        arguments.forEachIndexed { index, argument ->
            if (index != 0) {
                writer.write(", ")
            }
            writer.write(argument.toString())
        }
        writer.write(")")
        writer.newLine()

        ++depth.get().value

        return Arguments.time()
    }

    @Advice.OnMethodExit
    @JvmStatic
    fun onExit(@Advice.Enter startTime: Long) {
        val writer = Arguments.writerFactory(Thread.currentThread()) ?: return
        val endTime = Arguments.time()

        val depth = Stats.callStackDepth

        val durationInMillis = endTime - startTime
        if (durationInMillis > Arguments.thresholdMillis) {
            Utils.writeIndent(depth, writer, 'o')
            writer.write("took: $durationInMillis ms")
            writer.newLine()
        }
        writer.flush()

        --depth.get().value
    }
}