package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.error.StreamException;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.PriorityFrame;
import com.webpieces.http2.api.dto.lowlevel.WindowUpdateFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;

public abstract class Level6LocalFlowControl {

	private static final Logger log = LoggerFactory.getLogger(Level6LocalFlowControl.class);
	private Level7MarshalAndPing marshalLayer;
	private long connectionLocalWindowSize;
	private long totalSent = 0;
	private long totalRecovered = 0;
	private EngineResultListener notifyListener;
	private String logId;

	public Level6LocalFlowControl(
			String logId,
			Level7MarshalAndPing marshalLayer,
			EngineResultListener notifyListener,
			HeaderSettings localSettings
	) {
		this.marshalLayer = marshalLayer;
		this.notifyListener = notifyListener;
		this.connectionLocalWindowSize = localSettings.getInitialWindowSize();
	}
	
//	public CompletableFuture<Void> fireHeadersToClient(Stream stream, Http2Trailers payload) {
//		return notifyListener.sendPieceToApp(stream, payload);
//	}

	public CompletableFuture<Void> firePriorityToClient(Stream stream, PriorityFrame payload) {
		return notifyListener.sendPieceToApp(stream, payload);		
	}
	
	public CompletableFuture<Void> fireRstToClient(Stream stream, CancelReason payload) {
		return notifyListener.sendRstToApp(stream, payload);		
	}	
	
	public CompletableFuture<Void> fireDataToClient(Stream stream, StreamMsg payload) {
		if(!(payload instanceof DataFrame))
			return notifyListener.sendPieceToApp(stream, payload);

		DataFrame f = (DataFrame) payload;
		long frameLength = f.getTransmitFrameLength();

		if(frameLength > connectionLocalWindowSize) {
			throw new ConnectionException(CancelReasonCode.FLOW_CONTROL_ERROR, logId, f.getStreamId(), 
					"connectionLocalWindowSize too small="+connectionLocalWindowSize
					+" frame len="+frameLength+" for frame="+f);
		} else if(frameLength > stream.getLocalWindowSize()) {
			throw new StreamException(CancelReasonCode.FLOW_CONTROL_ERROR, logId, f.getStreamId(), 
					"connectionLocalWindowSize too small="+connectionLocalWindowSize
					+" frame len="+frameLength+" for frame="+f);
		}
		
		totalSent += frameLength;
		connectionLocalWindowSize -= frameLength;
		stream.incrementLocalWindow(-frameLength);
		log.info("received framelen="+frameLength+" newConnectionWindowSize="
				+connectionLocalWindowSize+" streamSize="+stream.getLocalWindowSize()+" totalSent="+totalSent);
		
		return notifyListener.sendPieceToApp(stream, payload)
			.thenApply(c -> updateFlowControl(frameLength, stream));
	}

	private Void updateFlowControl(long frameLength, Stream stream) {
		if(frameLength == 0)
			return null; //nothing to do if it is a 0 length frame.  
		
		//TODO: we could optimize this to send very large window updates and send less window updates instead of
		//what we do currently sending many increase window by 13 byte updates and such.
		connectionLocalWindowSize += frameLength;
		stream.incrementLocalWindow(frameLength);
		totalRecovered += frameLength;

		int len = (int) frameLength;
		WindowUpdateFrame w1 = new WindowUpdateFrame();
		w1.setStreamId(0);
		w1.setWindowSizeIncrement(len);		

		marshalLayer.sendFrameToSocket(w1);
		
		if(!stream.isClosed()) {
			
			//IF the stream is not closed, update flow control
			WindowUpdateFrame w2 = new WindowUpdateFrame();
			w2.setStreamId(stream.getStreamId());
			w2.setWindowSizeIncrement(len);
			
			log.info("sending BOTH WUF increments. framelen="+frameLength+" recovered="+totalRecovered );
			marshalLayer.sendFrameToSocket(w2);
		} else {
			log.info("sending WUF increments. framelen="+frameLength+" recovered="+totalRecovered);
		}

		return null;
	}
	
}
