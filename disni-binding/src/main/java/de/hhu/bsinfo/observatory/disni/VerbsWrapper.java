package de.hhu.bsinfo.observatory.disni;

import com.ibm.disni.verbs.*;

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
    private RdmaCmId connectionId;

    /**
     * JVerbs context.
     */
    private IbvContext context;

    /**
     * The protection domain, in which all InfiniBand resources will be registered.
     */
    private IbvPd protDom;

    /**
     * Send Completion channel (Unused, but required for the creation of a completion queue).
     */
    private IbvCompChannel sendCompChannel;

    /**
     * Receive Completion channel (Unused, but required for the creation of a completion queue).
     */
    private IbvCompChannel recvCompChannel;

    /**
     * The send completion queue.
     */
    private IbvCQ sendCompQueue;

    /**
     * The receive completion queue.
     */
    private IbvCQ recvCompQueue;

    /**
     * The queue pair.
     */
    private IbvQP queuePair;

    /**
     * An array, which holds all work completions for the send completion queue.
     */
    private IbvWC[] sendWorkComps;

    /**
     * An array, which holds all work completions for the receive completion queue.
     */
    private IbvWC[] receiveWorkComps;

    /**
     * Stateful Verbs Method for posting Send Work Requests.
     */
    private SVCPostSend postSendMethod;

    /**
     * Stateful Verbs Method for posting Receive Work Requests.
     */
    private SVCPostRecv postReceiveMethod;

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
    private SVCPollCq sendCqMethod;

    /**
     * Stateful Verbs Method for polling the receive completion queue.
     */
    private SVCPollCq recvCqMethod;

    /**
     * Reusable send work requests.
     */
    private IbvSendWR[] sendWrs;

    /**
     * Reusable receive work requests.
     */
    private IbvRecvWR[] recvWrs;

    /**
     * List of send work requests.
     */
    private LinkedList<IbvSendWR> sendWrList;

    /**
     * List of receive work requests.
     */
    private LinkedList<IbvRecvWR> recvWrList;

    /**
     * Constructor.
     *
     * @param id The connection id, from which to get the context
     * @param queueSize Desired size of the queue pair and completion queue
     */
    VerbsWrapper(RdmaCmId id, int queueSize) throws IOException {
        // Get context
        connectionId = id;
        context = id.getVerbs();

        // Create protection domain
        protDom = context.allocPd();

        // Create send completion queue
        sendCompChannel = context.createCompChannel();
        sendCompQueue = context.createCQ(sendCompChannel, queueSize, 0);

        // Create receive completion queue
        recvCompChannel = context.createCompChannel();
        recvCompQueue = context.createCQ(recvCompChannel, queueSize, 0);

        // Create queue pair
        IbvQPInitAttr attr = new IbvQPInitAttr();
        attr.cap().setMax_recv_sge(1);
        attr.cap().setMax_recv_wr(queueSize);
        attr.cap().setMax_send_sge(1);
        attr.cap().setMax_send_wr(queueSize);
        attr.setQp_type(IbvQP.IBV_QPT_RC);
        attr.setSend_cq(sendCompQueue);
        attr.setRecv_cq(recvCompQueue);

        queuePair = id.createQP(protDom, attr);

        // Create work completion lists
        sendWorkComps = new IbvWC[queueSize];
        receiveWorkComps = new IbvWC[queueSize];

        for(int i = 0; i < this.sendWorkComps.length; i++) {
            sendWorkComps[i] = new IbvWC();
        }

        for(int i = 0; i < this.receiveWorkComps.length; i++) {
            receiveWorkComps[i] = new IbvWC();
        }

        lastSend = -1;
        lastReceive = -1;

        sendWrs = new IbvSendWR[queueSize];
        recvWrs = new IbvRecvWR[queueSize];

        for(int i = 0; i < this.sendWrs.length; i++) {
            sendWrs[i] = new IbvSendWR();
        }

        for(int i = 0; i < this.sendWrs.length; i++) {
            recvWrs[i] = new IbvRecvWR();
        }

        sendWrList = new LinkedList<>();
        recvWrList = new LinkedList<>();

        sendCqMethod = sendCompQueue.poll(sendWorkComps, queueSize);
        recvCqMethod = recvCompQueue.poll(receiveWorkComps, queueSize);
    }

    /**
     * Get a stateful verbs call, that posts a list of work requests to the send queue.
     *
     * @param sendWrs The list of work requests to be posted
     *
     * @return The stateful verbs call
     */
    private SVCPostSend getPostSendMethod(LinkedList<IbvSendWR> sendWrs) throws IOException {
        if(lastSend != sendWrs.size()) {
            lastSend = sendWrs.size();

            if(postSendMethod != null) {
                postSendMethod.free();
            }

            postSendMethod = queuePair.postSend(sendWrs, null);
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
    private SVCPostRecv getPostReceiveMethod(LinkedList<IbvRecvWR> recvWrs) throws IOException {
        if(lastReceive != recvWrs.size()) {
            lastReceive = recvWrs.size();

            if(postReceiveMethod != null) {
                postReceiveMethod.free();
            }

            postReceiveMethod = queuePair.postRecv(recvWrs, null);
        }

        if(!postReceiveMethod.isValid()) {
            throw new IOException("PostReceiveMethod invalid!");
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
    private IbvWC[] getWorkCompletions(Mode mode) {
        return mode == Mode.SEND ? sendWorkComps : receiveWorkComps;
    }

    /**
     * Register a buffer as memory region.
     *
     * @param buffer The buffer to be registered
     *
     * @return The registered memory region
     */
    IbvMr registerMemoryRegion(ByteBuffer buffer) throws IOException {
        int accessFlags = IbvMr.IBV_ACCESS_LOCAL_WRITE |
                IbvMr.IBV_ACCESS_REMOTE_WRITE |
                IbvMr.IBV_ACCESS_REMOTE_READ;

        return protDom.regMr(buffer, accessFlags).execute().free().getMr();
    }

    /**
     * Poll the completion queue a single time and get the amount of polled work completions.
     *
     * @param mode Whether to poll the send or the receive completion queue
     *
     * @return The amount of polled work completions
     */
    int pollCompletionQueue(Mode mode) throws IOException {
        SVCPollCq pollMethod = mode == Mode.SEND ? sendCqMethod : recvCqMethod;

        if(!pollMethod.isValid()) {
            throw new IOException("PollCqMethod invalid!");
        }

        pollMethod.execute();

        int polled = pollMethod.getPolls();

        IbvWC[] workComps = getWorkCompletions(mode);

        for(int i = 0; i < polled; i++) {
            if(workComps[i].getStatus() != IbvWC.IbvWcStatus.IBV_WC_SUCCESS.ordinal()) {
                throw new IOException("Work completion failed with status [" + workComps[i].getStatus() + "]");
            }
        }

        return polled;
    }

    void sendMessages(int amount, LinkedList<IbvSge> scatterGatherElements) throws IOException {
        if(amount <= 0) {
            return;
        }

        sendWrList.clear();

        for(int i = 0; i < amount; i++) {
            sendWrs[i].setWr_id(1);
            sendWrs[i].setSg_list(scatterGatherElements);
            sendWrs[i].setOpcode(IbvSendWR.IBV_WR_SEND);
            sendWrs[i].setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);

            sendWrList.add(sendWrs[i]);
        }

        getPostSendMethod(sendWrList).execute();
    }

    void receiveMessages(int amount, LinkedList<IbvSge> scatterGatherElements) throws IOException {
        if(amount <= 0) {
            return;
        }

        recvWrList.clear();

        for(int i = 0; i < amount; i++) {
            recvWrs[i].setWr_id(1);
            recvWrs[i].setSg_list(scatterGatherElements);

            recvWrList.add(recvWrs[i]);
        }

        getPostReceiveMethod(recvWrList).execute();
    }

    void executeRdmaOperations(int amount, LinkedList<IbvSge> scatterGatherElements, RdmaMode mode, MemoryRegionInformation remoteInfo) throws IOException {
        if(amount <= 0) {
            return;
        }

        sendWrList.clear();

        for(int i = 0; i < amount; i++) {
            sendWrs[i].setWr_id(1);
            sendWrs[i].setSg_list(scatterGatherElements);
            sendWrs[i].setOpcode(mode == RdmaMode.WRITE ? IbvSendWR.IBV_WR_RDMA_WRITE : IbvSendWR.IBV_WR_RDMA_READ);
            sendWrs[i].setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);

            sendWrs[i].getRdma().setRemote_addr(remoteInfo.getAddress());
            sendWrs[i].getRdma().setRkey(remoteInfo.getRemoteKey());

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

        sendCompQueue.destroyCQ();
        recvCompQueue.destroyCQ();
        sendCompChannel.destroyCompChannel();
        recvCompChannel.destroyCompChannel();
        connectionId.destroyQP();
        protDom.deallocPd();
    }
}
