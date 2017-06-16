package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ByteAckTracker {

	public ConcurrentLinkedQueue<Record> records = new ConcurrentLinkedQueue<>();
	private AtomicInteger numberBytesToAck = new AtomicInteger(0);
	
	public AckAggregator createTracker(int incomingBytes, int numAcksNeeded, int totalBytesParsed) {
		CompletableFuture<Void> byteFuture = new CompletableFuture<Void>();
		records.add(new Record(incomingBytes, byteFuture));
		
		CompletableFuture<Void> allAcksReceived = new CompletableFuture<Void>();
		AckAggregator ack = new AckAggregator(byteFuture, numAcksNeeded, allAcksReceived);
		allAcksReceived.thenApply(v -> ackParsedBytes(totalBytesParsed));
		
		return ack;
	}

	private class Record {
		public int incomingBytes;
		public CompletableFuture<Void> byteFuture;

		public Record(int incomingBytes, CompletableFuture<Void> byteFuture) {
			this.incomingBytes = incomingBytes;
			this.byteFuture = byteFuture;
		}

		@Override
		public String toString() {
			return "Record [incomingBytes=" + incomingBytes + "]";
		}
	}

	public Void ackParsedBytes(int numBytes) {
		numberBytesToAck.addAndGet(numBytes);
		
		while(true) {
			Record removedRecord;
			synchronized(this) {
				Record record = records.peek();
				if(record == null)
					return null;
				int num = numberBytesToAck.get();
				if(num < record.incomingBytes) {
					return null; //not enough bytes acked yet
				}
				
				numberBytesToAck.addAndGet(-record.incomingBytes);

				removedRecord = records.poll();
			}
			
			removedRecord.byteFuture.complete(null); //ack this set of bytes
		}
	}

}
