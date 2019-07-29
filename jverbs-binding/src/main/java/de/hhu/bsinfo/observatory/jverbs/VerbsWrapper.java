package de.hhu.bsinfo.observatory.jverbs;

import com.ibm.net.rdma.jverbs.cm.ConnectionId;
import com.ibm.net.rdma.jverbs.verbs.CompletionChannel;
import com.ibm.net.rdma.jverbs.verbs.CompletionQueue;
import com.ibm.net.rdma.jverbs.verbs.MemoryRegion;
import com.ibm.net.rdma.jverbs.verbs.PollCQMethod;
import com.ibm.net.rdma.jverbs.verbs.PostReceiveMethod;
import com.ibm.net.rdma.jverbs.verbs.PostSendMethod;
import com.ibm.net.rdma.jverbs.verbs.ProtectionDomain;
import com.ibm.net.rdma.jverbs.verbs.QueuePair;
import com.ibm.net.rdma.jverbs.verbs.QueuePair.Type;
import com.ibm.net.rdma.jverbs.verbs.QueuePairInitAttribute;
import com.ibm.net.rdma.jverbs.verbs.ReceiveWorkRequest;
import com.ibm.net.rdma.jverbs.verbs.ScatterGatherElement;
import com.ibm.net.rdma.jverbs.verbs.SendWorkRequest;
import com.ibm.net.rdma.jverbs.verbs.VerbsContext;
import com.ibm.net.rdma.jverbs.verbs.WorkCompletion;
import com.ibm.net.rdma.jverbs.verbs.WorkCompletion.Status;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps some of the Disni-functions and -classes.
 *
 * @author Fabian Ruhland, HHU
 * @date 2019
 */
class VerbsWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbsWrapper.class);

    /**
     * Connection id for the connection to the remote host.
     *
     * Required to get the verbs context.
     */
    private ConnectionId connectionId;

    /**
     * JVerbs context.
     */
    private VerbsContext context;

    /**
     * The protection domain, in which all infiniband resources will be registered.
     */
    private ProtectionDomain protDom;

    /**
     * Send Completion channel (Unused, but required for the creation of a completion queue).
     */
    private CompletionChannel sendCompChannel;

    /**
     * Receive Completion channel (Unused, but required for the creation of a completion queue).
     */
    private CompletionChannel recvCompChannel;

    /**
     * The send completion queue.
     */
    private CompletionQueue sendCompQueue;

    /**
     * The receive completion queue.
     */
    private CompletionQueue recvCompQueue;

    /**
     * The queue pair.
     */
    private QueuePair queuePair;

    /**
     * An array, which holds all work completions for the send completion queue.
     */
    private WorkCompletion[] sendWorkComps;

    /**
     * An array, which holds all work completions for the receive completion queue.
     */
    private WorkCompletion[] receiveWorkComps;

    /**
     * Stateful Verbs Method for posting Send Work Requests.
     */
    private PostSendMethod postSendMethod;

    /**
     * Stateful Verbs Method for posting Receive Work Requests.
     */
    private PostReceiveMethod postReceiveMethod;

    /**
     * Amount of last posted send work requests.
     */
    private int lastSend;

    /**
     * Amount of last posted receive work requests.
     */
    private int lastReceive;

    /**
     * Stateful Verbs Method for polling the send completion queue.
     */
    private PollCQMethod sendCqMethod;

    /**
     * Stateful Verbs Method for polling the receive completion queue.
     */
    private PollCQMethod recvCqMethod;

    /**
     * Reusable send work requests.
     */
    private SendWorkRequest[] sendWrs;

    /**
     * Reusable receive work requests.
     */
    private ReceiveWorkRequest[] recvWrs;

    /**
     * List of send work requests.
     */
    private LinkedList<SendWorkRequest> sendWrList;

    /**
     * List of receive work requests.
     */
    private LinkedList<ReceiveWorkRequest> recvWrList;

    /**
     * Constructor.
     *
     * @param id The connection id, from which to get the context
     * @param queueSize Desired size of the queue pair and completion queue
     */
    VerbsWrapper(ConnectionId id, int queueSize) throws IOException {
        // Get context
        connectionId = id;
        context = id.getVerbsContext();

        // Create protection domain
        protDom = context.allocProtectionDomain();

        // Create send completion queue
        sendCompChannel = context.createCompletionChannel();
        sendCompQueue = context.createCompletionQueue(sendCompChannel, queueSize, 0);

        // Create receive completion queue
        recvCompChannel = context.createCompletionChannel();
        recvCompQueue = context.createCompletionQueue(recvCompChannel, queueSize, 0);

        // Create queue pair
        QueuePairInitAttribute attr = new QueuePairInitAttribute();
        attr.getCap().setMaxReceiveSge(1);
        attr.getCap().setMaxReceiveWorkRequest(queueSize);
        attr.getCap().setMaxSendSge(1);
        attr.getCap().setMaxSendWorkRequest(queueSize);
        attr.setQueuePairType(Type.IBV_QPT_RC);
        attr.setSendCompletionQueue(sendCompQueue);
        attr.setReceiveCompletionQueue(recvCompQueue);

        queuePair = id.createQueuePair(protDom, attr);

        // Create work completion lists
        sendWorkComps = new WorkCompletion[queueSize];
        receiveWorkComps = new WorkCompletion[queueSize];

        for(int i = 0; i < this.sendWorkComps.length; i++) {
            sendWorkComps[i] = new WorkCompletion();
        }

        for(int i = 0; i < this.receiveWorkComps.length; i++) {
            receiveWorkComps[i] = new WorkCompletion();
        }

        lastSend = -1;
        lastReceive = -1;

        sendWrs = new SendWorkRequest[queueSize];
        recvWrs = new ReceiveWorkRequest[queueSize];

        for(int i = 0; i < this.sendWrs.length; i++) {
            sendWrs[i] = new SendWorkRequest();
        }

        for(int i = 0; i < this.sendWrs.length; i++) {
            recvWrs[i] = new ReceiveWorkRequest();
        }

        sendWrList = new LinkedList<>();
        recvWrList = new LinkedList<>();

        sendCqMethod = sendCompQueue.pollCQ(sendWorkComps, queueSize);
        recvCqMethod = recvCompQueue.pollCQ(receiveWorkComps, queueSize);
    }

    /**
     * Get a stateful verbs call, that posts a list of work requests to the send queue.
     *
     * @param sendWrs The list of work requests to be posted
     *
     * @return The stateful verbs call
     */
    private PostSendMethod getPostSendMethod(LinkedList<SendWorkRequest> sendWrs) throws IOException {
        if(lastSend != sendWrs.size()) {
            lastSend = sendWrs.size();

            if(postSendMethod != null) {
                postSendMethod.free();
            }

            postSendMethod = queuePair.preparePostSend(sendWrs);
        }

        if(!postSendMethod.isValid()) {
            throw new IOException("PostSendMethod invalid!");
        }

        return postSendMethod;
    }

    /**
     * Get a stateful verbs call, that posts a list of work requests to the recv queue.
     *
     * @param recvWrs The list of work requests to be posted
     *
     * @return The stateful verbs call
     */
    private PostReceiveMethod getPostReceiveMethod(LinkedList<ReceiveWorkRequest> recvWrs) throws IOException {
        if(lastReceive != recvWrs.size()) {
            lastReceive = recvWrs.size();

            if(postReceiveMethod != null) {
                postReceiveMethod.free();
            }

            postReceiveMethod = queuePair.preparePostReceive(recvWrs);
        }

        if(!postReceiveMethod.isValid()) {
            throw new IOException("PostSendMethod invalid!");
        }

        return postReceiveMethod;
    }

    /**
     * Get the work completion array.
     *
     * Can be used to retrieve the work completions after the completion queue has been polled.
     *
     * @param mode Whether to get the send or receive work completions
     *
     * @return The work completions
     */
    private WorkCompletion[] getWorkCompletions(Mode mode) {
        return mode == Mode.SEND ? sendWorkComps : receiveWorkComps;
    }

    /**
     * Register a buffer as memory region.
     *
     * @param buffer The buffer to be registered
     *
     * @return The registered memory region
     */
    MemoryRegion registerMemoryRegion(ByteBuffer buffer) throws IOException {
        int accessFlags = MemoryRegion.IBV_ACCESS_LOCAL_WRITE |
                MemoryRegion.IBV_ACCESS_REMOTE_WRITE |
                MemoryRegion.IBV_ACCESS_REMOTE_READ;

        return protDom.registerMemoryRegion(buffer, accessFlags).execute().free().getMemoryRegion();
    }

    /**
     * Deregister a memory region.
     *
     * @param memoryRegion The memory region to be deregistered
     *
     * @return The registered memory region
     */
    void deregisterMemoryRegion(MemoryRegion memoryRegion) throws IOException {
        protDom.deregisterMemoryRegion(memoryRegion).execute().free();
    }

    /**
     * Poll the completion queue a single time and get the amount of polled work completions.
     *
     * @param mode Whether to poll the send or the receive completion queue
     *
     * @return The amount of polled work completions
     */
    int pollCompletionQueue(Mode mode) throws IOException {
        PollCQMethod pollMethod = mode == Mode.SEND ? sendCqMethod : recvCqMethod;

        if(!pollMethod.isValid()) {
            throw new IOException("PollCqMethod invalid!");
        }

        pollMethod.execute();

        int polled = pollMethod.getPolls();

        WorkCompletion[] workComps = getWorkCompletions(mode);

        for(int i = 0; i < polled; i++) {
            if(workComps[i].getStatus() != Status.IBV_WC_SUCCESS) {
                throw new IOException("Work completion failed with status [" + workComps[i].getStatus() + "]");
            }
        }

        return polled;
    }

    void sendMessages(int amount, LinkedList<ScatterGatherElement> scatterGatherElements) throws IOException {
        if(amount <= 0) {
            return;
        }

        sendWrList.clear();

        for(int i = 0; i < amount; i++) {
            sendWrs[i].setWorkRequestId(1);
            sendWrs[i].setSgeList(scatterGatherElements);
            sendWrs[i].setOpcode(SendWorkRequest.Opcode.IBV_WR_SEND);
            sendWrs[i].setSendFlags(SendWorkRequest.IBV_SEND_SIGNALED);

            sendWrList.add(sendWrs[i]);
        }

        getPostSendMethod(sendWrList).execute();
    }

    void receiveMessages(int amount, LinkedList<ScatterGatherElement> scatterGatherElements) throws IOException {
        if(amount <= 0) {
            return;
        }

        recvWrList.clear();

        for(int i = 0; i < amount; i++) {
            recvWrs[i].setWorkRequestId(1);
            recvWrs[i].setSgeList(scatterGatherElements);

            recvWrList.add(recvWrs[i]);
        }

        getPostReceiveMethod(recvWrList).execute();
    }

    void executeRdmaOperations(int amount, LinkedList<ScatterGatherElement> scatterGatherElements, RdmaMode mode, MemoryRegionInformation remoteInfo) throws IOException {
        if(amount <= 0) {
            return;
        }

        sendWrList.clear();

        for(int i = 0; i < amount; i++) {
            sendWrs[i].setWorkRequestId(1);
            sendWrs[i].setSgeList(scatterGatherElements);
            sendWrs[i].setOpcode(mode == RdmaMode.WRITE ? SendWorkRequest.Opcode.IBV_WR_RDMA_WRITE : SendWorkRequest.Opcode.IBV_WR_RDMA_READ);
            sendWrs[i].setSendFlags(SendWorkRequest.IBV_SEND_SIGNALED);

            sendWrs[i].getRdma().setRemoteAddress(remoteInfo.getAddress());
            sendWrs[i].getRdma().setRemoteKey(remoteInfo.getRemoteKey());

            sendWrList.add(sendWrs[i]);
        }

        getPostSendMethod(sendWrList).execute();
    }

    /**
     * Destroy all JVerbs resources.
     */
    void destroy() throws Exception {
        if(postSendMethod != null) postSendMethod.free();
        if(postReceiveMethod != null) postReceiveMethod.free();
        sendCqMethod.free();
        recvCqMethod.free();

        context.destroyCompletionQueue(sendCompQueue);
        context.destroyCompletionQueue(recvCompQueue);
        context.destroyCompletionChannel(sendCompChannel);
        context.destroyCompletionChannel(recvCompChannel);
        connectionId.destroyQueuePair();
        context.deallocProtectionDomain(protDom);
    }
}
