package de.hhu.bsinfo.observatory.neutrino;

import java.nio.ByteBuffer;
import java.util.StringJoiner;

public class ConnectionInformation {

    private final byte portNumber;
    private final short localId;
    private final int queuePairNumber;

    ConnectionInformation(byte portNumber, short localId, int queuePairNumber) {
        this.portNumber = portNumber;
        this.localId = localId;
        this.queuePairNumber = queuePairNumber;
    }

    ConnectionInformation(ByteBuffer buffer) {
        portNumber = buffer.get();
        localId = buffer.getShort();
        queuePairNumber = buffer.getInt();
    }

    static int getSizeInBytes() {
        return Byte.BYTES + Short.BYTES + Integer.BYTES;
    }

    static ConnectionInformation fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        return new ConnectionInformation(buffer.get(), buffer.getShort(), buffer.getInt());
    }

    byte getPortNumber() {
        return portNumber;
    }

    short getLocalId() {
        return localId;
    }

    int getQueuePairNumber() {
        return queuePairNumber;
    }

    byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getSizeInBytes());

        buffer.put(portNumber);
        buffer.putShort(localId);
        buffer.putInt(queuePairNumber);

        return buffer.array();
    }

    @Override
    public String toString() {
        return "ConnectionInformation {" +
                "\n\tportNumber=" + portNumber +
                ",\n\tlocalId=" + localId +
                ",\n\tqueuePairNumber=" + queuePairNumber +
                "\n}";
    }
}
