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
package org.playorm.nio.impl.libs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.CorruptPacketException;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.PacketListener;
import org.playorm.nio.api.libs.PacketProcessor;


public class HeaderTrailerProcessor implements PacketProcessor {
//--------------------------------------------------------------------
//	FIELDS/MEMBERS
//--------------------------------------------------------------------
	private static final Logger log = Logger.getLogger(HeaderTrailerProcessor.class.getName());
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	
	private PacketListener listener;
	
	private int maxSizeContents = 1000000;
	private byte[] packetSeparator;
	
	private ByteBuffer head;
	private ByteBuffer body;
	private ByteBuffer tail;

	private static final int HEADER_SIZE = Integer.SIZE/8;
	
	private ProcessingState state = ProcessingState.PROCESSING_HEADER;
	private Object id;

//--------------------------------------------------------------------
//	CONSTRUCTORS
//--------------------------------------------------------------------
	public HeaderTrailerProcessor(Object id, byte[] packetSeparator) {
		this.id = id;
		this.packetSeparator = packetSeparator;
		
		head = ByteBuffer.allocate(HEADER_SIZE);
		tail = ByteBuffer.allocate(packetSeparator.length);
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

	public ByteBuffer processOutgoing(ByteBuffer b) {
		if(b.remaining() > maxSizeContents)
			throw new IllegalArgumentException(id+"Cannot write out packets larger than size="
					+maxSizeContents+" actual size11="+b.remaining());
		ByteBuffer readOnlyTail = ByteBuffer.wrap(packetSeparator).asReadOnlyBuffer();
		
		ByteBuffer packet = ByteBuffer.allocate(b.remaining()+readOnlyTail.remaining()+4);
		int size = b.remaining();
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
			log.log(Level.WARNING, id+"Corrupt packet received", e);
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
			log.log(Level.FINEST, id+"processing stream");
		if(b == null)
			throw new IllegalArgumentException(id+" evt cannot be null");

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
		boolean isDone = HELPER.processForPacket(b, head);

		if(isDone) {
			//get header data....
			HELPER.doneFillingBuffer(head);
			int numNeededBytes = head.getInt();
			if(numNeededBytes <= 0 || numNeededBytes > maxSizeContents)
				throw new CorruptPacketException(id+"header='"+numNeededBytes+"' is not valid.  Must\n"
						+"be less than maxSizeContents11="
						+maxSizeContents+" and greater than 0", true, false);			
			body = ByteBuffer.allocate(numNeededBytes);
			body.limit(numNeededBytes);
			state = ProcessingState.PROCESSING_BODY;
			head.clear();

		}
	}
	private void processBody(ByteBuffer b) {
		if(HELPER.processForPacket(b, body)) {
			state = ProcessingState.PROCESSING_TAIL;
		}
	}
	private boolean processTail(ByteBuffer b, Object passthrough) throws IOException {
		if(HELPER.processForPacket(b, tail)) {
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
		throw new UnsupportedOperationException(id+"recovery is not implemented yet but would be easy to do so");
	}
	
	private void clearState() {
		//reset all state first....
		state = ProcessingState.PROCESSING_HEADER;

		HELPER.eraseBuffer(head);
		HELPER.eraseBuffer(tail);
		body = null;
	}
	private void firePacket(Object passthrough) throws IOException {
		HELPER.doneFillingBuffer(body);

		listener.incomingPacket(body, passthrough);
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