# NIO binding for Observatory (Java)

This binding does not access InfiniBand hardware directly, but uses Java NIO socket channels. Its purpose is to be used with InfiniBand solutions, that intercept socket traffic and redirect it over an InfiBand HCA. It is implemented using Java 8 and does not support RDMA benchmarks.

The socket binding has been tested with the following InfiniBand solutions:
 - [IP over InfiniBand](https://www.ietf.org/rfc/rfc4392.txt)
 - [hadroNIO](https://github.com/hhu-bsinfo/hadroNIO)

## Build instructions

To build Observatory using this binding, run the following command from the `java` directory:

```
./gradlew bindings:nio-binding:installDist
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/nio-binding/install/observatory/
./bin/observatory
```