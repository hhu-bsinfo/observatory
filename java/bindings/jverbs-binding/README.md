# jVerbs binding for Observatory (Java)

This binding uses IBM's proprietary [jVerbs library](https://www.ibm.com/support/knowledgecenter/SSYKE2_8.0.0/com.ibm.java.api.80.doc/com.ibm.net.rdma/com/ibm/net/rdma/jverbs/verbs/package-summary.html) to access InfiniBand hardware. It is implemented using Java 8 and can only be used with the [IBM SDK](https://developer.ibm.com/javasdk/downloads/sdk8/).

As DiSNI, it utilizes *Stateful verb methods* (`StatefulVerbsMethod` Java objects), which encapsulate the verb to call including all parameters with parameter serialization to native space. Once the object is prepared, it can be executed, which actually calls the native verb. These objects are reusable for further calls with the same parameters, to avoid repeated serialization and creating new objects which would burden garbage collection.

To make sure that SVM objects are being reused, this binding posts work requests in batches of 10.

## Build instructions

To build Observatory using this binding, run the following command from the `java` directory:

```
./gradlew bindings:jverbs-binding:installDist
```

Make sure that the IBM SDK is set as your current default JDK. Otherwise you nee to provide the path to your SDK installation to gradle with the parameter `-Dorg.gradle.java.home=<path/to/ibm-sdk>`

## Run instructions

To run Observatory using this binding, change into the build output directory and run the `observatory` executable:

```
cd build/jverbs-binding/install/observatory/
./bin/observatory
```