// blah explicitly defining a state machine will happen later

//package org.webpieces.httpclient.impl;
//
//import com.webpieces.http2parser.api.dto.Http2Frame;
//import com.webpieces.http2parser.api.dto.Http2FrameType;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.webpieces.httpclient.impl.Http2StateMachine.Side.RCV;
//import static org.webpieces.httpclient.impl.Http2StateMachine.Side.SEND;
//
//public class Http2StateMachine {
//    enum Side {
//        SEND, RCV
//    }
//
//    private class TransitionTable {
//        Map<Side, Map<Http2FrameType, Stream.StreamStatus>> transitions;
//
//
//        TransitionTable(Map<Http2FrameType, Stream.StreamStatus> sendTransitions,
//                        Map<Http2FrameType, Stream.StreamStatus> rcvTransitions) {
//            transitions = new HashMap<>();
//            transitions.put(SEND, sendTransitions);
//            transitions.put(RCV, rcvTransitions);
//        }
//
//        public Map<Side, Map<Http2FrameType, Stream.StreamStatus>> getTransitions() {
//            return transitions;
//        }
//    }
//
//    private Map<Stream.StreamStatus,
//            TransitionTable> transitionTables = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> idleTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> reservedLocalTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> reservedRemoteTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> openTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> halfClosedLocalTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> halfClosedRemoteTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> closedTransitionsSend = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> idleTransitionsRcv = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> reservedLocalTransitionsRcv = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> reservedRemoteTransitionsRcv = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> openTransitionsRcv = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> halfClosedLocalTransitionsRcv = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> halfClosedRemoteTransitionsRcv = new HashMap<>();
//    private Map<Http2FrameType, Stream.StreamStatus> closedTransitionsRcv = new HashMap<>();
//
//    public Http2StateMachine() {
//        idleTransitionsSend.put(Http2FrameType.PUSH_PROMISE, Stream.StreamStatus.RESERVED_LOCAL);
//        idleTransitionsRcv.put(Http2FrameType.PUSH_PROMISE, Stream.StreamStatus.RESERVED_REMOTE);
//        idleTransitionsRcv.put(Http2FrameType.HEADERS, Stream.StreamStatus.OPEN);
//        idleTransitionsSend.put(Http2FrameType.HEADERS, Stream.StreamStatus.OPEN);
//
//        reservedLocalTransitionsRcv
//        transitionTables.put(Stream.StreamStatus.IDLE, new TransitionTable(idleTransitionsSend, idleTransitionsRcv));
//        transitionTables.put(Stream.StreamStatus.RESERVED_LOCAL, new TransitionTable(reservedLocalTransitionsSend, reservedLocalTransitionsRcv));
//        transitionTables.put(Stream.StreamStatus.RESERVED_REMOTE, new TransitionTable(reservedRemoteTransitionsSend, reservedRemoteTransitionsRcv));
//        transitionTables.put(Stream.StreamStatus.OPEN, new TransitionTable(openTransitionsSend, openTransitionsRcv));
//        transitionTables.put(Stream.StreamStatus.HALF_CLOSED_LOCAL, new TransitionTable(halfClosedLocalTransitionsSend, halfClosedLocalTransitionsRcv));
//        transitionTables.put(Stream.StreamStatus.HALF_CLOSED_REMOTE, new TransitionTable(halfClosedRemoteTransitionsSend, halfClosedRemoteTransitionsRcv));
//        transitionTables.put(Stream.StreamStatus.CLOSED, new TransitionTable(closedTransitionsSend, closedTransitionsRcv);
//    }
//
//    public Stream.StreamStatus getNewStatus(Side side, Stream.StreamStatus startingStatus, Http2FrameType frameType) {
//        return transitionTables.get(startingStatus).getTransitions().get(side).get(frameType);
//    }
//}
