package jstacktrace

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import net.bytebuddy.agent.ByteBuddyAgent
import java.io.File

class App : CliktCommand() {
    override fun run() = Unit
}

class Trace : CliktCommand() {
    private val processId : String by argument("pid", help = "The pid of the JVM.")

    private val filterFile : File by argument("filter", help = "A file containing a list of methods to trace.").convert {
        val file = File(it)
        if (!file.exists()) {
            fail("File does not exist: $it")
        }
        file
    }


    override fun run() {
        val jarLocation = File(Trace::class.java.protectionDomain.codeSource.location.toURI().path)
        ByteBuddyAgent.attach(jarLocation, processId, filterFile.absolutePath)
    }
}

fun main(args: Array<String>) = App().subcommands(Trace()).main(args)
