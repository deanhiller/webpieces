package com.webpieces.http2engine.impl.shared;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.error.StreamException;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.PriorityFrame;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.UnknownFrame;
import com.webpieces.http2.api.dto.lowlevel.WindowUpdateFrame;
import com.webpieces.http2engine.api.error.ConnReset2;
import com.webpieces.http2engine.api.error.ShutdownConnection;

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

	public XFuture<Void> applyRemoteSettingsAndAck(SettingsFrame settings) {
		remoteSettings.applyRemoteSettings(settings);
		
		//now that settings is applied, ack the settings
		SettingsFrame settingsAck = new SettingsFrame(true);
		
		log.info("sending remote settings ack frame");
		return notifyListener.sendControlDataToSocket(settingsAck);
	}

	public XFuture<Void> sendPriorityFrameToApp(PriorityFrame msg) {
		return streamsLayer.sendPriorityFrameToApp(msg);			
	}

	public XFuture<Void> sendDataToApp(DataFrame msg) {
		return streamsLayer.sendDataToApp(msg);
	}

	public XFuture<Void> sendTrailersToApp(Http2Trailers msg) {
		return streamsLayer.sendTrailersToApp(msg);
	}
	
	public XFuture<Void> sendRstToApp(RstStreamFrame frame) {
		return streamsLayer.sendRstToApp(frame);
	}
	
	public XFuture<Void> sendRstToServerAndApp(StreamException e) {
		return streamsLayer.sendRstToServerAndApp(e);
	}

	public XFuture<Void> sendGoAwayToApp(ConnReset2 reset) {
		return streamsLayer.sendGoAwayToApp(reset);
	}
	
	public XFuture<Void> sendGoAwayToSvrAndResetAllToApp(ShutdownConnection reset) {
		return streamsLayer.sendGoAwayToSvrAndResetAllToApp(reset);
	}
	
	public XFuture<Void> updateWindowSize(WindowUpdateFrame msg) {
		return streamsLayer.updateWindowSize(msg);
	}

	public XFuture<Void> sendUnkownFrameToApp(UnknownFrame msg) {
		return streamsLayer.sendUnknownFrame(msg);
	}

}
