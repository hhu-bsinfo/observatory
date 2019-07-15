package de.hhu.bsinfo.observatory.disni;

import com.ibm.disni.verbs.*;

import com.ibm.disni.verbs.IbvSendWR.IbvWrOcode;
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
     * Size of the queue pair and completion queue.
     */
    private int queueSize;

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
     * The protection domain, in which all infiniband resources will be registered.
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
     * An array, which holds all work completions.
     */
    private IbvWC[] workComps;

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
     * Used by Connection.pollCompletionQueue(JVerbsWrapper.CqType type) to determine whether
     * the send or the completion queue shall be polled.
     */
    enum CqType {
        SEND_CQ,    /**< Send completion queue */
        RECV_CQ     /**< Receive completion queue */
    }

    /**
     * Constructor.
     *
     * @param id The connection id, from which to get the context
     * @param queueSize Desired size of the queue pair and completion queue
     */
    VerbsWrapper(RdmaCmId id, int queueSize) throws IOException {
        this.queueSize = queueSize;

        // Get context
        this.connectionId = id;
        this.context = id.getVerbs();

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

        // Create work completion list
        workComps = new IbvWC[queueSize];

        for(int i = 0; i < this.workComps.length; i++) {
            this.workComps[i] = new IbvWC();
        }

        lastSend = -1;
        lastReceive = -1;

        this.sendWrs = new IbvSendWR[queueSize];
        this.recvWrs = new IbvRecvWR[queueSize];

        for(int i = 0; i < this.sendWrs.length; i++) {
            this.sendWrs[i] = new IbvSendWR();
        }

        for(int i = 0; i < this.sendWrs.length; i++) {
            this.recvWrs[i] = new IbvRecvWR();
        }

        this.sendWrList = new LinkedList<>();
        this.recvWrList = new LinkedList<>();
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
            LOGGER.error("PostSendMethod invalid!");
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
            LOGGER.error("PostReceiveMethod invalid!");
        }

        return postReceiveMethod;
    }

    /**
     * Get a stateful verbs call, that can be used to poll the completion queue.
     *
     * @param type Whether to poll the send or the receive completion queue
     *
     * @return The stateful verbs call
     */
    private SVCPollCq getPollCqMethod(CqType type) throws IOException {
        SVCPollCq pollCqMethod;

        if(type == CqType.SEND_CQ) {
            if(sendCqMethod == null) {
                sendCqMethod = sendCompQueue.poll(workComps, queueSize);
            }

            pollCqMethod = sendCqMethod;
        } else {
            if(recvCqMethod == null) {
                recvCqMethod = recvCompQueue.poll(workComps, queueSize);
            }

            pollCqMethod = recvCqMethod;
        }

        if (!pollCqMethod.isValid()) {
            LOGGER.error("PollCqMethod invalid!");
        }

        return pollCqMethod;
    }

    /**
     * Get the work completion array.
     *
     * Can be used to retrieve the work completion after the completion queue has been polled.
     *
     * @return The work completions
     */
    private IbvWC[] getWorkCompletions() {
        return workComps;
    }

    /**
     * Register a buffer as memory region.
     *
     * @param buffer The buffer to be registered
     *
     * @return The registered memory region
     */
    IbvMr registerMemoryRegion(ByteBuffer buffer) throws IOException {
        int accessFlags = IbvMr.IBV_ACCESS_LOCAL_WRITE  |
            IbvMr.IBV_ACCESS_REMOTE_WRITE |
            IbvMr.IBV_ACCESS_REMOTE_READ;

        return protDom.regMr(buffer, accessFlags).execute().free().getMr();
    }

    /**
     * Poll the completion queue a single time and get the amount of polled work completions.
     *
     * @param type Whether to poll the send or the receive completion queue
     *
     * @return The amount of polled work completions, or -1 if an error has occurred
     */
    int pollCompletionQueue(VerbsWrapper.CqType type) throws IOException {
        SVCPollCq pollMethod = getPollCqMethod(type);

        if(!pollMethod.isValid()) {
            return -1;
        }

        pollMethod.execute();

        int polled = pollMethod.getPolls();

        IbvWC[] workComps = getWorkCompletions();

        for(int i = 0; i < polled; i++) {
            if(workComps[i].getStatus() != IbvWC.IbvWcStatus.IBV_WC_SUCCESS.ordinal()) {
                LOGGER.error("Work completion failed with status [{}]", workComps[i].getStatus());
                return -1;
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

    void resetStatefulCalls() {
        lastSend = -1;
        lastReceive = -1;

        if(postSendMethod != null) {
            postSendMethod.free();
            postSendMethod = null;
        }

        if(postReceiveMethod != null) {
            postReceiveMethod.free();
            postReceiveMethod = null;
        }
    }

    /**
     * Destroy all JVerbs resources.
     */
    void destroy() throws Exception {
        postSendMethod.free();
        postSendMethod.free();
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
