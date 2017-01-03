package org.webpieces.httpcommon.impl;

import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.lib.SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS;
import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.SERVER;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.CLOSED;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.IDLE;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.api.exceptions.GoAwayError;
import org.webpieces.httpcommon.api.exceptions.Http2Error;
import org.webpieces.httpcommon.api.exceptions.InternalError;
import org.webpieces.httpcommon.api.exceptions.RstStreamError;
import org.webpieces.httpcommon.impl.Http2EngineImpl.PendingData;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

class Http2DataListener implements DataListener {
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    /**
	 * 
	 */
	private final Http2EngineImpl http2EngineImpl;
	private DataWrapper oldData;
    private AtomicBoolean gotPreface = new AtomicBoolean(false);
    
	/**
	 * @param http2EngineImpl
	 */
	Http2DataListener(Http2EngineImpl http2EngineImpl) {
		this.http2EngineImpl = http2EngineImpl;
		oldData = this.http2EngineImpl.http2Parser.prepareToParse();
	}

    private void handleData(DataFrame frame, Stream stream) {
        // Only allowable if stream is open or half closed local
        switch(stream.getStatus()) {
            case OPEN:
            case HALF_CLOSED_LOCAL:
                int payloadLength = this.http2EngineImpl.http2Parser.getFrameLength(frame);
                this.http2EngineImpl.decrementIncomingWindow(frame.getStreamId(), payloadLength);
                stream.checkAgainstContentLength(frame.getData().getReadableSize(), frame.isEndStream());

                this.http2EngineImpl.sideSpecificHandleData(frame, payloadLength, stream);

                if(frame.isEndStream())
                    this.http2EngineImpl.receivedEndStream(stream);
                break;
            case HALF_CLOSED_REMOTE:
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, Http2EngineImpl.wrapperGen.emptyWrapper());
            case CLOSED:
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, Http2EngineImpl.wrapperGen.emptyWrapper());
            case IDLE:
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
            default:
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
        }
    }

    private void handleHeaders(HeadersFrame frame, Stream stream) {
        boolean isTrailer = false;
        switch (stream.getStatus()) {
            case IDLE:
                long currentlyOpenStreams = this.http2EngineImpl.countOpenRemoteOriginatedStreams();
                Http2EngineImpl.log.info("got headers with currently open streams: " + currentlyOpenStreams);
                if(this.http2EngineImpl.localSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                        currentlyOpenStreams >= this.http2EngineImpl.localSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                    throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, stream.getStreamId());
                }
                stream.setStatus(Stream.StreamStatus.OPEN);
                break;
            case HALF_CLOSED_LOCAL:
                // No status change in this case
                break;
            case RESERVED_REMOTE:
                stream.setStatus(Stream.StreamStatus.HALF_CLOSED_LOCAL);
                break;
            case OPEN:
                if(!stream.isTrailerEnabled()) {
                    throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, Http2EngineImpl.wrapperGen.emptyWrapper());
                } else {
                    isTrailer = true;
                }
                break;
            default: // HALF_CLOSED_REMOTE, or CLOSED
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, Http2EngineImpl.wrapperGen.emptyWrapper());
        }

        if(frame.isPriority()) {
            stream.setPriorityDetails(frame.getPriorityDetails());
        }

        if(frame.isEndHeaders()) {
            // the parser has already accumulated the headers in the frame for us.

            boolean isComplete = frame.isEndStream();

            if(isTrailer) {
                // Make sure that the headers match what we are expecting.
                List<String> allowedTrailerHeaders = stream.getTrailerHeaders();
                for(Http2Header header: frame.getHeaderList()) {
                    if(!allowedTrailerHeaders.contains(header.getName()))
                        throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
                }
            }

            // if we have no headers must be a compression error?
            if(frame.getHeaderList().isEmpty()) {
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.COMPRESSION_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
            }
            this.http2EngineImpl.sideSpecificHandleHeaders(frame, isTrailer, stream);

            if (isComplete)
                this.http2EngineImpl.receivedEndStream(stream);

        }
        else {
            throw new InternalError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2EngineImpl.wrapperGen.emptyWrapper());
        }
    }


    private void handlePriority(PriorityFrame frame, Stream stream) {
        // Can be received in any state. We aren't doing anything with this right now.
        stream.setPriorityDetails(frame.getPriorityDetails());
    }

    private void handleRstStream(RstStreamFrame frame, Stream stream) {
        switch(stream.getStatus()) {
            case OPEN:
            case HALF_CLOSED_REMOTE:
            case HALF_CLOSED_LOCAL:
            case RESERVED_LOCAL:
            case RESERVED_REMOTE:
            case CLOSED:
                this.http2EngineImpl.sideSpecificHandleRstStream(frame, stream);
                stream.setStatus(CLOSED);
                break;
            default:
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
        }
    }

    private void handlePushPromise(PushPromiseFrame frame, Stream stream) {
        if(this.http2EngineImpl.side == SERVER) {
            // Can't get pushpromise in the server
            throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
        }

        // Can get this on any stream id, creates a new stream
        if(frame.isEndHeaders()) {
            long currentlyOpenStreams = this.http2EngineImpl.countOpenRemoteOriginatedStreams();
            Http2EngineImpl.log.info("got push promise with currently open streams: " + currentlyOpenStreams);
            if(this.http2EngineImpl.localSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                    currentlyOpenStreams >= this.http2EngineImpl.localSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, frame.getPromisedStreamId());
            }
            int newStreamId = frame.getPromisedStreamId();
            if(newStreamId <= this.http2EngineImpl.lastIncomingStreamId.get()) {
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
            }
            this.http2EngineImpl.lastIncomingStreamId.set(newStreamId);

            Stream promisedStream = new Stream();
            this.http2EngineImpl.initializeFlowControl(newStreamId);
            promisedStream.setStreamId(newStreamId);

            // TODO: make sure streamid is valid
            // TODO: close all lower numbered even IDLE streams
            this.http2EngineImpl.activeStreams.put(newStreamId, promisedStream);

            // Uses the same listener as the stream it came in on
            promisedStream.setResponseListener(stream.getResponseListener());
            
            HttpRequest request = this.http2EngineImpl.requestFromHeaders(new LinkedList<>(frame.getHeaderList()), promisedStream);
            promisedStream.setRequest(request);
            promisedStream.setStatus(Stream.StreamStatus.RESERVED_REMOTE);
        } else {
            throw new InternalError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2EngineImpl.wrapperGen.emptyWrapper());
        }
    }

    private void handleWindowUpdate(WindowUpdateFrame frame, Stream stream) {
        if(frame.getWindowSizeIncrement() == 0) {
            throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
        }

        this.http2EngineImpl.incrementOutgoingWindow(frame.getStreamId(), frame.getWindowSizeIncrement());

        // clear all queues if the connection-level stream
        if(frame.getStreamId() == 0x0) {
            for (Map.Entry<Integer, ConcurrentLinkedDeque<PendingData>> entry : this.http2EngineImpl.outgoingDataQueue.entrySet()) {
                if (!entry.getValue().isEmpty())
                    this.http2EngineImpl.clearQueue(entry.getKey());
            }
        }
        else {
            if(stream.getStatus() == IDLE) {
                throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
            }
            if(this.http2EngineImpl.outgoingDataQueue.containsKey(frame.getStreamId())) {
                this.http2EngineImpl.clearQueue(frame.getStreamId());
            }
        }
    }

    private void handleSettings(SettingsFrame frame) {
        if(frame.isAck()) {
            // we received an ack, so the settings we sent have been accepted.
            for(Map.Entry<SettingsParameter, Long> entry: this.http2EngineImpl.localRequestedSettings.entrySet()) {
                this.http2EngineImpl.localSettings.put(entry.getKey(), entry.getValue());
            }
        } else {
            this.http2EngineImpl.setRemoteSettings(frame, true);
        }
    }

    // TODO: actually deal with this goaway stuff where necessary
    private void handleGoAway(GoAwayFrame frame) {
        this.http2EngineImpl.remoteGoneAway = true;
        this.http2EngineImpl.goneAwayLastStreamId = frame.getLastStreamId();
        this.http2EngineImpl.goneAwayErrorCode = frame.getErrorCode();
        this.http2EngineImpl.additionalDebugData = frame.getDebugData();
        farEndClosed(this.http2EngineImpl.channel);
    }

    private void handlePing(PingFrame frame) {
        if(!frame.isPingResponse()) {
            // Send the same frame back, setting ping response
            frame.setIsPingResponse(true);
            Http2EngineImpl.log.info("sending ping response: " + frame);
            this.http2EngineImpl.channel.write(ByteBuffer.wrap(this.http2EngineImpl.http2Parser.marshal(frame).createByteArray()));
        } else {
            // measure latency from the ping that was sent. The opaqueData we sent is
            // System.nanoTime() so we just measure the difference
            long latency = System.nanoTime() - frame.getOpaqueData();
            Http2EngineImpl.log.info("Ping: {} ms", latency * 1e-6);
        }
    }

    private void handleFrame(Http2Frame frame) {
        if(frame.getFrameType() != SETTINGS && !this.http2EngineImpl.gotSettings.get()) {
            preconditions();
        }

        // Transition the stream state
        if(frame.getStreamId() != 0x0) {
            Stream stream = this.http2EngineImpl.activeStreams.get(frame.getStreamId());

            // If the stream doesn't exist, create it, if server and if streamid is odd.
            if (stream == null) {
                if (this.http2EngineImpl.side == SERVER) {
                    int streamId = frame.getStreamId();
                    if(streamId <= this.http2EngineImpl.lastIncomingStreamId.get() || frame.getStreamId() % 2 != 1) {
                        throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
                    }
                    this.http2EngineImpl.lastIncomingStreamId.set(streamId);
                    stream = new Stream();
                    stream.setStreamId(streamId);
                    this.http2EngineImpl.initializeFlowControl(stream.getStreamId());
                    this.http2EngineImpl.activeStreams.put(stream.getStreamId(), stream);
                } else {
                    throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
                }
            }

            switch (frame.getFrameType()) {
                case DATA:
                    handleData((DataFrame) frame, stream);
                    break;
                case HEADERS:
                    handleHeaders((HeadersFrame) frame, stream);
                    break;
                case PRIORITY:
                    handlePriority((PriorityFrame) frame, stream);
                    break;
                case RST_STREAM:
                    handleRstStream((RstStreamFrame) frame, stream);
                    break;
                case PUSH_PROMISE:
                    handlePushPromise((PushPromiseFrame) frame, stream);
                    break;
                case WINDOW_UPDATE:
                    handleWindowUpdate((WindowUpdateFrame) frame, stream);
                    break;
                case CONTINUATION:
                    throw new InternalError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2EngineImpl.wrapperGen.emptyWrapper());
                default:
                    throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), frame.getStreamId(), Http2ErrorCode.PROTOCOL_ERROR,
                            Http2EngineImpl.wrapperGen.emptyWrapper());
            }
        } else {
            switch (frame.getFrameType()) {
                case WINDOW_UPDATE:
                    handleWindowUpdate((WindowUpdateFrame) frame, null);
                    break;
                case SETTINGS:
                    handleSettings((SettingsFrame) frame);
                    break;
                case GOAWAY:
                    handleGoAway((GoAwayFrame) frame);
                    break;
                case PING:
                    handlePing((PingFrame) frame);
                    break;
                default:
                    throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR,
                            Http2EngineImpl.wrapperGen.emptyWrapper());
            }
        }
    }

	private void preconditions() {
		// If we haven't gotten the settings, let's wait a little bit because another thread
		// might have the settings frame and hasn't gotten around to processing it yet.
		try {
		    Http2EngineImpl.log.info("Waiting for settings frame to arrive");
		    if (!this.http2EngineImpl.settingsLatch.await(500, TimeUnit.MILLISECONDS))
		        throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
		} catch (InterruptedException e) {
		    Http2EngineImpl.log.error("Caught exception while waiting for settings frame", e);
		    throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
		}
	}

    @Override
    public void incomingData(Channel channel, ByteBuffer b) {
        DataWrapper newData = Http2EngineImpl.wrapperGen.wrapByteBuffer(b);
        try {
            // TODO: turn the preface into a frame type.
            // First check to make sure we got our preface
            if(this.http2EngineImpl.side == SERVER && !gotPreface.get()) {
                // check to make sure we got the preface.
                DataWrapper combined = Http2EngineImpl.wrapperGen.chainDataWrappers(oldData, newData);
                int prefaceLength = Http2EngineImpl.prefaceHexString.length()/2;
                if(combined.getReadableSize() >= prefaceLength) {
                    List<? extends DataWrapper> split = Http2EngineImpl.wrapperGen.split(combined, prefaceLength);
                    if(Arrays.equals(split.get(0).createByteArray(), (DatatypeConverter.parseHexBinary(Http2EngineImpl.prefaceHexString)))) {
                        gotPreface.set(true);
                        oldData = split.get(1);
                        Http2EngineImpl.log.info("got http2 preface");
                        this.http2EngineImpl.sendLocalRequestedSettings();
                    } else {
                        throw new GoAwayError(0, Http2ErrorCode.PROTOCOL_ERROR, Http2EngineImpl.wrapperGen.emptyWrapper());
                    }
                } else {
                    oldData = combined;
                }
            } else { // Either we got the preface or we don't need it
                try {
                    ParserResult parserResult = this.http2EngineImpl.http2Parser.parse(oldData, newData, this.http2EngineImpl.decoder, this.http2EngineImpl.localSettings.toNewer());

                    for (Http2Frame frame : parserResult.getParsedFrames()) {
                        Http2EngineImpl.log.info("got frame=" + frame);
                        handleFrame(frame);
                    }
                    oldData = parserResult.getMoreData();
                }
                catch (ParseException e) {
                    if(e.isConnectionLevel()) {
                        if(e.hasStream()) {
                            throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), e.getStreamId(), e.getErrorCode(), Http2EngineImpl.wrapperGen.emptyWrapper());
                        }
                        else {
                            throw new GoAwayError(this.http2EngineImpl.lastClosedRemoteOriginatedStream().orElse(0), e.getErrorCode(), Http2EngineImpl.wrapperGen.emptyWrapper());
                        }
                    } else {
                        throw new RstStreamError(e.getErrorCode(), e.getStreamId());
                    }
                }
            }
        } catch (Http2Error e) {
            Http2EngineImpl.log.error("sending error frames " + e.toFrames(), e);
            ByteBuffer dataPayload = translate(e.toFrames());
            channel.write(dataPayload);
            if(RstStreamError.class.isInstance(e)) {
                // Mark the stream closed
                Stream stream = this.http2EngineImpl.activeStreams.get(((RstStreamError) e).getStreamId());
                if(stream != null)
                    stream.setStatus(CLOSED);
            }
            if(GoAwayError.class.isInstance(e)) {
                // TODO: Shut this connection down properly.
                channel.close();
            }
        }
    }

    private ByteBuffer translate(List<Http2Frame> frames) {
    	DataWrapper allData = dataGen.emptyWrapper();
    	for(Http2Frame f : frames) {
    		DataWrapper data = this.http2EngineImpl.http2Parser.marshal(f);
    		allData = dataGen.chainDataWrappers(allData, data);
    	}
    	byte[] byteArray = allData.createByteArray();
		return ByteBuffer.wrap(byteArray);
	}

	@Override
    public void farEndClosed(Channel channel) {
        // TODO: deal with this
    }

    @Override
    public void failure(Channel channel, ByteBuffer data, Exception e) {
        // TODO: deal with this
    }

    @Override
    public void applyBackPressure(Channel channel) {

    }

    @Override
    public void releaseBackPressure(Channel channel) {

    }
}