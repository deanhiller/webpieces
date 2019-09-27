package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.error.ConnReset2;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.error.StreamException;

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

	protected Level4PreconditionChecks<?> streamsLayer;
	private Level7MarshalAndPing notifyListener;
	private RemoteSettingsManagement remoteSettings;

	public Level3IncomingSynchro(
			Level4PreconditionChecks<?> streamsLayer, 
			Level7MarshalAndPing notifyListener,
			RemoteSettingsManagement remoteSettings
			) {
		this.streamsLayer = streamsLayer;
		this.notifyListener = notifyListener;
		this.remoteSettings = remoteSettings;
	}

	public CompletableFuture<Void> applyRemoteSettingsAndAck(SettingsFrame settings) {
		remoteSettings.applyRemoteSettings(settings);
		
		//now that settings is applied, ack the settings
		SettingsFrame settingsAck = new SettingsFrame(true);
		
		log.info("sending remote settings ack frame");
		return notifyListener.sendControlDataToSocket(settingsAck);
	}

	public CompletableFuture<Void> sendPriorityFrameToApp(PriorityFrame msg) {
		return streamsLayer.sendPriorityFrameToApp(msg);			
	}

	public CompletableFuture<Void> sendDataToApp(DataFrame msg) {
		return streamsLayer.sendDataToApp(msg);
	}

	public CompletableFuture<Void> sendTrailersToApp(Http2Trailers msg) {
		return streamsLayer.sendTrailersToApp(msg);
	}
	
	public CompletableFuture<Void> sendRstToApp(RstStreamFrame frame) {
		return streamsLayer.sendRstToApp(frame);
	}
	
	public CompletableFuture<Void> sendRstToServerAndApp(StreamException e) {
		return streamsLayer.sendRstToServerAndApp(e);
	}

	public CompletableFuture<Void> sendGoAwayToApp(ConnReset2 reset) {
		return streamsLayer.sendGoAwayToApp(reset);
	}
	
	public CompletableFuture<Void> sendGoAwayToSvrAndResetAllToApp(ShutdownConnection reset) {
		return streamsLayer.sendGoAwayToSvrAndResetAllToApp(reset);
	}
	
	public CompletableFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		return streamsLayer.updateWindowSize(msg);
	}

	public CompletableFuture<Void> sendUnkownFrameToApp(UnknownFrame msg) {
		return streamsLayer.sendUnknownFrame(msg);
	}

}
