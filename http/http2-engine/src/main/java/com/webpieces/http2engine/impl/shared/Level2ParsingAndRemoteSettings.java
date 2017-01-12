package com.webpieces.http2engine.impl.shared;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class Level2ParsingAndRemoteSettings {

	private static final Logger log = LoggerFactory.getLogger(Level2ParsingAndRemoteSettings.class);
	private static final HeaderSettings DEFAULT = new HeaderSettings();

	private HpackParser lowLevelParser;
	private UnmarshalState parsingState;
	private Level6MarshalAndPing marshalLayer;
	private String id;
	private Level5RemoteFlowControl remoteFlowControl;
	private Level3StreamInitialization level3StreamInit;
	private HeaderSettings remoteSettings;
	private HeaderSettings localSettings;

	public Level2ParsingAndRemoteSettings(
			Level3StreamInitialization level3StreamInit,
			Level5RemoteFlowControl level5FlowControl,
			Level6MarshalAndPing level6NotifyListener, 
			HpackParser lowLevelParser, 
			HeaderSettings localSettings,
			HeaderSettings remoteSettings
	) {
		this.localSettings = localSettings;
		this.remoteSettings = remoteSettings;
		this.id = localSettings.getId();
		this.level3StreamInit = level3StreamInit;
		this.remoteFlowControl = level5FlowControl;
		this.marshalLayer = level6NotifyListener;
		this.lowLevelParser = lowLevelParser;
		parsingState = lowLevelParser.prepareToUnmarshal(4096, localSettings.getHeaderTableSize(), localSettings.getMaxFrameSize());
	}

	/**
	 * NOT thread safe!!! BUT channelmanager keeps everything virtually thread-safe (ie. it's all going to come in order)
	 */
	public void parse(DataWrapper newData) {
		parsingState = lowLevelParser.unmarshal(parsingState, newData);
		List<Http2Msg> parsedMessages = parsingState.getParsedFrames();
		
		for(Http2Msg lowLevelFrame : parsedMessages) {
			process(lowLevelFrame);
		}
	}

	public void process(Http2Msg msg) {
		log.info(id+"frame from socket="+msg);
		if(msg instanceof PartialStream) {
			level3StreamInit.sendPayloadToClient((PartialStream) msg);
		} else if(msg instanceof GoAwayFrame) {
			marshalLayer.sendControlFrameToClient(msg);
		} else if(msg instanceof PingFrame) {
			marshalLayer.processPing((PingFrame)msg);
		} else if(msg instanceof SettingsFrame) {
			processHttp2SettingsFrame((SettingsFrame) msg);
		} else if(msg instanceof WindowUpdateFrame){
			level3StreamInit.updateWindowSize((WindowUpdateFrame)msg);
		} else
			throw new IllegalArgumentException("Unknown HttpMsg type.  msg="+msg+" type="+msg.getClass());
	}

	private void processHttp2SettingsFrame(SettingsFrame settings) {
		if(settings.isAck()) {
			log.info("server acked our settings frame");
		} else {
			log.info("applying remote settings frame");
			
			applyRemoteSettings(settings);
			//now that settings is applied, ack the settings
			SettingsFrame settingsAck = new SettingsFrame(true);
			
			log.info("sending remote settings ack frame");
			marshalLayer.sendControlDataToSocket(settingsAck);
		}
	}

	public void applyRemoteSettings(SettingsFrame settings) {
		for(Http2Setting setting : settings.getSettings()) {
			SettingsParameter key = setting.getKnownName();
			if(key == null)
				//TODO: forward unknown settings to clients
				continue; //unknown setting so skip it
			apply(key, setting.getValue());
		}
	}
	
	private void apply(SettingsParameter key, long value) {
		switch(key) {
			case SETTINGS_HEADER_TABLE_SIZE:
				marshalLayer.setEncoderMaxTableSize( convertToInt(value));
				break;
			case SETTINGS_ENABLE_PUSH:
				applyPushEnabled(value);
				break;
			case SETTINGS_MAX_CONCURRENT_STREAMS:
				level3StreamInit.setMaxConcurrentStreams(value);
				break;
			case SETTINGS_INITIAL_WINDOW_SIZE:
				remoteFlowControl.resetInitialWindowSize(value);
				break;
			case SETTINGS_MAX_FRAME_SIZE:
				applyMaxFrameSize(value);
				break;
			case SETTINGS_MAX_HEADER_LIST_SIZE:
				remoteSettings.setMaxHeaderListSize(value);
				break;
			default:
				throw new RuntimeException("bug, someone forgot to add some new setting="+key+" which had value="+value);
		}
	}
	
	private void applyPushEnabled(long value) {
		if(value == 0)
			remoteSettings.setPushEnabled(false);
		else if(value == 1)
			remoteSettings.setPushEnabled(true);
		else
			throw new RuntimeException("bug, this should not happen with other preconditions in place");
	}
	
	private void applyMaxFrameSize(long val) {
		int value = convertToInt(val);
		remoteSettings.setMaxFrameSize(value);
	}
	
	private int convertToInt(long value) {
		if(value > Integer.MAX_VALUE)
			throw new RuntimeException("Bug, hpack library only supports up to max integer.  need to modify that library");
		return (int)value;
	}

	public SettingsFrame createSettingsFrame(HeaderSettings localSettings) {
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

	public CompletableFuture<Void> sendSettings() {
		SettingsFrame settings = createSettingsFrame(localSettings);
		return marshalLayer.sendFrameToSocket(settings);
	}
}
