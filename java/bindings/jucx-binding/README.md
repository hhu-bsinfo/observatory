# JUCX binding for Observatory (Java)

This binding uses the [UCX](https://www.openucx.org/) library with its Java binding [JUCX](https://github.com/openucx/ucx/tree/master/bindings/java) to access InfiniBand hardware. It is implemented using Java 8.

## Build instructions

To build Observatory using this binding, run the following command from the `java` directory:

```
./gradlew bindings:jucx-binding:installDist
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/jucx-binding/install/observatory/
./bin/observatory
```