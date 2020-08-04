# Dummy binding for Observatory (C++)

This binding shall serve as an example and a starting point for new implementations. It is not functional, as each implemented method just returns `Status::NOT_IMPLEMENTED`.

## Build instructions

To build Observatory using this binding, run the following command from the `cpp` directory:

```
./build.sh dummy-binding
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/dummy-binding/
./bin/observatory
```