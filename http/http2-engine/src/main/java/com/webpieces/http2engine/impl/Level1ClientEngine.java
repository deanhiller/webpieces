package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class Level1ClientEngine implements Http2ClientEngine {
	
	private static final Logger log = LoggerFactory.getLogger(Level1ClientEngine.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final byte[] preface = DatatypeConverter.parseHexBinary("505249202a20485454502f322e300d0a0d0a534d0d0a0d0a");
	private static final HeaderSettings DEFAULT = new HeaderSettings();

	private Level2ParsingAndRemoteSettings parsing;
	private Level3StreamInitialization streamInit;
	private Level6NotifyListeners notifyListener;
	private HeaderSettings localSettings;

	public Level1ClientEngine(
			HpackParser parser, 
			ClientEngineListener socketListener, 
			HeaderSettings localSettings
	) {
		this.localSettings = localSettings;
		HeaderSettings remoteSettings = new HeaderSettings();

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState();
		
		notifyListener = new Level6NotifyListeners(parser, socketListener, remoteSettings);
		Level5RemoteFlowControl remoteFlowCtrl = new Level5RemoteFlowControl(streamState, notifyListener, remoteSettings);
		Level5LocalFlowControl localFlowCtrl = new Level5LocalFlowControl(notifyListener, localSettings);
		Level4ClientStateMachine clientSm = new Level4ClientStateMachine(localSettings.getId(), remoteFlowCtrl, localFlowCtrl);
		streamInit = new Level3StreamInitialization(streamState, clientSm, remoteFlowCtrl, localSettings, remoteSettings);
		parsing = new Level2ParsingAndRemoteSettings(streamInit, remoteFlowCtrl, notifyListener, parser, localSettings, remoteSettings);
	}

	@Override
	public CompletableFuture<Void> sendInitializationToSocket() {
		DataWrapper prefaceData = dataGen.wrapByteArray(preface);
		SettingsFrame settings = create();
		log.info("sending settings frame="+settings);
		return notifyListener.sendInitDataToSocket(prefaceData, settings);
	}
	
	private SettingsFrame create() {
		SettingsFrame f = new SettingsFrame();
		
		if(localSettings.getHeaderTableSize() != DEFAULT.getHeaderTableSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_HEADER_TABLE_SIZE, localSettings.getHeaderTableSize()));
		if(localSettings.isPushEnabled() != DEFAULT.isPushEnabled()) {
			long enabled = 1;
			if(!localSettings.isPushEnabled())
				enabled = 0;
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_ENABLE_PUSH, enabled));
		}
		if(localSettings.getMaxConcurrentStreams() != DEFAULT.getMaxConcurrentStreams())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS, localSettings.getMaxConcurrentStreams()));
		if(localSettings.getInitialWindowSize() != DEFAULT.getInitialWindowSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_INITIAL_WINDOW_SIZE, localSettings.getInitialWindowSize()));
		if(localSettings.getMaxFrameSize() != DEFAULT.getMaxFrameSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_FRAME_SIZE, localSettings.getMaxFrameSize()));		
		if(localSettings.getMaxHeaderListSize() != DEFAULT.getMaxHeaderListSize())
			f.addSetting(new Http2Setting(SettingsParameter.SETTINGS_MAX_HEADER_LIST_SIZE, localSettings.getMaxHeaderListSize()));			
		
		return f;
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		PingFrame ping = new PingFrame();
		return notifyListener.sendPing(ping);
	}
	
	@Override
	public CompletableFuture<StreamWriter> sendFrameToSocket(Http2Headers headers, Http2ResponseListener responseListener) {
		int streamId = headers.getStreamId();
		if(streamId <= 0)
			throw new IllegalArgumentException("frames for requests must have a streamId > 0");
		else if(streamId % 2 == 0)
			throw new IllegalArgumentException("Client cannot send frames with even stream ids to server per http/2 spec");
		
		return streamInit.createStreamAndSend(headers, responseListener);
	}

	@Override
	public void parse(DataWrapper newData) {
		parsing.parse(newData);
	}

	@Override
	public void farEndClosed() {
		notifyListener.farEndClosed();
	}

}
