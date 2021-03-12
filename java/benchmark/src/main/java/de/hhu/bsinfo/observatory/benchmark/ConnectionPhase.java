package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

import java.net.InetSocketAddress;

class ConnectionPhase extends BenchmarkPhase {

    private static int portCounter = 1;

    ConnectionPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    Status execute() {
        Benchmark benchmark = getBenchmark();

        if(benchmark.isServer()) {
            InetSocketAddress bindAddress = new InetSocketAddress(benchmark.getBindAddress().getAddress(), benchmark.getBindAddress().getPort() + portCounter++);
            return benchmark.serve(bindAddress);
        } else {
            InetSocketAddress bindAddress = new InetSocketAddress(benchmark.getBindAddress().getAddress(), benchmark.getBindAddress().getPort() + portCounter);
            InetSocketAddress remoteAddress = new InetSocketAddress(benchmark.getRemoteAddress().getAddress(), benchmark.getRemoteAddress().getPort() + portCounter++);
            Status status = Status.UNKNOWN_ERROR;

            for(int i = 0; i < getBenchmark().getConnectionRetries(); i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}

                status = benchmark.connect(bindAddress, remoteAddress);

                if(status == Status.OK || status == Status.NOT_IMPLEMENTED) {
                    return status;
                }
            }

            return status;
        }
    }
}
