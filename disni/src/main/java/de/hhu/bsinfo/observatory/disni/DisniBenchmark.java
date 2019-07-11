package de.hhu.bsinfo.observatory.disni;

import com.ibm.disni.verbs.RdmaConnParam;
import com.ibm.disni.verbs.RdmaEventChannel;
import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisniBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisniBenchmark.class);

    private RdmaConnParam connectionParameter;
    private RdmaEventChannel eventChannel;

    @Override
    protected Status initialize() {
        connectionParameter = new RdmaConnParam();

        try {
            this.connectionParameter.setInitiator_depth((byte) 1);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Initiator depth'!");
        }

        try {
            this.connectionParameter.setResponder_resources((byte) 1);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Responder resources'!");
        }

        try{
            this.connectionParameter.setRetry_count((byte) 3);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Retry count'!");
        }

        try {
            this.connectionParameter.setRnr_retry_count((byte) 6);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'RNR Retry count'!");
        }

        try {
            eventChannel = RdmaEventChannel.createEventChannel();
        } catch (IOException e) {
            LOGGER.error("Unable to create event channel");
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status connect(InetSocketAddress serverAddress) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status cleanup() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status measureMessagingThroughput(BenchmarkMode mode, ThroughputMeasurement measurement) {
        return Status.NOT_IMPLEMENTED;
    }
}
