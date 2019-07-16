package de.hhu.bsinfo.observatory.disni;

import java.nio.ByteBuffer;

public class MemoryRegionInformation {

    private long address;
    private int remoteKey;

    public MemoryRegionInformation(long address, int remoteKey) {
        this.address = address;
        this.remoteKey = remoteKey;
    }

    public static int getSizeInBytes() {
        return Long.BYTES + Integer.BYTES;
    }

    public static MemoryRegionInformation fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        return new MemoryRegionInformation(buffer.getLong(), buffer.getInt());
    }

    public long getAddress() {
        return address;
    }

    public int getRemoteKey() {
        return remoteKey;
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getSizeInBytes());

        buffer.putLong(address);
        buffer.putInt(remoteKey);

        return buffer.array();
    }

    @Override
    public String toString() {
        return "MemoryRegionInfo {" +
                "\n\taddress=" + address +
                ",\n\tremoteKey=" + remoteKey +
                "\n}";
    }
}