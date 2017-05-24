package com.webpieces.http2engine.impl.shared;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.ParseFailReason;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public abstract class Level2ParsingAndRemoteSettings {

	private static final Logger log = LoggerFactory.getLogger(Level2ParsingAndRemoteSettings.class);

	private HpackParser lowLevelParser;
	private UnmarshalState parsingState;
	private Level7MarshalAndPing marshalLayer;
	private String id;
	protected Level3IncomingSynchro syncro;
	private HeaderSettings localSettings;

	protected Level3OutgoingSynchro outSyncro;

	public Level2ParsingAndRemoteSettings(
			Level3IncomingSynchro syncro,
			Level3OutgoingSynchro outSyncro,
			Level7MarshalAndPing notifyListener, 
			HpackParser lowLevelParser, 
			Http2Config config
	) {
		this.outSyncro = outSyncro;
		this.localSettings = config.getLocalSettings();
		this.id = config.getId();
		
		this.syncro = syncro;
		this.marshalLayer = notifyListener;
		this.lowLevelParser = lowLevelParser;
		parsingState = lowLevelParser.prepareToUnmarshal(4096, localSettings.getHeaderTableSize(), localSettings.getMaxFrameSize());
	}

	/**
	 * NOT thread safe!!! BUT channelmanager keeps everything virtually thread-safe (ie. it's all going to come in order)
	 * @return 
	 * @return 
	 */
	public CompletableFuture<Void> parse(DataWrapper newData) {
		CompletableFuture<Void> future;
		try {
			future = parseImpl(newData);
		} catch(Throwable t) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(t);
		}
		
		return future.handle((resp, t) -> handleError(resp, t));
	}

	private Void handleError(Object object, Throwable e) {
		if(e == null) 
			return null;
		else if(e instanceof ConnectionClosedException) {
			log.trace(() -> "Normal exception since we are closing and they do not know yet", e);
			
		} else if(e instanceof StreamException) {
			log.error("shutting the stream down due to error", e);
			syncro.sendRstToServerAndApp((StreamException) e).exceptionally( t -> logExc("stream", t));
		} else if(e instanceof ConnectionException) {
			log.error("shutting the connection down due to error", e);
			ConnectionReset reset = new ConnectionReset((ConnectionException) e);
			syncro.sendGoAwayToSvrAndResetAllToApp(reset).exceptionally( t -> logExc("connection", t)); //send GoAway
		} else {
			log.error("shutting the connection down due to error", e);
			ConnectionException exc = new ConnectionException(ParseFailReason.BUG, 0, e.getMessage(), e);
			ConnectionReset reset = new ConnectionReset(exc);
			syncro.sendGoAwayToSvrAndResetAllToApp(reset).exceptionally( t -> logExc("connection", t)); //send GoAwa
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
		if(msg instanceof PartialStream) {
			//we may be sending to client app or server app depending on if this is serverside or clientside
			return sendPayloadToApp((PartialStream) msg);
		} else if(msg instanceof GoAwayFrame) {
			ConnectionReset reset = new ConnectionReset("Far end sent goaway to us", (GoAwayFrame)msg, true);
			return syncro.sendGoAwayToApp(reset).exceptionally( t -> logExc("connection", t)); //send GoAwa
		} else if(msg instanceof PingFrame) {
			return marshalLayer.processPing((PingFrame)msg);
		} else if(msg instanceof SettingsFrame) {
			return processHttp2SettingsFrame((SettingsFrame) msg);
		} else if(msg instanceof WindowUpdateFrame){
			return syncro.updateWindowSize((WindowUpdateFrame)msg);
		} 
		
		return processSpecific(msg);
	}

	private CompletableFuture<Void> sendPayloadToApp(PartialStream msg) {
		if(msg instanceof PriorityFrame) {
			return syncro.sendPriorityFrame((PriorityFrame)msg);
		} else if(msg instanceof RstStreamFrame) {
			return syncro.sendRstToApp((RstStreamFrame) msg);
		}
		return syncro.sendPayloadToApp((PartialStream) msg);
//				.thenApply( s -> {
//					if(s != null)
//						outSyncro.release(s, msg);
//					return null;
//				});
	}

	protected abstract CompletableFuture<Void> processSpecific(Http2Msg msg);

	private CompletableFuture<Void> processHttp2SettingsFrame(SettingsFrame settings) {
		if(settings.isAck()) {
			log.info("server acked our settings frame");
			return CompletableFuture.completedFuture(null);
		} else {
			log.info("applying remote settings frame");
			
			return syncro.applyRemoteSettingsAndAck(settings);
		}
	}

}
