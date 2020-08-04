# Socket binding for Observatory (C++)

This binding uses the standard native ibverbs library (availabe in the [rdma-core](https://github.com/linux-rdma/rdma-core) package) to access InfiniBand hardware. Its purpose is to get baseline results, that other implementations can be compared against.

## Build instructions

To build Observatory using this binding, run the following command from the `cpp` directory:

```
./build.sh verbs-binding
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/verbs-binding/
./bin/observatory
```