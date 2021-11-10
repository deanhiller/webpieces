package com.webpieces.http2engine.impl.shared;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.util.locking.PermitQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;

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

	private Level4PreconditionChecks<?> streamsLayer;
	private HeaderSettings localSettings;

	private Level7MarshalAndPing notifyListener;

	protected PermitQueue maxConcurrentQueue;
	//purely for logging!!!  do not use for something else
	protected AtomicInteger acquiredCnt = new AtomicInteger(0);
	
	public Level3OutgoingSynchro(
			PermitQueue maxConcurrentQueue,
			Level4PreconditionChecks<?> streams,
			Level7MarshalAndPing notifyListener,
			HeaderSettings localSettings
	) {
		this.maxConcurrentQueue = maxConcurrentQueue;
		this.streamsLayer = streams;
		this.notifyListener = notifyListener;
		this.localSettings = localSettings;
	}

	public XFuture<Void> sendSettingsToSocket() {
		SettingsFrame settings = HeaderSettings.createSettingsFrame(localSettings);
		return notifyListener.sendFrameToSocket(settings);
	}
	
	public XFuture<Void> sendDataToSocket(Stream stream, StreamMsg data) {
		return streamsLayer.sendDataToSocket(stream, data);
	}

	public XFuture<Void> sendRstToSocket(Stream stream, RstStreamFrame data) {
		return streamsLayer.sendRstToSocket(stream, data);
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
