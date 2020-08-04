# DiSNI binding for Observatory (Java)

This binding uses IBM's [Direct Storage and Networking Interface](https://developer.ibm.com/technologies/analytics/projects/direct-storage-and-networking-interface-disni/) to access InfiniBand hardware. It is implemented using Java 8.

As jVerbs, it utilizes *Stateful verb methods* (`StatefulVerbsMethod` Java objects), which encapsulate the verb to call including all parameters with parameter serialization to native space. Once the object is prepared, it can be executed, which actually calls the native verb. These objects are reusable for further calls with the same parameters, to avoid repeated serialization and creating new objects which would burden garbage collection.

To make sure that SVM objects are being reused, this binding posts work requests in batches of 10.

## Build instructions

To build Observatory using this binding, run the following command from the `java` directory:

```
./gradlew bindings:disni-binding:installDist
```

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/disni-binding/install/observatory/
./bin/observatory
```