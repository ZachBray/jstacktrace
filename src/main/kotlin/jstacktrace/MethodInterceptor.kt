package jstacktrace

import net.bytebuddy.asm.Advice

class MutableInt(var value: Int = 0)

object Stats {
    val callStackDepth = ThreadLocal<MutableInt>()
}

object MethodInterceptor {
    @Advice.OnMethodEnter
    @JvmStatic
    fun onEnter(@Advice.Origin("#t::#m") method: String,
                @Advice.AllArguments arguments: Array<Any>) {

        val depth = Stats.callStackDepth

        if (depth.get() == null) {
            depth.set(MutableInt())
        }

        for (i in 1..depth.get().value) {
            print(" |")
            if (i == depth.get().value) {
                print("-> ")
            } else {
                print("   ")
            }
        }
        print(method)
        print("(")
        arguments.forEachIndexed { index, argument ->
            if (index != 0) {
                print(", ")
            }
            print(argument)
        }
        println(")")

        ++depth.get().value
    }

    @Advice.OnMethodExit
    @JvmStatic
    fun onExit() {
        val depth = Stats.callStackDepth
        --depth.get().value
    }
}