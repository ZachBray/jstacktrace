import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.ajalt:clikt:1.6.0")
    implementation("net.bytebuddy:byte-buddy:1.9.10")
    implementation("net.bytebuddy:byte-buddy-agent:1.9.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    applicationName = "jstacktrace"
    mainClassName = "jstacktrace.AppKt"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val jar: Jar by tasks
jar.apply {
    manifest {
        attributes(mapOf(
                "Premain-Class" to "jstacktrace.AgentKt",
                "Agent-Class" to "jstacktrace.AgentKt",
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true"
        ))
    }
}