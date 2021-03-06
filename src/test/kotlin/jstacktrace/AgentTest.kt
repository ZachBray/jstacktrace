/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package jstacktrace

import net.bytebuddy.agent.ByteBuddyAgent
import org.agrona.IoUtil
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentTest {
    @Test
    fun shouldTraceSelectedMethodCalls() {
        // Arrange
        val tempDir = Files.createTempDirectory("jstacktrace-agent-test")
        val instrumentation = ByteBuddyAgent.install()
        try {
            val filterSpec = """
                jstacktrace.TestFunctions::fib
            """.trimIndent()
            Arguments.time = { 100 }
            // Act
            attach(filterSpec, tempDir, instrumentation)
            TestFunctions.fib(5)
            // Assert
            val threadId = Thread.currentThread().id
            val expectedOutputFile = tempDir.resolve("trace-$threadId.log").toFile()
            assertTrue(expectedOutputFile.exists())
            assertEquals("""
              Thread name: main
              [ms: 100] jstacktrace.TestFunctions::fib(5)
              [ms: 100]  |-> jstacktrace.TestFunctions::fib(4)
              [ms: 100]  |    |-> jstacktrace.TestFunctions::fib(3)
              [ms: 100]  |    |    |-> jstacktrace.TestFunctions::fib(2)
              [ms: 100]  |    |    |    |-> jstacktrace.TestFunctions::fib(1)
              [ms: 100]  |    |    |    |-> jstacktrace.TestFunctions::fib(0)
              [ms: 200]  |    |    |    |    |-o took: 100 ms
              [ms: 200]  |    |    |    |-o took: 100 ms
              [ms: 200]  |    |    |-> jstacktrace.TestFunctions::fib(1)
              [ms: 200]  |    |    |-o took: 100 ms
              [ms: 200]  |    |-> jstacktrace.TestFunctions::fib(2)
              [ms: 200]  |    |    |-> jstacktrace.TestFunctions::fib(1)
              [ms: 200]  |    |    |-> jstacktrace.TestFunctions::fib(0)
              [ms: 300]  |    |    |    |-o took: 100 ms
              [ms: 300]  |    |    |-o took: 100 ms
              [ms: 300]  |    |-o took: 200 ms
              [ms: 300]  |-> jstacktrace.TestFunctions::fib(3)
              [ms: 300]  |    |-> jstacktrace.TestFunctions::fib(2)
              [ms: 300]  |    |    |-> jstacktrace.TestFunctions::fib(1)
              [ms: 300]  |    |    |-> jstacktrace.TestFunctions::fib(0)
              [ms: 400]  |    |    |    |-o took: 100 ms
              [ms: 400]  |    |    |-o took: 100 ms
              [ms: 400]  |    |-> jstacktrace.TestFunctions::fib(1)
              [ms: 400]  |    |-o took: 100 ms
              [ms: 400]  |-o took: 300 ms

            """.trimIndent(), expectedOutputFile.readText())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IoUtil.delete(tempDir.toFile(), true)
        }
    }
}

object TestFunctions {
    fun fib(n: Int): Int =
            if (n == 0) {
                val t = Arguments.time()
                Arguments.time = { t + 100 }
                0
            }
            else if (n == 1) 0
            else fib(n - 1) + fib(n - 2)
}
