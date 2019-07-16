package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.util.concurrent.atomic.AtomicReference;

class WarmupPhase extends BenchmarkPhase {

    WarmupPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    Status execute() {
        AtomicReference<Status> sendStatus = new AtomicReference<>();
        AtomicReference<Status> receiveStatus = new AtomicReference<>();

        Thread sendThread = new Thread(() -> sendStatus.set(getBenchmark().benchmarkMessagingSendThroughput(1000)));
        Thread receiveThread = new Thread(() -> receiveStatus.set(getBenchmark().benchmarkMessagingReceiveThroughput(1000)));

        sendThread.start();
        receiveThread.start();

        try {
            sendThread.join();
            receiveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Status.UNKNOWN_ERROR;
        }

        if(sendStatus.get() != Status.OK) {
            return sendStatus.get();
        } else if(receiveStatus.get() != Status.OK) {
            return receiveStatus.get();
        } else {
            return Status.OK;
        }
    }
}
