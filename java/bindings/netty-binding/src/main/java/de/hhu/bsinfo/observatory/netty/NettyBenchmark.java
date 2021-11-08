package de.hhu.bsinfo.observatory.netty;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyBenchmark.class);

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Handler handler;
    private Channel channel;

    private int messageSize;
    private ByteBuf sendBuffer;

    @Override
    protected Status initialize() {
        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        LOGGER.info("Starting server on [{}]", bindAddress);
        final EventLoopGroup acceptorGroup = new NioEventLoopGroup();
        final ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(acceptorGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel channel) {
                        channel.closeFuture().addListener(future -> LOGGER.info("Socket channel closed"));
                        NettyBenchmark.this.channel = channel;
                        synchronized (workerGroup) {
                            workerGroup.notifyAll();
                        }
                    }
                });

        final ChannelFuture bindFuture = bootstrap.bind(bindAddress).addListener(future -> {
            if (future.isSuccess()) {
                LOGGER.info("Server is running");
            } else {
                LOGGER.error("Unable to start server", future.cause());
            }
        });

        try {
            if (!bindFuture.sync().isSuccess()) {
                return Status.NETWORK_ERROR;
            }

            synchronized (workerGroup) {
                workerGroup.wait();
            }

            return Status.OK;
        } catch (InterruptedException e) {
            LOGGER.error("Failed to wait for an incoming connection", e);
            return Status.IO_ERROR;
        } finally {
            acceptorGroup.shutdownGracefully();
        }
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        LOGGER.info("Connecting to server [{}]", serverAddress);
        final Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(final SocketChannel channel) {
                    NettyBenchmark.this.channel = channel;
                }
            });

        try {
            final ChannelFuture connectFuture = bootstrap.connect(serverAddress, bindAddress);
            connectFuture.channel().closeFuture().addListener(future -> LOGGER.info("Socket channel closed"));

            return connectFuture.sync().isSuccess() ? Status.OK : Status.NETWORK_ERROR;
        } catch (InterruptedException e) {
            LOGGER.error("Failed to connect to the remote server", e);
            return Status.IO_ERROR;
        }
    }

    @Override
    protected Status prepare(int operationSize, int operationCount) {
        messageSize = operationSize;
        sendBuffer = channel.alloc().buffer(messageSize);
        for (int i = 0; i < operationSize; i++) {
            sendBuffer.writeByte(i);
        }

        handler = new Handler(operationSize, 0);
        channel.pipeline().addLast(handler);

        return Status.OK;
    }

    @Override
    protected Status cleanup() {
        try {
            return workerGroup.shutdownGracefully().sync().isSuccess() ? Status.OK : Status.NETWORK_ERROR;
        } catch (InterruptedException e) {
            return Status.IO_ERROR;
        }
    }

    @Override
    protected Status fillReceiveQueue() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        sendBuffer.retain(messageCount);
        handler.reset(1);

        try {
            for (int i = 0; i < messageCount; i++) {
                channel.writeAndFlush(sendBuffer).sync();
                sendBuffer.resetReaderIndex();
            }

            while (!handler.isFinished()) {}

            return Status.OK;
        } catch (InterruptedException e) {
            LOGGER.error("Thread has been interrupted unexpectedly, while sending a message", e);
            return Status.IO_ERROR;
        }
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        sendBuffer.retain(1);
        handler.reset(messageCount);
        while (!handler.isFinished()) {}

        try {
            channel.writeAndFlush(sendBuffer).sync();
        } catch (InterruptedException e) {
            LOGGER.error("Thread has been interrupted unexpectedly, while sending a message", e);
            return Status.IO_ERROR;
        }

        return Status.OK;
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
        sendBuffer.retain(1);
        handler.reset(1);

        try {
            channel.writeAndFlush(sendBuffer).sync();
            sendBuffer.resetReaderIndex();

            while (!handler.isFinished()) {}

            return Status.OK;
        } catch (InterruptedException e) {
            LOGGER.error("Thread has been interrupted unexpectedly, while performing a ping pong operation", e);
            return Status.IO_ERROR;
        }
    }

    @Override
    protected Status performPingPongIterationClient() {
        sendBuffer.retain(1);
        handler.reset(1);

        try {
            while (!handler.isFinished()) {}

            channel.writeAndFlush(sendBuffer).sync();
            sendBuffer.resetReaderIndex();

            return Status.OK;
        } catch (InterruptedException e) {
            LOGGER.error("Thread has been interrupted unexpectedly, while performing a ping pong operation", e);
            return Status.IO_ERROR;
        }
    }
}
