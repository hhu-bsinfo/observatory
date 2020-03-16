package de.hhu.bsinfo.observatory.neutrino;

import de.hhu.bsinfo.neutrino.verbs.AccessFlag;
import de.hhu.bsinfo.neutrino.verbs.CompletionQueue;
import de.hhu.bsinfo.neutrino.verbs.Context;
import de.hhu.bsinfo.neutrino.verbs.PortAttributes;
import de.hhu.bsinfo.neutrino.verbs.ProtectionDomain;
import de.hhu.bsinfo.neutrino.verbs.QueuePair;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectionContext implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionContext.class);

    private final Context context;
    private final ProtectionDomain protectionDomain;

    private final CompletionQueue sendCompletionQueue;
    private final CompletionQueue receiveCompletionQueue;

    private final QueuePair queuePair;

    private final PortAttributes port;

    ConnectionContext(int deviceNumber, int portNumber, int queueSize) throws IOException {
        int numDevices = Context.getDeviceCount();

        if(numDevices <= deviceNumber) {
            throw new InvalidParameterException("Invalid device number '" + deviceNumber + "'. Only " + numDevices + " InfiniBand " + (numDevices == 1 ? "device was" : "devices were") + " found in your system");
        }

        context = Context.openDevice(deviceNumber);
        if(context == null) {
            throw new IOException("Unable to open context");
        }

        LOGGER.info("Opened context for device {}", context.getDeviceName());

        protectionDomain = context.allocateProtectionDomain();
        if(protectionDomain == null) {
            throw new IOException("Unable to allocate protection domain");
        }

        LOGGER.info("Allocated protection domain");

        port = context.queryPort(portNumber);
        if(port == null) {
            throw new IOException("Unable to query port");
        }

        sendCompletionQueue = context.createCompletionQueue(queueSize, null);
        receiveCompletionQueue = context.createCompletionQueue(queueSize, null);
        if(sendCompletionQueue == null || receiveCompletionQueue == null) {
            throw new IOException("Unable to create completion queues");
        }

        LOGGER.info("Created completion queues");

        queuePair = protectionDomain.createQueuePair(new QueuePair.InitialAttributes.Builder(
                QueuePair.Type.RC, sendCompletionQueue, receiveCompletionQueue, queueSize, queueSize, 1, 1).build());
        if(queuePair == null) {
            throw new IOException("Unable to create queue pair");
        }

        LOGGER.info("Created queue pair");

        if(!queuePair.modify(QueuePair.Attributes.Builder.buildInitAttributesRC((short) 0, (byte) 1, AccessFlag.LOCAL_WRITE, AccessFlag.REMOTE_READ, AccessFlag.REMOTE_WRITE))) {
            throw new IOException(("Unable to move queue pair into INIT state"));
        }

        LOGGER.info("Moved queue pair into INIT state");
    }

    void connect(Socket socket) throws IOException {
        ConnectionInformation localInfo = new ConnectionInformation((byte) 1, port.getLocalId(), queuePair.getQueuePairNumber());
        byte[] remoteBytes = new byte[ConnectionInformation.getSizeInBytes()];

        LOGGER.info("Sending local connection information: {}", localInfo);

        new DataOutputStream(socket.getOutputStream()).write(localInfo.toBytes());

        LOGGER.info("Waiting for remote connection information");

        new DataInputStream(socket.getInputStream()).readFully(remoteBytes);
        ConnectionInformation remoteInfo = ConnectionInformation.fromBytes(remoteBytes);

        LOGGER.info("Received connection information: {}", remoteInfo);

        if(!queuePair.modify(QueuePair.Attributes.Builder.buildReadyToReceiveAttributesRC(
                remoteInfo.getQueuePairNumber(), remoteInfo.getLocalId(), remoteInfo.getPortNumber()))) {
            throw new IOException("Unable to move queue pair into RTR state");
        }

        LOGGER.info("Moved queue pair into RTR state");

        if(!queuePair.modify(QueuePair.Attributes.Builder.buildReadyToSendAttributesRC())) {
            throw new IOException("Unable to move queue pair into RTS state");
        }

        LOGGER.info("Moved queue pair into RTS state");
    }

    ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

    CompletionQueue getSendCompletionQueue() {
        return sendCompletionQueue;
    }

    CompletionQueue getReceiveCompletionQueue() {
        return receiveCompletionQueue;
    }

    QueuePair getQueuePair() {
        return queuePair;
    }

    @Override
    public void close() {
        queuePair.close();
        sendCompletionQueue.close();
        protectionDomain.close();
        context.close();
    }
}
