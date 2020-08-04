# Socket binding for Observatory (C++)

This binding does not access InfiniBand hardware directly, but uses plain sockets. Its purpose is to be used with InfiniBand solutions, that intercept socket traffic and redirect it over an InfiBand HCA. It does not support RDMA benchmarks.

The socket binding has been tested with the following InfiniBand solutions:
 - [IP over InfiniBand](https://www.ietf.org/rfc/rfc4392.txt)
 - [libvma](https://github.com/Mellanox/libvma/) (by Mellanox)

## Build instructions

To build Observatory using this binding, run the following command from the `cpp` directory:

```
./build.sh socket-binding
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/socket-binding/
./bin/observatory
```