# Observatory

<p align="center">
  <a href="https://www.cs.hhu.de/en/research-groups/operating-systems-prof-dr-michael-schoettner/research/observatory.html"><img src="res/logo.svg" width=300></a>
</p>

<p align="center">
  <a href="https://travis-ci.org/hhu-bsinfo/observatory"><img src="https://www.travis-ci.org/hhu-bsinfo/observatory.svg?branch=master"></a>
  <a href="https://openjdk.java.net/projects/jdk8/"><img src="https://img.shields.io/badge/java-8-blue.svg"></a>
  <a href="https://isocpp.org/"><img src="https://img.shields.io/badge/C%2B%2B-11-blue"></a>
  <a href="https://github.com/hhu-bsinfo/observatory/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-GPLv3-orange.svg"></a>
</p>

## Introduction

Observatory is an extensible Java framework for benchmarking InfiniBand verbs implementations. It is being developed as a research project by the [Operating Systems group](https://www.cs.hhu.de/en/research-groups/operating-systems.html) at the *Heinrich Heine University Düsseldorf*.

<p align="center">
  <a href="https://www.uni-duesseldorf.de/home/en/home.html"><img src="res/hhu.svg" width=300></a>
</p>

It is intended to benchmark fundamental point-to-point connections by measuring uni- and bidirectional throughput, one-sided latency and full round-trip time. It also uses the [jDetector](https://github.com/hhu-bsinfo/jdetector) library to determine the data overhead, imposed by any software defined protocol.

Observatory provides a single lean interface, that combines messaging, as well as RDMA operations, so that implementations for different libaries can easily be developed. To configure a set of benchmark runs, a JSON file can be used.

## Build and Run Instructions

Observatory offers a [Java](https://github.com/hhu-bsinfo/observatory/tree/development/java) and a [C++](https://github.com/hhu-bsinfo/observatory/tree/development/cpp) implementation. For instructions on how to build and run each implemenation, see the README file in the corresponding subdirectory.

## Configuration

Observatory uses a simple server-client architecture to setup a connection between two nodes. It can be configured with the following command line arguments:

 - `--server, -s`: Configure this instance as a server and wait for a client instance to connect.
 - `--remote, -r <address>:<port>`: Connect this instance to a remote server.
 - `--address, -a <address>:<port>`: Use a specific address to bind this instance to (Default: `0.0.0.0:2998`).
 - `--retries, -t <number>`: Specify the amount of retries after a failed connection attempt (Default: 10).
 - `--output, -o <path>`: Specify the path to where the result files shall be written (Default: `./result/`).
 - `--config, -c <path>`: Specify configuration file to be used.

Observatory uses a JSON-file to configure benchmark runs, which specifies the following parameters. For more details and examples, see the [examples foder](https://github.com/hhu-bsinfo/observatory/blob/master/example/config).

 - `resultName`: The name to use in the result files.
 - `parameters`: An array of key-value tuples, that are defined by each binding. See the README file in each binding's subdirectory for further details.
 - `detector`: Configuration parameters for the `jDetector` libraries, which measures raw data throughput by reading the HCA's hardware counters. When running the Java version of Observatory, make sure to have the native library `libdetectorJNI.so` installed, before enabling this feature.
   * `enabled`: Boolean switch to enable/disable the use of `jDetector`.
   * `deviceNumber`: The device number to use for reading the hardware counters (in most cases, `0` should work fine).
   * `mode`: Either `mad` to utilize the `ibmad`-library (needs root privileges), or `compat` to read the hardware counters from the local filesystem. If you are unsure, use `compat`, since it will work fine for Observatory's use case. For more details, see the [jDetector repository](https://github.com/hhu-bsinfo/jdetector).
 - `operations`:An array of objects, describing the benchmark operations to  execute. An operation is defined as a set of iterations, where each iteration describes a single benchmark run.
   * `name`: The name of the benchmark operation. Valid names are `MessagingThroughput`, `MessagingLatency`, `MessagingPingPong`, `RdmaWriteThroughput`, `RdmaWriteLatency`, `RdmaReadThroughput`, `RdmaReadLatency`.
   * `modes`: An array containing the benchmark modes to execute. Valid modes are `unidirectional` and `bidirectional`. Each benchmark operation will be executed once for each mode.
   * `repetitions`: The amount of times each iteration shall be repeated. This useful, if one plans on calculating average values and deviations afterwards.
   * `iterations`: An array of objects describing the benchmark iterations.
     - `size`: The buffer size to be used (e.g. message size).
     - `count`: The amount of times the buffer shall be transferred.
     - `warmUp`: The amount of warm up iterations to execute. A warm up iteration is exactly the same as a normal iteration without measuring time. If you do not want to execute warm up iterations, just set this value to `0`.

## Architecture

<p align="center">
  <img src="res/architecture.svg" width=500>
</p>

The benchmark itself is made up of six phases that are executed consecutively:

 1. **Initialization**: During this phase, the client should allocate any needed resources (e.g open an IB context and allocate a Protection Domain). Client-specific configuration parameters, that are defined in the configuration file (JSON), are passed to the client as key-value tuples. This can be used to pass IB related parameters to the client (e.g. the device and port number).
 2. **Connection**: A connection is setup, after IB connection information has been exchanged (e.g. via TCP sockets).
 3. **Preparation**: The operation size, which dictates the size of the messages being sent, respectively the size of RDMA writes/reads being performed, is passed to the client, allowing it to allocate matching buffers to use in the benchmark. It is also reasonable to preallocate reusable Work Requests during this phase.
 4. **Warmup**: A configurable amount of operations are executed as a warmup, allowing the JVM and its JIT to optimize the benchmark code.
 5. **Operation**: This is the main phase of the benchmark, executing the configured amount of operations. If a bidirectional benchmark run is performed, dedicated threads for sending and receiving are started. If a throughput benchmark is being performed, two timestamps will be taken right before the first operation starts and right after the last one has finished. Otherwise, if a latency measurement is performed, the time needed for each operation is measured and stored in an array. This allows calculating percentiles afterwards. Furthermore, the benchmark utilizes the performance counters of the IB HCA to determine the raw amount of data being sent/received. Thus, it is possible to calculate the overhead added by any software defined protocol which is especially relevant for the socket-based libraries.
 6. **Cleanup**: The benchmark is finished, resources shall be freed and all connections shall be closed.
  
Observatory currently provides Java implementations for TCP-Sockets, [jVerbs](https://www.ibm.com/support/knowledgecenter/en/SSYKE2_7.1.0/com.ibm.java.lnx.71.doc/diag/understanding/rdma_jverbs.html), [DiSNI](https://developer.ibm.com/technologies/analytics/projects/direct-storage-and-networking-interface-disni/), [JUCX](https://www.openucx.org/) and [neutrino](https://www.cs.hhu.de/en/research-groups/operating-systems-prof-dr-michael-schoettner/research/neutrino.html), as well as native implementations for TCP-Sockets and [libibverbs](https://github.com/linux-rdma/rdma-core).

## Publications

 - *Performance analysis and evaluation of Java-based InfiniBand solutions*, Fabian Ruhland, Filip Krakowski, Michael Schöttner, to appear in: Proceedings of the IEEE International Symposium on Parallel and Distributed Computing ([ISPDC](https://ispdc2020.ipipan.waw.pl/)), Warsaw, Poland, 2020.
