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

First, create a filter specification file to describe which methods to trace. This file should contain lines in the
format described below.

```
fully.qualified.Type1::method1
fully.qualified.Type1::method2
fully.qualified.Type2::method1
```

Second, run jstacktrace passing in the pid of the application you want to trace.

```bash
java -jar jstacktrace-all.jar trace <PID> <FILTER-SPEC-FILE> <OUTPUT-DIR>
```

This should write a trace of method calls of each thread to its own file under the output directory. For example:

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
