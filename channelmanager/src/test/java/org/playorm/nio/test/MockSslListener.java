package org.playorm.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.playorm.nio.api.libs.SSLListener;


public class MockSslListener implements SSLListener {

	private List<ByteBuffer> encryptedPackets = new ArrayList<ByteBuffer>();
	private List<ByteBuffer> unencryptedPackets = new ArrayList<ByteBuffer>();
	private Runnable runnable;
	private boolean isEncryptedLinkEstablished;
	private boolean isClosed;

	@Override
	public void encryptedLinkEstablished() throws IOException {
		isEncryptedLinkEstablished = true;
	}

	@Override
	public void packetEncrypted(ByteBuffer engineToSocketData,
			Object passThrough) throws IOException {
		ByteBuffer temp = ByteBuffer.allocate(engineToSocketData.remaining());
		temp.put(engineToSocketData);
		temp.flip();
		encryptedPackets.add(temp);
	}

	@Override
	public void packetUnencrypted(ByteBuffer out, Object passThrough) throws IOException {
		ByteBuffer temp = ByteBuffer.allocate(out.remaining());
		temp.put(out);
		temp.flip();
		unencryptedPackets.add(temp);
	}

	@Override
	public void runTask(Runnable r) {
		this.runnable = r;
	}

	@Override
	public void closed(boolean clientInitiated) {
		isClosed = true;
	}

	public ByteBuffer getPacketEncrypted() {
		return encryptedPackets.remove(0);
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public List<ByteBuffer> getAllPackets() {
		List<ByteBuffer> temp = encryptedPackets;
		encryptedPackets = new ArrayList<ByteBuffer>();
		return temp;
	}

	public boolean isEncryptedLinkEstablished() {
		return isEncryptedLinkEstablished;
	}

	public ByteBuffer getPacketUnencrypted() {
		return unencryptedPackets.remove(0);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public ByteBuffer getAssembledUnencryptedPacket() {
		//in some cases, ssl engine may spit out 3 packets instead of 1 for the total packet
		ByteBuffer temp = ByteBuffer.allocate(5000);
		for(ByteBuffer b : unencryptedPackets) {
			temp.put(b);
		}
		unencryptedPackets.clear();
		temp.flip();
		return temp;
	}

}
