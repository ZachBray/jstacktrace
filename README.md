# jstacktrace

This is a tool to trace the execution of java methods. It is not optimised and may have a detrimental effect on
application performance. Therefore, it is dangerous to use in production.

## Build from source

Create a shadow jar by running the following command.

```bash
./gradlew installShadowDist
```

This will create a fat jar file under `build/install/jstacktrace-shadow/lib/jstacktrace-all.jar`. Copy it somewhere
convenient.

## Usage

Before tracing, we must create a filter specification file to describe which methods to trace.
This file should contain lines in the format described below.

```
fully.qualified.Type1::method1
fully.qualified.Type1::method2
fully.qualified.Type2::method1
```

We also need to obtain the a jstacktrace release from GitHub.

```bash
wget https://github.com/ZachBray/jstacktrace/releases/download/v0.1.0/jstacktrace-all.jar
```

### Attach to an existing process

To attach to an existing process, we run jstacktrace passing in the pid of the application you want to trace.

```bash
java -jar jstacktrace-all.jar trace <PID> <FILTER-SPEC-FILE> <OUTPUT-DIR>
```

### Attach when starting a process

To run jstacktace from process start, we use JVM args.

```bash
java -javaagent:path/to/jstacktrace-all.jar=<FILTER-SPEC-FILE>|<OUTPUT-DIR> ...
```

### Attach to Gradle test

To attach to a Gradle test, we can use JVM args like above. Edit our the relevant `build.gradle` and configure the test section to pass in our `-javaagent` JVM arg.

```gradle
test {
  jvmArgs '-javaagent:path/to/jstacktrace-all.jar=<FILTER-SPEC-FILE>|<OUTPUT-DIR>'
}
```

## Output

Attaching to a process using any of the methods above should write a trace of method calls of each thread to its own file under the output directory. For example:

```
jstacktrace.TestFunctions::fib(5)
 |-> jstacktrace.TestFunctions::fib(4)
 |    |-> jstacktrace.TestFunctions::fib(3)
 |    |    |-> jstacktrace.TestFunctions::fib(2)
 |    |    |    |-> jstacktrace.TestFunctions::fib(1)
 |    |    |    |-> jstacktrace.TestFunctions::fib(0)
 |    |    |-> jstacktrace.TestFunctions::fib(1)
 |    |-> jstacktrace.TestFunctions::fib(2)
 |    |    |-> jstacktrace.TestFunctions::fib(1)
 |    |    |-> jstacktrace.TestFunctions::fib(0)
 |-> jstacktrace.TestFunctions::fib(3)
 |    |-> jstacktrace.TestFunctions::fib(2)
 |    |    |-> jstacktrace.TestFunctions::fib(1)
 |    |    |-> jstacktrace.TestFunctions::fib(0)
 |    |-> jstacktrace.TestFunctions::fib(1)

```
