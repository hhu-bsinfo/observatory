package de.hhu.bsinfo.observatory.nio;

public interface Handler extends Runnable {

    boolean isFinished();
    void reset(int messageCount);
}
