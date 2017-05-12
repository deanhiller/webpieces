package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.impl.RequestWriterImpl;
import com.webpieces.http2engine.impl.shared.Level2Synchro;
import com.webpieces.http2engine.impl.shared.Level3ParsingAndRemoteSettings;

public class Level1BClientSynchro extends Level2Synchro {
	private static final Logger log = LoggerFactory.getLogger(Level1BClientSynchro.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final byte[] preface = DatatypeConverter.parseHexBinary("505249202a20485454502f322e300d0a0d0a534d0d0a0d0a");
	private Level8NotifyListeners finalLayer;
	private Level4ClientStreams streamInit;

	public Level1BClientSynchro(Level4ClientStreams level3, Level3ParsingAndRemoteSettings parsing, Level8NotifyListeners finalLayer, SessionExecutor executor) {
		super(level3, parsing, executor);
		streamInit = level3;
		this.finalLayer = finalLayer;
		
	}

	public CompletableFuture<Void> sendInitializationToSocket() {
		//important, this forces the engine to a virtual single thread(each engine/socket has one virtual thread)
		//this makes it very easy not to have bugs AND very easy to test AND for better throughput, you can
		//just connect more sockets
		return executor.executeCall(this, () -> { 
			log.info("sending preface");
			DataWrapper prefaceData = dataGen.wrapByteArray(preface);
			finalLayer.sendPreface(prefaceData);
	
			return parsing.sendSettings();
		});
	}

	public CompletableFuture<StreamWriter> sendFrameToSocket(Http2Headers headers, Http2ResponseListener responseListener) {
		//important, this forces the engine to a virtual single thread(each engine/socket has one virtual thread)
		//this makes it very easy not to have bugs AND very easy to test AND for better throughput, you can
		//just connect more sockets
		return executor.executeCall(this, () -> { 
			int streamId = headers.getStreamId();
			if(streamId <= 0)
				throw new IllegalArgumentException("frames for requests must have a streamId > 0");
			else if(streamId % 2 == 0)
				throw new IllegalArgumentException("Client cannot send frames with even stream ids to server per http/2 spec");
			
			return streamInit.createStreamAndSend(headers, responseListener)
					.thenApply((s) -> new RequestWriterImpl(s, this));
		});
	}




}
