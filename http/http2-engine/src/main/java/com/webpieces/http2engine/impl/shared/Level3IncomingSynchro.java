package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.FuturePermitQueue;

/**
 * We want events to run to completion so statemachine changes are final!!  This means advanced things
 * like sendtoSocket().thenApply(checkForCloseState) get run before the next event is fired.  
 * Therefore, we must ensure we permit queue up all things coming in and run them to completion
 * before processing the next event.
 * 
 * ie. every Future must complete before the next one is run
 * 
 * @author dhiller
 *
 */
public abstract class Level3IncomingSynchro {

	private static final Logger log = LoggerFactory.getLogger(Level3IncomingSynchro.class);

	protected Level4AbstractStreamMgr<?> streamsLayer;
	private Level7MarshalAndPing notifyListener;
	private RemoteSettingsManagement remoteSettings;


//	//We allow processing serially of each method call.  All futures below only mean the request was comletely
//	//consumed by client or by the socket(ie. Future.complete(...) is when the next request can be processed
	protected FuturePermitQueue serializer;

	public Level3IncomingSynchro(
			FuturePermitQueue serializer,
			Level4AbstractStreamMgr<?> streamsLayer, 
			Level7MarshalAndPing notifyListener,
			RemoteSettingsManagement remoteSettings
			) {
		this.serializer = serializer;
		this.streamsLayer = streamsLayer;
		this.notifyListener = notifyListener;
		this.remoteSettings = remoteSettings;
	}

	public CompletableFuture<Void> applyRemoteSettingsAndAck(SettingsFrame settings) {
		return serializer.runRequest( () -> {
		
			remoteSettings.applyRemoteSettings(settings);
			
			//now that settings is applied, ack the settings
			SettingsFrame settingsAck = new SettingsFrame(true);
			
			log.info("sending remote settings ack frame");
			return notifyListener.sendControlDataToSocket(settingsAck);
		});
	}

	public CompletableFuture<Void> sendPriorityFrame(PriorityFrame msg) {
		return serializer.runRequest( () -> {
			return streamsLayer.sendPriorityFrame(msg);			
		});
	}

	public CompletableFuture<Void> sendPayloadToApp(PartialStream msg) {
		return serializer.runRequest( () -> {
			return streamsLayer.sendPayloadToApp(msg);
		});
	}

	public CompletableFuture<Void> sendRstToApp(RstStreamFrame frame) {
		return serializer.runRequest( () -> {
			return streamsLayer.sendRstToApp(frame);
		});
	}
	
	public CompletableFuture<Void> sendRstToServerAndApp(StreamException e) {
		return serializer.runRequest( () -> {
			return streamsLayer.sendRstToServerAndApp(e);
		});
	}

	public CompletableFuture<Void> sendGoAwayToApp(ConnectionReset reset) {
		return serializer.runRequest( () -> {
			return streamsLayer.sendGoAwayToApp(reset);
		});
	}
	
	public CompletableFuture<Void> sendGoAwayToSvrAndResetAllToApp(ConnectionReset reset) {
		return serializer.runRequest( () -> {
			CompletableFuture<Void> future = streamsLayer.sendGoAwayToSvrAndResetAllToApp(reset);
			return future;
		});
	}
	
	public CompletableFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		return serializer.runRequest( () -> {
			return streamsLayer.updateWindowSize(msg);
		});
	}

}
