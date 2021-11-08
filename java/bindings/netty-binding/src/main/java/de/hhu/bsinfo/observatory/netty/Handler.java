package de.hhu.bsinfo.observatory.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    private final int messageSize;
    private int messageCount;

    private int receivedMessages = 0;
    private int receivedBytes = 0;
    private volatile boolean finished = false;

    public Handler(final int messageSize, final int messageCount) {
        this.messageSize = messageSize;
        this.messageCount = messageCount;
    }

    @Override
    public void channelActive(final ChannelHandlerContext context) {
        if (context.channel().parent() != null) {
            LOGGER.info("Accepted incoming connection from [{}]", context.channel().remoteAddress());
        } else {
            LOGGER.info("Successfully connected to [{}]", context.channel().remoteAddress());
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        final ByteBuf buffer = (ByteBuf) message;

        receivedBytes += buffer.readableBytes();
        while (receivedBytes >= messageSize) {
            receivedBytes -= messageSize;
            receivedMessages++;
        }
        buffer.release();

        if (receivedMessages >= messageCount) {
            finished = true;
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        LOGGER.error("An exception occurred", cause);
        context.channel().close();
    }

    public boolean isFinished() {
        return finished;
    }

    public void reset(int messageCount) {
        this.messageCount = messageCount;
        receivedMessages = 0;
        finished = false;
    }
}
