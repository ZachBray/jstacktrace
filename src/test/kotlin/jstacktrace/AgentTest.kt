/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package jstacktrace

import net.bytebuddy.agent.ByteBuddyAgent
import kotlin.test.Test

class AgentTest {
    @Test
    fun shouldTraceSelectedMethodCalls() {
        val filterSpec = """
            jstacktrace.TestFunctions::fib
        """.trimIndent()
        val instrumentation = ByteBuddyAgent.install()
        attach(filterSpec, instrumentation)
        TestFunctions.fib(5)
    }
}

object TestFunctions {
    fun fib(n: Int): Int =
            if (n == 0) 0
            else if (n == 1) 0
            else fib(n - 1) + fib(n - 2)
}