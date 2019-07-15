package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;
import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class RdmaWriteThroughputPhase extends MessagingThroughputPhase {

    private static final String STOP_MESSAGE = "stop";

    RdmaWriteThroughputPhase(Benchmark benchmark, BenchmarkMode mode, Map<Integer, Integer> measurementOptions) {
        super(benchmark, mode, measurementOptions);
    }

    @Override
    protected Status executeSingleMeasurement(Measurement measurement) {
        if(getMode() == BenchmarkMode.SEND) {
            Status status = getBenchmark().measureRdmaThroughput(getMode(), RdmaMode.WRITE, (ThroughputMeasurement) measurement);

            try {
                DataOutputStream stream = new DataOutputStream(getBenchmark().getOffChannelSocket().getOutputStream());
                stream.write(STOP_MESSAGE.getBytes());

                return status;
            } catch (IOException e) {
                e.printStackTrace();
                return Status.NETWORK_ERROR;
            }
        } else {
            try {
                byte[] bytes = new byte[STOP_MESSAGE.getBytes().length];
                DataInputStream stream = new DataInputStream(getBenchmark().getOffChannelSocket().getInputStream());

                stream.readFully(bytes);

                if (!new String(bytes).equals(STOP_MESSAGE)) {
                    return Status.NETWORK_ERROR;
                }

                return Status.OK;
            } catch (IOException e) {
                e.printStackTrace();
                return Status.NETWORK_ERROR;
            }
        }
    }
}
