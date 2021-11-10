package org.webpieces.util.acking;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class ByteAckTracker {

	public ConcurrentLinkedQueue<Record> records = new ConcurrentLinkedQueue<>();
	private AtomicInteger totalBytesToAckOutstanding = new AtomicInteger(0);
	private AckMetrics metrics;

	private boolean fromSocket;

	public ByteAckTracker() {
	}
	
	public ByteAckTracker(AckMetrics metrics, boolean fromSocket) {
		this.metrics = metrics;
		this.fromSocket = fromSocket;
	}

	public XFuture<Void> addBytesToTrack(int incomingBytes) {
		if(metrics != null)
			metrics.incrementTrackedBytes(incomingBytes);

		XFuture<Void> byteFuture = new XFuture<Void>();
		records.add(new Record(incomingBytes, byteFuture));
		totalBytesToAckOutstanding.addAndGet(incomingBytes);

		return byteFuture;
	}

	private class Record {
		public int bytesToAckLeft;
		public XFuture<Void> byteFuture;

		public Record(int incomingBytes, XFuture<Void> byteFuture) {
			this.bytesToAckLeft = incomingBytes;
			this.byteFuture = byteFuture;
		}

		@Override
		public String toString() {
			return "Record [incomingBytes=" + bytesToAckLeft + "]";
		}
	}

	public Void ackBytes(int numBytes) {
		if(numBytes == 0)
			return null; //mine as well short circuit

		totalBytesToAckOutstanding.addAndGet(-numBytes);

		if(metrics != null)
			metrics.incrementAckedBytes(numBytes);
		
		while(numBytes > 0) {
			Record removedRecord;
			synchronized(this) {
				Record record = records.peek();
				if(record == null) {
					int totalToAck = totalBytesToAckOutstanding.get();
					throw new IllegalStateException("bug, misaligned client.  He acked more bytes than he added. totalToAck="+totalToAck+" numBytes="+numBytes);
				}
				if(numBytes < record.bytesToAckLeft) {
					record.bytesToAckLeft = record.bytesToAckLeft-numBytes;
					return null; //not enough bytes acked yet
				}

				numBytes = numBytes - record.bytesToAckLeft;

				removedRecord = records.poll();
			}
			
			removedRecord.byteFuture.complete(null); //ack this set of bytes
		}
		
		return null;
	}

}
