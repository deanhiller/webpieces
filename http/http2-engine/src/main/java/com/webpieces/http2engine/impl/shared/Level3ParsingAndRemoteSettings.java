package com.webpieces.http2engine.impl.shared;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.StreamException;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class Level3ParsingAndRemoteSettings {

	private static final Logger log = LoggerFactory.getLogger(Level3ParsingAndRemoteSettings.class);

	private HpackParser lowLevelParser;
	private UnmarshalState parsingState;
	private Level7MarshalAndPing marshalLayer;
	private String id;
	private Level6RemoteFlowControl remoteFlowControl;
	private Level4AbstractStreamMgr level3StreamInit;
	private HeaderSettings remoteSettings;
	private HeaderSettings localSettings;

	public Level3ParsingAndRemoteSettings(
			Level4AbstractStreamMgr level3StreamInit,
			Level6RemoteFlowControl level5FlowControl,
			Level7MarshalAndPing level6NotifyListener, 
			HpackParser lowLevelParser, 
			Http2Config config,
			HeaderSettings remoteSettings
	) {
		this.localSettings = config.getLocalSettings();
		this.remoteSettings = remoteSettings;
		this.id = config.getId();
		
		this.level3StreamInit = level3StreamInit;
		this.remoteFlowControl = level5FlowControl;
		this.marshalLayer = level6NotifyListener;
		this.lowLevelParser = lowLevelParser;
		parsingState = lowLevelParser.prepareToUnmarshal(4096, localSettings.getHeaderTableSize(), localSettings.getMaxFrameSize());
	}

	/**
	 * NOT thread safe!!! BUT channelmanager keeps everything virtually thread-safe (ie. it's all going to come in order)
	 * @return 
	 */
	public void parse(DataWrapper newData) {
		CompletableFuture<Void> future;
		try {
			future = parseImpl(newData);
		} catch(Throwable t) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(t);
		}
		
		future.handle((resp, t) -> handleError(resp, t));
	}

	private Void handleError(Object object, Throwable e) {
		if(e == null) 
			return null;
		else if(e instanceof ConnectionClosedException) {
			log.trace(() -> "Normal exception since we are closing and they do not know yet", e);
		} else if(e instanceof StreamException) {
			log.error("shutting the stream down due to error", e);
			level3StreamInit.sendRstToServerAndClient((StreamException) e).exceptionally( t -> logExc("stream", t));
		} else if(e instanceof ConnectionException) {
			log.error("shutting the connection down due to error", e);
			level3StreamInit.sendClientResetsAndSvrGoAway((ConnectionException) e).exceptionally( t -> logExc("connection", t)); //send GoAway
		} else {
			log.error("shutting the connection down due to error", e);
			level3StreamInit.sendClientResetsAndSvrGoAway(new ConnectionException(ParseFailReason.BUG, 0, e.getMessage(), e)).exceptionally( t -> logExc("connection", t)); //send GoAwa
		}
		return null;
	}

	private Void logExc(String thing, Throwable t) {
		log.error("error trying to close "+thing, t);
		return null;
	}
	
	public CompletableFuture<Void> parseImpl(DataWrapper newData) {
		parsingState = lowLevelParser.unmarshal(parsingState, newData);
		List<Http2Msg> parsedMessages = parsingState.getParsedFrames();
		
		CompletableFuture<Void> future = CompletableFuture.completedFuture((Void)null);
		for(Http2Msg lowLevelFrame : parsedMessages) {
			CompletableFuture<Void> f = process(lowLevelFrame);
			future = future.thenCompose(s -> f);
		}
		return future;
	}

	public CompletableFuture<Void> process(Http2Msg msg) {
		log.info(id+"frame from socket="+msg);
		if(msg instanceof PriorityFrame) {
			return level3StreamInit.sendPriorityFrame((PriorityFrame)msg);
		} else if(msg instanceof PartialStream) {
			//we may be sending to client app or server app depending on if this is serverside or clientside
			return level3StreamInit.sendPayloadToApp((PartialStream) msg);
		} else if(msg instanceof GoAwayFrame) {
			return marshalLayer.sendControlFrameToClient(msg);
		} else if(msg instanceof PingFrame) {
			return marshalLayer.processPing((PingFrame)msg);
		} else if(msg instanceof SettingsFrame) {
			return processHttp2SettingsFrame((SettingsFrame) msg);
		} else if(msg instanceof WindowUpdateFrame){
			return level3StreamInit.updateWindowSize((WindowUpdateFrame)msg);
		} else
			throw new IllegalArgumentException("Unknown HttpMsg type.  msg="+msg+" type="+msg.getClass());
	}

	private CompletableFuture<Void> processHttp2SettingsFrame(SettingsFrame settings) {
		if(settings.isAck()) {
			log.info("server acked our settings frame");
			return CompletableFuture.completedFuture(null);
		} else {
			log.info("applying remote settings frame");
			
			applyRemoteSettings(settings);
			//now that settings is applied, ack the settings
			SettingsFrame settingsAck = new SettingsFrame(true);
			
			log.info("sending remote settings ack frame");
			return marshalLayer.sendControlDataToSocket(settingsAck);
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

	public CompletableFuture<Void> sendSettings() {
		SettingsFrame settings = HeaderSettings.createSettingsFrame(localSettings);
		return marshalLayer.sendFrameToSocket(settings);
	}
}
