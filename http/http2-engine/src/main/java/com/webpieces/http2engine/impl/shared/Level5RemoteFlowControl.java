package com.webpieces.http2engine.impl.shared;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.impl.DataTry;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level5RemoteFlowControl {

	private static final Logger log = LoggerFactory.getLogger(Level5RemoteFlowControl.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private HeaderSettings remoteSettings;
	private Level6MarshalAndPing layer6NotifyListener;

	private long remoteWindowSize;
	
	private Object remoteLock = new Object();
	
	private LinkedList<DataTry> dataQueue = new LinkedList<>();

	private StreamState streamState;

	public Level5RemoteFlowControl(
			StreamState streamState,
			Level6MarshalAndPing layer6NotifyListener, 
			HeaderSettings remoteSettings
	) {
		this.streamState = streamState;
		this.layer6NotifyListener = layer6NotifyListener;
		this.remoteSettings = remoteSettings;
		remoteWindowSize = remoteSettings.getInitialWindowSize();
	}

	public CompletableFuture<Void> sendPayloadToSocket(Stream stream, PartialStream payload) {
		log.info("sending payload to socket="+payload);
		if(!(payload instanceof DataFrame))
			return layer6NotifyListener.sendFrameToSocket(payload);
		
		DataFrame f = (DataFrame) payload;
		CompletableFuture<Void> future = new CompletableFuture<>();
		DataTry data = new DataTry(stream, f, future, false);
		trySendPayload(data);
		return future;
	}
	
	private void trySendPayload(DataTry data) {
		long length = data.getDataFrame().getTransmitFrameLength();
		Stream stream = data.getStream();

		boolean send;
		synchronized (remoteLock) {
			long min = Math.min(remoteWindowSize, stream.getRemoteWindowSize());
			long lengthToSend = Math.min(length, min);
			if(length != lengthToSend) {
				//must split DataFrame into two since WindowUpdateSize is not large enough
				List<DataTry> tuple = splitDataFrame(data, lengthToSend);
				data = tuple.get(0); //swap the right size to send
				dataQueue.add(0, tuple.get(1));
			}

			if(lengthToSend > 0) {
				stream.incrementRemoteWindow(-lengthToSend);
				remoteWindowSize -= lengthToSend;
				send = true;
			} else if(data.isWasQueuedBefore()) {
				dataQueue.add(0, data); //insert BACK at beginning of queue
				send = false;
			} else {
				dataQueue.add(data); //insert at end of queue
				send = false;
			}
			
			log.info("flow control.  send="+send+" window="+remoteWindowSize+" streamWindow="+stream.getRemoteWindowSize());
		}
		
		if(send) {
			DataTry finalTry = data;
			layer6NotifyListener.sendFrameToSocket(data.getDataFrame())
					.handle((v, t) -> processComplete(v, t, finalTry.getFuture()));
		}
		
	}

	private List<DataTry> splitDataFrame(DataTry dataTry, long lengthToSend) {
		if(lengthToSend > Integer.MAX_VALUE)
			throw new IllegalStateException("bug, length to send should not be this large(per spec)="+lengthToSend);
		int len = (int) lengthToSend; 
		DataFrame dataFrame = dataTry.getDataFrame();
		DataWrapper data = dataFrame.getData();
		
		if(dataFrame.getPadding().getReadableSize() > 0)
			throw new UnsupportedOperationException("Splitting padding under these conditions would be"
					+ " quite difficult so we skipped it.  perhaps stop using padding or"
					+ " modify this code but some padding would have to go in one"
					+ " DataFrame and the rest in the next as the window size is not large enough");
		
		List<? extends DataWrapper> split = dataGen.split(data, len);
		
		DataFrame dF1 = new DataFrame();
		dF1.setData(split.get(0));
		DataFrame dF2 = new DataFrame();
		dF2.setData(split.get(1));
		
		List<DataTry> tuple = new ArrayList<>();
		tuple.add(new DataTry(dataTry.getStream(), dF1, null, dataTry.isWasQueuedBefore()));
		tuple.add(new DataTry(dataTry.getStream(), dF2, dataTry.getFuture(), dataTry.isWasQueuedBefore()));
		
		return tuple;
	}

	private Object processComplete(Void v, Throwable t, CompletableFuture<Void> future) {
		if(future == null)
			return null; //nothing to do as this was only first piece of data
		
		if(t != null) {
			future.completeExceptionally(new RuntimeException(t));
		} else
			future.complete(null);
		return null;
	}

	public void resetInitialWindowSize(long initialWindow) {
		long difference = initialWindow - remoteSettings.getInitialWindowSize();
		
		log.info("modify window size="+initialWindow);
		
		synchronized(remoteLock) {
			remoteWindowSize += difference;
			//next line MUST be set before updating all streams or some streams could be created
			//just after updating all streams and before updating initial window size(ie. they are created with old size).  instead
			//make sure all new streams are using this initialWindow first
			remoteSettings.setInitialWindowSize(initialWindow); 
			//now, update all streams that need updating
			streamState.updateAllStreams(initialWindow);
		}
	}

	//NOTE: this method virtually single threaded when used with channelmanager
	//synchronized is to synchronize with other client threads
	public CompletableFuture<Void> updateConnectionWindowSize(WindowUpdateFrame msg) {
		int increment = msg.getWindowSizeIncrement();
		if(increment == 0) {
			throw new Http2ParseException(ParseFailReason.WINDOW_SIZE_INVALID, msg.getStreamId(), 
					"Received windowUpdate size increment=0");
		}
		
		DataTry dataTry = null;
		DataTry temp = dataQueue.peek();
		synchronized(remoteLock) {
			remoteWindowSize += increment;
			if(remoteWindowSize > Integer.MAX_VALUE)
				throw new Http2ParseException(ParseFailReason.FLOW_CONTROL_ERROR_CONNECTION, 0, 
						"(remote end bad)global remoteWindowSize too large="+remoteWindowSize+" from windows increment="+increment);
			
			if(temp != null && remoteWindowSize > temp.getDataFrame().getTransmitFrameLength())
				dataTry = dataQueue.poll();
			
			log.info("updated window to="+remoteWindowSize+" increment="+msg.getWindowSizeIncrement()+" dataTry to submit="+dataTry);
		}
		
		if(dataTry != null) {
			dataTry.setWasQueuedBefore(true);
			trySendPayload(dataTry);
		}
		
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> updateStreamWindowSize(Stream stream, WindowUpdateFrame msg) {
		if(msg.getWindowSizeIncrement() == 0) {
			throw new Http2ParseException(ParseFailReason.WINDOW_SIZE_INVALID, msg.getStreamId(), 
					"Received windowUpdate size increment=0");
		}
		
		DataTry dataTry = null;
		DataTry temp = dataQueue.peek();
		synchronized(remoteLock) {
			long remoteWindowSize = stream.incrementRemoteWindow(msg.getWindowSizeIncrement());
			
			if(temp != null && remoteWindowSize > temp.getDataFrame().getTransmitFrameLength())
				dataTry = dataQueue.poll();
			
			log.info("updated stream "+stream.getStreamId()+" window to="
					+stream.getRemoteWindowSize()+" increment="+msg.getWindowSizeIncrement()+" dataTry to submit="+dataTry);

		}
		
		if(dataTry != null) {
			dataTry.setWasQueuedBefore(true);
			trySendPayload(dataTry);		
		}

		//someday, remove synchronized above and then complete future when it is complete instead maybe
		return CompletableFuture.completedFuture(null);
	}

}
