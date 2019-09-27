package org.webpieces.nio.api.throughput;

import java.util.Timer;
import java.util.TimerTask;

import org.webpieces.nio.api.channels.TCPChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytesRecorder {

	private static final Logger log = LoggerFactory.getLogger(BytesRecorder.class);
	private Timer timer = new Timer();
	
	private long totalBytes = 0;
	private long totalBytesLastRount;
	private long lastTime;
	
	private void logBytesTxfrd() {
		long bytesTxfrd = getBytes();
		long bytesThisRound = bytesTxfrd - totalBytesLastRount;
		totalBytesLastRount = bytesTxfrd;

		long now = System.currentTimeMillis();
		long roundTime = now - lastTime;
		lastTime = now;
		long roundBytesPerMs = bytesThisRound / roundTime;
		double megaRoundPerMs = ((double)roundBytesPerMs) / 1_000_000;
		double megaRoundPerSec = megaRoundPerMs * 1000;
		log.info("this round="+megaRoundPerSec+"MBytes/Sec.  bytesThisRound=" +bytesThisRound+" totalBytes="+bytesTxfrd+" time="+roundTime);
	}
	
	private synchronized long getBytes() {
		return totalBytes;
	}

	public synchronized void recordBytes(int size) {
		totalBytes += size;
	}
	
	public void start() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				logBytesTxfrd();
			}
		}, 1000, 5000);		
	}

	public void setClientChannel(TCPChannel channel) {
	}

	public void setServerChannel(TCPChannel proxy) {
	}

	public void markTime() {
		lastTime = System.currentTimeMillis();
	}
}
