/*
Copyright (c) 2002, Dean Hiller
All rights reserved.

*****************************************************************
IF YOU MAKE CHANGES TO THIS CODE AND DO NOT POST THEM, YOU 
WILL BE IN VIOLATION OF THE LICENSE I HAVE GIVEN YOU.  Contact
me at deanhiller@users.sourceforge.net if you need a different
license.
*****************************************************************

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.playorm.nio.test.nottested;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.CorruptPacketException;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.PacketListener;
import org.playorm.nio.api.libs.PacketProcessor;
import org.playorm.nio.impl.libs.ProcessingState;



public class SequenceProcessor implements PacketProcessor {
//--------------------------------------------------------------------
//	FIELDS/MEMBERS
//--------------------------------------------------------------------
	private static final Logger log = Logger.getLogger(SequenceProcessor.class.getName());
	private static final int HEADER_SIZE = Integer.SIZE/8*2;
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	
	private Object id;
	private PacketListener listener;
	private int sequenceNum = -1;
	private ProcessingState state = ProcessingState.PROCESSING_HEADER;
	
	private int maxSizeContents;
	private byte[] packetSeparator;
		
	private BufferInfo head;
	private BufferInfo body;
	private BufferInfo tail;

	private class BufferInfo {
		private boolean haveData = false;
		private ByteBuffer cache;
		private int index = 0;
		private int numNeededBytes = -1;
		private int sequence;
		public void setHaveData(boolean haveData) {
			this.haveData = haveData;
		}
		public boolean isHaveData() {
			return haveData;
		}
		public void setCache(ByteBuffer cache) {
			this.cache = cache;
		}
		public ByteBuffer getCache() {
			return cache;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public int getIndex() {
			return index;
		}
		public void setNumNeededBytes(int numNeededBytes) {
			this.numNeededBytes = numNeededBytes;
		}
		public int getNumNeededBytes() {
			return numNeededBytes;
		}
		public void setSequence(int sequence) {
			this.sequence = sequence;
		}
		public int getSequence() {
			return sequence;
		}
	}
//--------------------------------------------------------------------
//	CONSTRUCTORS
//--------------------------------------------------------------------
	public SequenceProcessor(Object id, int maxSizeContents, byte[] packetSeparator) {
		this.id = id;
		this.packetSeparator = packetSeparator;
		this.maxSizeContents = maxSizeContents;
		
		head = new BufferInfo();
		body = new BufferInfo();
		tail = new BufferInfo();
		body.setCache(ByteBuffer.allocate(maxSizeContents));
		head.setCache(ByteBuffer.allocate(HEADER_SIZE));
		tail.setCache(ByteBuffer.allocate(packetSeparator.length));
		clearState();
	}
//--------------------------------------------------------------------
//	BUSINESS METHODS
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	JAVABEANS GET/SET METHODS
//--------------------------------------------------------------------
	/**
	 * FILL IN JAVADOC HERE
	 */
	public void setPacketListener(PacketListener l) {
		listener = l;
	}

	private synchronized int getSequence() {
		return sequenceNum++;
	}
	
	public ByteBuffer processOutgoing(ByteBuffer b) {
		if(b.remaining() > maxSizeContents)
			throw new IllegalArgumentException("Cannot write out packets larger than size="+maxSizeContents);
		ByteBuffer readOnlyTail = ByteBuffer.wrap(packetSeparator).asReadOnlyBuffer();

		int seq = getSequence();

		if(log.isLoggable(Level.FINER))
			log.finer(id+"packetized-> #"+seq+" buf="+b);
		
		ByteBuffer packet = ByteBuffer.allocate(b.remaining()+readOnlyTail.remaining()+HEADER_SIZE);
		int size = b.remaining();
		packet.putInt(seq);
		packet.putInt(size);
		packet.put(b);
		packet.put(readOnlyTail);
		HELPER.doneFillingBuffer(packet);
		
		return packet;
	}
	/* (non-Javadoc)
	 * @see org.dhiller.common.bus.Listener#notify(java.lang.Object)
	 */
	public boolean incomingData(ByteBuffer b, Object passthrough) throws IOException {
		try {
			return notifyImpl(b, passthrough);
		} catch(CorruptPacketException e) {
			log.log(Level.WARNING, "Corrupt packet received", e);
			clearState();
			throw e;
		}
	}
	
// I think recovering could be a security problem...if they send bad data...kill the link
//	private void recover(ByteBuffer b) {
//		try {
//			throw new UnsupportedOperationException("recovery is not supported yet");
//			//state = RECOVERING;
//			//notifyImpl(b);
//		} catch(RuntimeException e) {
//			log.log(Level.WARNING, "Ignoring exception as it happened after another\n"
//					+"exception that we will throw back.  This exception may be\n"
//					+"a result of the first exception", e);
//		}
//	}

	public boolean notifyImpl(ByteBuffer b, Object passthrough) throws IOException {
		if (log.isLoggable(Level.FINEST))
			log.log(Level.FINEST, "processing stream");
		if(b == null)
			throw new IllegalArgumentException("evt cannot be null");

		boolean notified = false;
		
		while(b.remaining() > 0) {
			switch(state) {
				case PROCESSING_HEADER:
					processHeader(b);		
				break;
				case PROCESSING_BODY:
					processBody(b);
				break;
				case PROCESSING_TAIL:
					notified = processTail(b, passthrough);
				break;
				case RECOVERING:
					findNewTrailer(b);
				break;
				default:
				break;
			}
		}
		return notified;
	}
	
	private void processHeader(ByteBuffer b) {
		boolean isDone = processDone(b, head);

		if(isDone) {
			//get header data....
			HELPER.doneFillingBuffer(head.getCache());
			body.setSequence(head.getCache().getInt());
			body.setNumNeededBytes(head.getCache().getInt());
			state = ProcessingState.PROCESSING_BODY;
			head.getCache().clear();
			if(body.getNumNeededBytes() <= 0 || body.getNumNeededBytes() > maxSizeContents)
				throw new CorruptPacketException("header='"+body.getNumNeededBytes()+"' is not valid.  Must\n"
						+"be less than maxSizeContents="
						+maxSizeContents+" and greater than 0", true, false);
		}
	}
	private void processBody(ByteBuffer b) {
		if(processDone(b, body)) {
			state = ProcessingState.PROCESSING_TAIL;
		}
	}
	private boolean processTail(ByteBuffer b, Object passthrough) throws IOException {
		if(processDone(b, tail)) {
			state = ProcessingState.PROCESSING_HEADER;
			try {
				firePacket(passthrough);
			} finally {
				clearState();
			}
			return true;
		}
		return false;
	}
	
	private void findNewTrailer(ByteBuffer b) {
		throw new UnsupportedOperationException("recovery is not implemented yet but would be easy to do so");
	}
	
	private boolean processDone(ByteBuffer b, BufferInfo info) {

		int remain = b.remaining();
		int cacheSpace = info.getNumNeededBytes();
		int sizeToCopy = cacheSpace;
		
		if(remain <= 0)
			return info.isHaveData();
		
		if(remain < cacheSpace)
			sizeToCopy = remain;
		
		byte[] dst = info.getCache().array();
		
		if(sizeToCopy > 0) {
			info.setNumNeededBytes(info.getNumNeededBytes() - sizeToCopy);
			b.get(dst, info.getIndex(), sizeToCopy);
			int curPos = info.getCache().position();
			info.getCache().position(curPos+sizeToCopy);
			info.setIndex(info.getIndex() + sizeToCopy);
		}
		
		if(info.getNumNeededBytes() <= 0) {
			return true;
		}
		
		return false;
	}
	
	private void clearState() {
		//reset all state first....
		state = ProcessingState.PROCESSING_HEADER;
		head.setNumNeededBytes(HEADER_SIZE);
		body.setNumNeededBytes(-1);
		tail.setNumNeededBytes(packetSeparator.length);
		head.setIndex(0);
		body.setIndex(0);
		tail.setIndex(0);
		head.setHaveData(false);
		body.setHaveData(false);
		tail.setHaveData(false);
		HELPER.eraseBuffer(head.getCache());
		HELPER.eraseBuffer(body.getCache());
		HELPER.eraseBuffer(tail.getCache());
	}
	private void firePacket(Object passthrough) throws IOException {
		HELPER.doneFillingBuffer(body.getCache());

		if(log.isLoggable(Level.FINER))
			log.finer(id+"unpacketize<- #"+body.getSequence()+" b="+body.getCache());

		listener.incomingPacket(body.getCache(), passthrough);
	}
	
//--------------------------------------------------------------------
//Event Handler LIBRARY helpers
//These functions are like library functions that the event handlers above
//share to get their work done.
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	ADD LISTENERS/FIRE EVENT METHODS
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	INTERFACES/CLASSES
//--------------------------------------------------------------------
}