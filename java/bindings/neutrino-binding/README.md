# neutrino binding for Observatory (Java)

This binding uses the [neutrino](https://github.com/hhu-bsinfo/neutrino) library to access InfiniBand hardware. It is implemented using Java 11.

neutrino implements a thin JNI-layer to access the native ibverbs. It uses on-heap proxy objects to access native (off-heap) structs directly, allowing developers to manipulate native data structures without the need for copying them between the Java and native space.

## Build instructions

To build Observatory using this binding, run the following command from the `java` directory:

```
./gradlew bindings:neutrino-binding:installDist
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/neutrino-binding/install/observatory/
./bin/observatory
```