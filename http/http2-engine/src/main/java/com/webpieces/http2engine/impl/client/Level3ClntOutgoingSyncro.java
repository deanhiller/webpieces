package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.EngineStreamWriter;
import com.webpieces.http2engine.impl.shared.Level3OutgoingSynchro;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.Synchro;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.util.locking.FuturePermitQueue;
import com.webpieces.util.locking.PermitQueue;

public class Level3ClntOutgoingSyncro extends Level3OutgoingSynchro implements Synchro {

	private static final Logger log = LoggerFactory.getLogger(Level3ClntOutgoingSyncro.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final byte[] preface = DatatypeConverter.parseHexBinary("505249202a20485454502f322e300d0a0d0a534d0d0a0d0a");

	private Level8NotifyClntListeners finalLayer;
	private Level4ClientStreams streamInit;
	
	public Level3ClntOutgoingSyncro(
			FuturePermitQueue serializer,
			PermitQueue maxConcurrentQueue,
			Level4ClientStreams streamsLayer, 
			Level6RemoteFlowControl remoteFlow,
			Level7MarshalAndPing notifyListener,
			HeaderSettings localSettings,
			Level8NotifyClntListeners finalLayer
	) {
		super(serializer, maxConcurrentQueue, streamsLayer, notifyListener, localSettings);
		this.streamInit = streamsLayer;
		this.finalLayer = finalLayer;
	}
	
	public CompletableFuture<Void> sendInitializationToSocket() {
		//important, this forces the engine to a virtual single thread(each engine/socket has one virtual thread)
		//this makes it very easy not to have bugs AND very easy to test AND for better throughput, you can
		//just connect more sockets
		log.info("sending preface");
		DataWrapper prefaceData = dataGen.wrapByteArray(preface);
		
		return singleThreadSerializer.runRequest(() -> {
			return finalLayer.sendPreface(prefaceData)
				.thenCompose( v -> super.sendSettings());
		});
	}
	
	public CompletableFuture<StreamWriter> sendRequestToSocket(Http2Request headers, ResponseHandler2 responseListener) {
		//This gets tricky, BUT must use the maxConcurrent permit queue first, THEN the serializer permit queue
		return maxConcurrentQueue.runRequest( () -> {
			int val = acquiredCnt.incrementAndGet();
			log.info("got permit(cause="+headers+").  size="+maxConcurrentQueue.availablePermits()+" acquired="+val);
			
			return singleThreadSerializer.runRequest(() -> {
				return streamInit.createStreamAndSend(headers, responseListener);
			});
		}).thenApply( s -> new EngineStreamWriter(s, Level3ClntOutgoingSyncro.this));
	}
	

}
