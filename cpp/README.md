# Observatory C++ implementation

This is the C++ implementation of Observatory, written in C++-11 and using CMake as build system.

## Build instructions

Observatory depends on [nlohmann_json](https://github.com/nlohmann/json), [log4cpp](http://log4cpp.sourceforge.net/). Make sure to have these installed, before building Observatory.

Each Observatory binding can be build into a standalone application. The bindings are located in the subdirectory `bindings`. To build a binding you can run the following command from the project's root directory:

```
./build.sh <binding-name>
```

For example, to build the `socket-binding` you can run:

```
./build.sh socket-binding
```

It is also possible to build multiple implementations at once, by providing multiple names, seperated by spaces.

## Run instructions

After building a binding you need to change into the build output directory:

```
cd  build/<binding-subdirectory>/
```

After that, simply run `./bin/observatory/`.

## Bindings

The following bindings are implemented for the C++ version of Observatory:
 - [Dummy Binding](https://github.com/hhu-bsinfo/observatory/tree/development/cpp/src/bindings/dummy-binding): An example binding, that can be used as a starting point for new implementations.
 - [Socket Binding](https://github.com/hhu-bsinfo/observatory/tree/development/cpp/src/bindings/socket-binding): This implementation uses plain sockets and does **not** support RDMA.
 - [Verbs Binding](https://github.com/hhu-bsinfo/observatory/tree/development/cpp/src/bindings/verbs-binding): This implementation uses the standard native ibverbs library access InfiniBand hardware.