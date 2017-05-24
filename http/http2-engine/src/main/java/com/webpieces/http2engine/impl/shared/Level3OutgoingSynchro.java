package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.FuturePermitQueue;
import com.webpieces.util.locking.PermitQueue;

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
public abstract class Level3OutgoingSynchro {

	private static final Logger log = LoggerFactory.getLogger(Level3OutgoingSynchro.class);

	protected FuturePermitQueue singleThreadSerializer;
	private Level4AbstractStreamMgr<?> streamsLayer;
	private HeaderSettings localSettings;

	private Level7MarshalAndPing notifyListener;

	protected PermitQueue maxConcurrentQueue;
	//purely for logging!!!  do not use for something else
	protected AtomicInteger acquiredCnt = new AtomicInteger(0);
	
	public Level3OutgoingSynchro(
			FuturePermitQueue serializer, 
			PermitQueue maxConcurrentQueue,
			Level4AbstractStreamMgr<?> streams,
			Level7MarshalAndPing notifyListener,
			HeaderSettings localSettings
	) {
		this.singleThreadSerializer = serializer;
		this.maxConcurrentQueue = maxConcurrentQueue;
		this.streamsLayer = streams;
		this.notifyListener = notifyListener;
		this.localSettings = localSettings;
	}

	public CompletableFuture<Void> sendSettings() {
		SettingsFrame settings = HeaderSettings.createSettingsFrame(localSettings);
		return notifyListener.sendFrameToSocket(settings);
	}
	
	public void initiateClose(String reason) {
		
	}
	
	public CompletableFuture<Void> sendData(Stream stream, PartialStream data) {		
		return singleThreadSerializer.runRequest( () -> {
			return streamsLayer.sendData(stream, data);
		});
	}
	
	public void modifyMaxConcurrentStreams(long value) {
		int permitCount = maxConcurrentQueue.totalPermits();
		if(value == permitCount)
			return;
		else if (value > Integer.MAX_VALUE)
			throw new IllegalArgumentException("remote setting too large");

		int modifyPermitsCnt = (int) (value - permitCount);
		maxConcurrentQueue.modifyPermitPoolSize(modifyPermitsCnt);
	}
}
