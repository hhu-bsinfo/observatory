package de.hhu.bsinfo.observatory.neutrino;

import java.nio.ByteBuffer;

class MemoryRegionInformation {

    private long address;
    private int remoteKey;

    MemoryRegionInformation(long address, int remoteKey) {
        this.address = address;
        this.remoteKey = remoteKey;
    }

    static int getSizeInBytes() {
        return Long.BYTES + Integer.BYTES;
    }

    static MemoryRegionInformation fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        return new MemoryRegionInformation(buffer.getLong(), buffer.getInt());
    }

    long getAddress() {
        return address;
    }

    int getRemoteKey() {
        return remoteKey;
    }

    byte[] toBytes() {
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