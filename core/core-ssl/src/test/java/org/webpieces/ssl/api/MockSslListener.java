package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

public class MockSslListener extends MockSuperclass implements SslListener {

	public boolean connected;
	public boolean closed;
	public boolean clientInitiated;
	
	enum Method implements MethodEnum {
		ENCRYPTED, ENCRYPTED_HANDSHAKE, DECRYPTED,
	}
	
	class BufferedFuture {
		public XFuture<Void> future;
		public ByteBuffer engineToSocketData;

		public BufferedFuture(XFuture<Void> future, ByteBuffer engineToSocketData) {
			this.future = future;
			this.engineToSocketData = engineToSocketData;
		}
	}
	
	@Override
	public void encryptedLinkEstablished() {
		if(connected)
			throw new RuntimeException("bug in implementation.  we should not fire connected twice");
		connected = true;
	}

	@Override
	public XFuture<Void> packetEncrypted(ByteBuffer engineToSocketData) {
		XFuture<Void> future = new XFuture<Void>();
		super.calledVoidMethod(Method.ENCRYPTED, new BufferedFuture(future, engineToSocketData));
		return future;
	}

	@Override
	public XFuture<Void> sendEncryptedHandshakeData(ByteBuffer engineToSocketData) {
		XFuture<Void> future = new XFuture<Void>();
		super.calledVoidMethod(Method.ENCRYPTED_HANDSHAKE, new BufferedFuture(future, engineToSocketData));
		return future;
	}
	
	@Override
	public XFuture<Void> packetUnencrypted(ByteBuffer out) {
		XFuture<Void> future = new XFuture<Void>();
		super.calledVoidMethod(Method.DECRYPTED, new BufferedFuture(future, out));
		return future;
	}

	@Override
	public void closed(boolean clientInitiated) {
		if(closed)
			throw new RuntimeException("bug in implementation.  we should not fire closed twice");
		closed = true;
		this.clientInitiated = clientInitiated;
	}

	public List<BufferedFuture> getEncrypted() {
		Stream<BufferedFuture> map = super.getCalledMethods(Method.ENCRYPTED).map(s -> (BufferedFuture)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}

	public List<BufferedFuture> getHandshake() {
		Stream<BufferedFuture> map = super.getCalledMethods(Method.ENCRYPTED_HANDSHAKE).map(s -> (BufferedFuture)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}
	
	public List<BufferedFuture> getDecrypted() {
		Stream<BufferedFuture> map = super.getCalledMethods(Method.DECRYPTED).map(s -> (BufferedFuture)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}

	public BufferedFuture getSingleDecrypted() {
		List<BufferedFuture> list = getDecrypted();
		if(list.size() != 1)
			throw new IllegalArgumentException("not exactly 1.  size="+list.size());
		return list.get(0);
	}
	
	public BufferedFuture getSingleHandshake() {
		List<BufferedFuture> list = getHandshake();
		if(list.size() != 1)
			throw new IllegalArgumentException("not exactly 1 called.  size="+list.size());
		return list.get(0);
	}

}
