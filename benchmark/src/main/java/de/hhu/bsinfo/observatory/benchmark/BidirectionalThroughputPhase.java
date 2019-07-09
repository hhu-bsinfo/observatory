package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BidirectionalThroughputPhase extends ThroughputPhase {

    private final ThroughputPhase sendPhase;
    private final ThroughputPhase receivePhase;

    BidirectionalThroughputPhase(ThroughputPhase sendPhase, ThroughputPhase receivePhase, Map<Integer, Integer> measurementOptions) {
        super(sendPhase.getBenchmark(), BenchmarkMode.SEND, measurementOptions);

        this.sendPhase = sendPhase;
        this.receivePhase = receivePhase;
    }

    @Override
    protected Status executeSingleMeasurement(Measurement measurement) {
        ThroughputMeasurement sendMeasurement = new ThroughputMeasurement(measurement.getOperationCount(), measurement.getOperationSize());
        ThroughputMeasurement receiveMeasurement = new ThroughputMeasurement(measurement.getOperationCount(), measurement.getOperationSize());

        AtomicReference<Status> sendStatus = new AtomicReference<>();
        AtomicReference<Status> receiveStatus = new AtomicReference<>();

        Thread sendThread = new Thread(() -> sendStatus.set(sendPhase.executeSingleMeasurement(sendMeasurement)), "SendThread");
        Thread receiveThread = new Thread(() -> receiveStatus.set(receivePhase.executeSingleMeasurement(receiveMeasurement)), "RecvThread");

        sendThread.start();
        receiveThread.start();

        try {
            sendThread.join();
            receiveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(sendStatus.get() != Status.OK) {
            return sendStatus.get();
        } else if(receiveStatus.get() != Status.OK) {
            return receiveStatus.get();
        } else {
            ThroughputMeasurement throughputMeasurement = (ThroughputMeasurement) measurement;

            throughputMeasurement.setTotalTime(sendMeasurement.getTotalTime());
            throughputMeasurement.setOperationThroughput(sendMeasurement.getOperationThroughput() + receiveMeasurement.getDataThroughput());
            throughputMeasurement.setDataThroughput(sendMeasurement.getDataThroughput() + receiveMeasurement.getDataThroughput());

            return Status.OK;
        }
    }
}
