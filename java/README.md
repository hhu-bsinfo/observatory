# Observatory Java implementation

This is the Java implementation of Observatory. The benchmark itself is written in Java 8, but implementations can use newer versions of Java. It uses Gradle as build system.

## Build instructions

Each Observatory binding can be build into a standalone application. The bindings are located in the subdirectory `bindings`. To build a binding you can run the following command from the project's root directory:

```
./gradlew bindings:<binding-project>:installDist
```

For example, to build the `socket-binding` you can run:

```
./gradlew bindings:socket-binding:installDist
```

See each binding's subdirectory for detailed instructions

## Run instructions

Observatory uses the [jDetector](https://github.com/hhu-bsinfo/jdetector) library to determine data overhead. Make sure to have the native library `libdetectorJNI.so` installed, before running Observatory with `detector` enabled.

After building a binding you need to change into the build output directory:

```
cd  build/<binding-subdirectory>/install/observatory/
```

After that, simply run `./bin/observatory/`.

## Bindings

The following bindings are implemented for the Java version of Observatory:
 - [Dummy Binding](https://github.com/hhu-bsinfo/observatory/tree/development/java/bindings/dummy-binding): An example binding, that can be used as a starting point for new implementations.
 - [Socket Binding](https://github.com/hhu-bsinfo/observatory/tree/development/java/bindings/socket-binding): This implementation uses plain Java sockets and does **not** support RDMA.
 - [jVerbs Binding](https://github.com/hhu-bsinfo/observatory/tree/development/java/bindings/jverbs-binding): This implementation uses IBM's proprietary *jVerbs* library to access InfiniBand hardware.
 - [DiSNI Binding](https://github.com/hhu-bsinfo/observatory/tree/development/java/bindings/disni-binding): This implementation uses IBM's open source library *DiSNI* to access InfiniBand hardware.
 - [neutrino Binding](https://github.com/hhu-bsinfo/observatory/tree/development/java/bindings/neutrino-binding): This implementation uses open source library *neutrino* (developed by the Operating Systems group at Heinrich Heine University Düsseldorf) to access InfiniBand hardware.
 - [JUCX Binding](https://github.com/hhu-bsinfo/observatory/tree/development/java/bindings/jucx-binding): This implementation uses the open source library *UCX* and it's Java binding *JUCX* to access InfiniBand hardware.