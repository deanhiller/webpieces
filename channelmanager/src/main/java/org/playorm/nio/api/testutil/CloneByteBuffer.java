package org.playorm.nio.api.testutil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.libs.BufferHelper;

import biz.xsoftware.mock.CloningBehavior;

public class CloneByteBuffer implements CloningBehavior {

	private static final Logger log = Logger.getLogger(CloneByteBuffer.class.getName());
	
	private static BufferHelper helper = ChannelServiceFactory.bufferHelper(null);

	public Object[] incomingDataCloner(DatagramChannel channel, InetSocketAddress addr, ByteBuffer b) {
		
		return new Object[] { channel, addr, clone(b) };
	}
	public void incomingData(DatagramChannel channel, InetSocketAddress addr, ByteBuffer b) {
	}	
	
	
	public Object[] writeCloner(ByteBuffer b) {
		return new Object[] { cloneWithoutModify(b) };
	}
	public int write(ByteBuffer b) {
		int remain = b.remaining();
		//make it look like the buffer was read completely like the channel would really do...
		b.position(b.limit());
	
		return remain;
	}

	public void packetEncrypted(ByteBuffer b, Object o) {
	}
	public Object[] packetEncryptedCloner(ByteBuffer b, Object o) {
		return new Object[] { clone(b), o };
	}
	public void packetUnencrypted(ByteBuffer out) {
	}
	public Object[] packetUnencryptedCloner(ByteBuffer out) {
		return new Object[] { clone(out) };
	}
	
	public void incomingPacket(ByteBuffer b, Object obj) {
	}
	public Object[] incomingPacketCloner(ByteBuffer b, Object obj) {
		return new Object[] { clone(b), obj};
	}
	
	public void incomingData(Channel channel, DataChunk chunk) {	
		//log.info("b.rem="+b.remaining());
		chunk.setProcessed("CloneByteBuffer");
	}
	
	public Object[] incomingDataCloner(Channel channel, DataChunk chunk) {
		ByteBuffer b = chunk.getData();
		return new Object[] { channel, clone(b) };
	}
//	private static final Logger log = Logger.getLogger(CloneByteBuffer.class.getName());
	
	public static Object clone(ByteBuffer b) {
		ByteBuffer newOne = ByteBuffer.allocate(b.remaining());
		newOne.put(b);
		helper.doneFillingBuffer(newOne);
		return newOne;
	}
	
	public static Object cloneWithoutModify(ByteBuffer b) {
		ByteBuffer newOne = ByteBuffer.allocate(b.remaining());
		byte[] backing = b.array();
		newOne.put(backing, b.position(), b.remaining());
		helper.doneFillingBuffer(newOne);
		
		return newOne;
	}

}
