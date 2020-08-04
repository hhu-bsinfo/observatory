# Dummy binding for Observatory (Java)

This binding shall serve as an example and a starting point for new implementations. It is not functional, as each implemented method just returns `Status.NOT_IMPLEMENTED`. It is implemented using Java 8.

## Build instructions

To build Observatory using this binding, run the following command from the `java` directory:

```
./gradlew bindings:dummy-binding:installDist
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/dummy-binding/install/observatory/
./bin/observatory
```

## Bindings

The following bindings are implemented for the C++ version of Observatory:
 - [Dummy Binding](https://github.com/hhu-bsinfo/observatory/tree/development/cpp/src/bindings/dummy-binding): An example binding, that can be used as a starting point for new implementations.
 - [Socket Binding](https://github.com/hhu-bsinfo/observatory/tree/development/cpp/src/bindings/socket-binding): This implementation uses plain sockets and does **not** support RDMA.
 - [Verbs Binding](https://github.com/hhu-bsinfo/observatory/tree/development/cpp/src/bindings/verbs-binding): This implementation uses the standard native ibverbs library access InfiniBand hardware.