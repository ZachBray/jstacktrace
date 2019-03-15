package jstacktrace

import net.bytebuddy.asm.Advice
import java.io.BufferedWriter

class MutableInt(var value: Int = 0)

object Stats {
    val callStackDepth = ThreadLocal<MutableInt>()
}

object Arguments {
    var writerFactory: (Thread) -> BufferedWriter? = { null }
}

object MethodInterceptor {
    @Advice.OnMethodEnter
    @JvmStatic
    fun onEnter(@Advice.Origin("#t::#m") method: String,
                @Advice.AllArguments arguments: Array<Any>) {

        val writer = Arguments.writerFactory(Thread.currentThread()) ?: return

        val depth = Stats.callStackDepth

        if (depth.get() == null) {
            depth.set(MutableInt())
        }

        for (i in 1..depth.get().value) {
            writer.write(" |")
            if (i == depth.get().value) {
                writer.write("-> ")
            } else {
                writer.write("   ")
            }
        }
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
    }

    @Advice.OnMethodExit
    @JvmStatic
    fun onExit() {
        val depth = Stats.callStackDepth
        --depth.get().value

        val writer = Arguments.writerFactory(Thread.currentThread()) ?: return
        writer.flush()
    }
}