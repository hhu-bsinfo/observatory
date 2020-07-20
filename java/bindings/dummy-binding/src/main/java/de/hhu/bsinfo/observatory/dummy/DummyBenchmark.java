package de.hhu.bsinfo.observatory.dummy;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

import java.net.InetSocketAddress;

public class DummyBenchmark extends Benchmark {
    @Override
    protected Status initialize() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status prepare(int operationSize, int operationCount) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status cleanup() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status fillReceiveQueue() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendSingleMessage() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status performSingleRdmaOperation(RdmaMode mode) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status performPingPongIterationServer() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status performPingPongIterationClient() {
        return Status.NOT_IMPLEMENTED;
    }
}
