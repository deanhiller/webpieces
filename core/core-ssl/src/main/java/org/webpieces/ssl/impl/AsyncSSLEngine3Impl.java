package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;

import org.webpieces.data.api.BufferPool;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLEngineException;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.ssl.api.SslListener;
import org.webpieces.util.acking.ByteAckTracker;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class AsyncSSLEngine3Impl implements AsyncSSLEngine {
	private static final Logger log = LoggerFactory.getLogger(AsyncSSLEngine2Impl.class);
	
	private BufferPool pool;
	private SslMementoImpl mem;
	private SslListener listener;
	private Object wrapLock = new Object();
	private boolean clientInitiated;
	private boolean sslEngineIsFarting = false;

	private AtomicBoolean fireClosed = new AtomicBoolean(false);
	private AtomicBoolean fireConnected = new AtomicBoolean(false);
	
	private ByteAckTracker encryptionTracker = new ByteAckTracker();
	private ByteAckTracker decryptionTracker = new ByteAckTracker();
	
	public AsyncSSLEngine3Impl(String loggingId, SSLEngine engine, BufferPool pool, SslListener listener) {
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null");
		this.pool = pool;
		this.listener = listener;
		ByteBuffer cachedOutBuffer = pool.nextBuffer(engine.getSession().getApplicationBufferSize());
		this.mem = new SslMementoImpl(loggingId, engine, cachedOutBuffer);
	}
	
	@Override
	public CompletableFuture<Void> beginHandshake() {
		mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);
		SSLEngine sslEngine = mem.getEngine();
		
		log.trace(()->mem+"start handshake");
		try {
			sslEngine.beginHandshake();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}

		if(sslEngine.getHandshakeStatus() != HandshakeStatus.NEED_WRAP)
			throw new IllegalStateException("Dude, WTF, after beginHandshake, SSLEngine has to be NEED WRAP to send first hello message");

		return doHandshakeLoop();
	}

	@Override
	public CompletableFuture<Void> feedEncryptedPacket(ByteBuffer encryptedInData) {
		CompletableFuture<Void> future = decryptionTracker.addBytesToTrack(encryptedInData.remaining());

		ByteBuffer cached = mem.getCachedToProcess();
		ByteBuffer newEncryptedData = combine(cached, encryptedInData);
		mem.setCachedEncryptedData(newEncryptedData);

		mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);


		//This is a bit complex to allow backpressure through the SSL Layer.  If not enough CompletableFutures
		//are resolved, the lower layers turn off the socket(deregister from selector) until quite a few are
		//resolved and we catch up.  This prevents the server from tanking under load ;).  Yes, it's pretty
		//fucking sick!!!  well, that's my opinion since I had fun adding shit that you'll never know about.
		doWork();
		
		return future;
	}

	private ByteBuffer combine(ByteBuffer cachedToProcessLaterData, ByteBuffer encryptedData) {
		int size = cachedToProcessLaterData.remaining()+encryptedData.remaining();
		ByteBuffer nextBuffer = pool.nextBuffer(size);
		nextBuffer.put(cachedToProcessLaterData);
		nextBuffer.put(encryptedData);
		nextBuffer.flip();
		
		pool.releaseBuffer(cachedToProcessLaterData);
		pool.releaseBuffer(encryptedData);

		return nextBuffer;
	}
	
	private void doWork() {
		SSLEngine engine = mem.getEngine();

		//keep doing work NEED_UNWRAP, NEED_WRAP, NEED_TASK until all done and return all the futures as one
		//When all futures are done, they will ack this one message that came in
		while(true && !sslEngineIsFarting) {
			HandshakeStatus hsStatus = engine.getHandshakeStatus();

			if(needUnwrap(hsStatus)) {
				boolean isNotEnoughData = unwrapPacket();
				if(isNotEnoughData)
					break;
			} else if(hsStatus == HandshakeStatus.NEED_TASK || hsStatus == HandshakeStatus.NEED_WRAP) {
				doHandshakeWork();
			} else {
				break;
			}
		}

		sslEngineIsFarting = false; //it's done farting
	}

	private boolean needUnwrap(HandshakeStatus hsStatus) {
		return (hsStatus == HandshakeStatus.NOT_HANDSHAKING || hsStatus == HandshakeStatus.NEED_UNWRAP)
				&& mem.getCachedToProcess().hasRemaining();
	}

	private CompletableFuture<Void> doHandshakeWork() {
		SSLEngine engine = mem.getEngine();
		HandshakeStatus hsStatus = engine.getHandshakeStatus();
		if(hsStatus == HandshakeStatus.NEED_TASK) {
			return createRunnable();	
		} else if(hsStatus == HandshakeStatus.NEED_WRAP) {
			return sendHandshakeMessage();
		}
		
		throw new UnsupportedOperationException("need to support state="+hsStatus);
	}

	private boolean unwrapPacket() {
		SSLEngine sslEngine = mem.getEngine();
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		Status status = null;

		logTrace1(mem.getCachedToProcess(), hsStatus);
		
		ByteBuffer encryptedData = mem.getCachedToProcess();
	
		SSLEngineResult result;
		
		ByteBuffer cachedOutBuffer = pool.nextBuffer(sslEngine.getSession().getApplicationBufferSize());

		int remainBeforeDecrypt = encryptedData.remaining();
		try {
			result = sslEngine.unwrap(encryptedData, cachedOutBuffer);
		} catch(SSLException e) {
			AsyncSSLEngineException ee = new AsyncSSLEngineException(
					"before exception status="+status+" hsStatus="+hsStatus+" b="+encryptedData, e);
			throw ee;
		}
		
		status = result.getStatus();
		hsStatus = result.getHandshakeStatus();

		if(status == Status.CLOSED) {
			//If we are connected, then the close was remotely initiated
			mem.compareSet(ConnectionState.CONNECTED, ConnectionState.DISCONNECTING);

			//If we are NOT connected and are in Disconnecting state, then fireClose
			if(mem.getConnectionState() == ConnectionState.DISCONNECTING)
				fireClose();
		}

		logAndCheck(encryptedData, result, cachedOutBuffer, status, hsStatus);
		logTrace(encryptedData, status, hsStatus);

		int totalBytesToAck = remainBeforeDecrypt - encryptedData.remaining();
		if(cachedOutBuffer.position() != 0) {
			cachedOutBuffer.flip();
			listener.packetUnencrypted(cachedOutBuffer).handle((v, t) -> {
				if(t != null)
					log.error("Exception in ssl listener", t);
				
				//after the client consumes the packet(completes the future), ack the number of bytes corresponding
				//to the packet we sent to the consumer.  NOTE: We do NOT care about how many bytes we sent the
				//consumer BUT only how many encrypted bytes there were to ack those bytes and release our nio library to
				//keep processing.  (in this way, the lower layer shuts off the peer from writing to us if your app gets slow)
				decryptionTracker.ackBytes(totalBytesToAck);
				return null;
			});
		} else {
			cachedOutBuffer.position(cachedOutBuffer.limit()); //pretend like all data is consumed
			pool.releaseBuffer(cachedOutBuffer); //buffer not needed since we did not use it and fill it with data
		
			if(hsStatus == HandshakeStatus.NEED_WRAP || hsStatus == HandshakeStatus.NEED_TASK){
				//THIS IS COMPLEX HERE.  In this case, we need to run a task and/or wrap data and need to
				//ack the bytes we just processed ONLY if those things get sent out.  If the NIC is putting backpressure
				//this then backpressures the incoming side until the peer can consumer our data first
				doHandshakeLoop().handle((v, t) -> {
					if(t != null)
						log.error("Exception in ssl listener", t);
					
					decryptionTracker.ackBytes(totalBytesToAck);
					return null;
				});
				
			} else {
				//The engine consumed the bytes so we are done with them, ack that payload as consumed.
				decryptionTracker.ackBytes(totalBytesToAck);
			}
		}
						
		if(encryptedData.hasRemaining()) {
			mem.setCachedEncryptedData(encryptedData);
		} else {
			mem.setCachedEncryptedData(SslMementoImpl.EMPTY);
			pool.releaseBuffer(encryptedData);
		}

		if(hsStatus == HandshakeStatus.FINISHED)
			fireLinkEstablished();

		if(status == Status.BUFFER_UNDERFLOW)
			return true;

		return false;
	}

	private CompletableFuture<Void> doHandshakeLoop() {
		SSLEngine engine = mem.getEngine();
		HandshakeStatus hsStatus = engine.getHandshakeStatus();

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		while((hsStatus == HandshakeStatus.NEED_WRAP && !sslEngineIsFarting) || hsStatus == HandshakeStatus.NEED_TASK) {
			CompletableFuture<Void> future = doHandshakeWork();
			futures.add(future);
			hsStatus = engine.getHandshakeStatus();
		}

		sslEngineIsFarting = false;
		CompletableFuture<Void> futureAll = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		return futureAll;
	}

	private void logTrace1(ByteBuffer encryptedInData, HandshakeStatus hsStatus) {
		log.trace(()->mem+"[sockToEngine] going to unwrap pos="+encryptedInData.position()+
					" lim="+encryptedInData.limit()+" hsStatus="+hsStatus+" cached="+mem.getCachedToProcess());
	}
	
	private void logTrace(ByteBuffer encryptedData, Status status, HandshakeStatus hsStatus) {
		log.trace(()->mem+"[sockToEngine] reset pos="+encryptedData.position()+" lim="+encryptedData.limit()+" status="+status+" hs="+hsStatus);
	}
	
	private void logAndCheck(ByteBuffer encryptedData, SSLEngineResult result, ByteBuffer outBuffer, Status status, HandshakeStatus hsStatus) {
		final ByteBuffer data = encryptedData;

		log.trace(()->mem+"[sockToEngine] unwrap done pos="+data.position()+" lim="+
					data.limit()+" status="+status+" hs="+hsStatus);
		
		if(status == Status.BUFFER_UNDERFLOW) {
			final ByteBuffer data1 = encryptedData;
			log.warn("buffer underflow. data="+data1.remaining());
		}
	}

	private CompletableFuture<Void> createRunnable() {
		SSLEngine sslEngine = mem.getEngine();
		Runnable r = sslEngine.getDelegatedTask();
		r.run(); //could offload to threadpool but not sure it buys us anything so KISS or YAGNI until later
		return CompletableFuture.completedFuture(null);
	}
	
	private CompletableFuture<Void> sendHandshakeMessage() {
		try {
			return sendHandshakeMessageImpl();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
	}
	
	private CompletableFuture<Void> sendHandshakeMessageImpl() throws SSLException {
		SSLEngine sslEngine = mem.getEngine();
		log.trace(()->mem+"sending handshake message");
		//HELPER.eraseBuffer(empty);
		
		ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());
			
		//CLOSE and all the threads that call feedPlainPacket can have contention on wrapping to encrypt and
		//must synchronize on sslEngine.wrap
		Status lastStatus;
		HandshakeStatus hsStatus;
		synchronized (wrapLock ) {

			HandshakeStatus beforeWrapHandshakeStatus = sslEngine.getHandshakeStatus();
			if (beforeWrapHandshakeStatus != HandshakeStatus.NEED_WRAP)
				throw new IllegalStateException("we should only be calling this method when hsStatus=NEED_WRAP.  hsStatus=" + beforeWrapHandshakeStatus);

			//KEEEEEP This very small.  wrap and then listener.packetEncrypted
			SSLEngineResult result = sslEngine.wrap(SslMementoImpl.EMPTY, engineToSocketData);
			lastStatus = result.getStatus();
			hsStatus = result.getHandshakeStatus();
		}
		
		{
			final Status lastStatus2 = lastStatus;
			final HandshakeStatus hsStatus2 = hsStatus;
			log.trace(()->mem+"write packet pos="+engineToSocketData.position()+" lim="+
						engineToSocketData.limit()+" status="+lastStatus2+" hs="+hsStatus2);
		}
		if(lastStatus == Status.BUFFER_OVERFLOW || lastStatus == Status.BUFFER_UNDERFLOW)
			throw new RuntimeException("status not right, status="+lastStatus+" even though we sized the buffer to consume all?");

		boolean readNoData = engineToSocketData.position() == 0;
		engineToSocketData.flip();
		try {
			CompletableFuture<Void> sentMsgFuture;
			if(readNoData) {
				log.trace(() -> "ssl engine is farting. READ 0 data.  hsStatus=\"+hsStatus+\" status=\"+lastStatus");
				//A big hack since the Engine was not working in live testing with FireFox and it would tell us to wrap
				//and NOT output any data AND not BufferOverflow.....you have to do 1 or the other, right
				//instead cut out of looping since there seems to be no data
				sslEngineIsFarting = true;
				sentMsgFuture = CompletableFuture.completedFuture(null);
			} else
				sentMsgFuture = listener.sendEncryptedHandshakeData(engineToSocketData);

			if (lastStatus == Status.CLOSED && !clientInitiated) {
				fireClose();
			} else if (hsStatus == HandshakeStatus.FINISHED) {
				fireLinkEstablished();
			}

			return sentMsgFuture;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private void fireClose() {
		//fire only ONCE...
		boolean shouldFire = fireClosed.compareAndSet(false, true);
		if(shouldFire) {
			mem.compareSet(ConnectionState.DISCONNECTING, ConnectionState.DISCONNECTED);
			listener.closed(clientInitiated);
		}
	}
	
	private void fireLinkEstablished() {
		boolean shouldFire = fireConnected.compareAndSet(false, true);
		if(shouldFire) {
			mem.compareSet(ConnectionState.CONNECTING, ConnectionState.CONNECTED);
			listener.encryptedLinkEstablished();
		}
	}
	
	@Override
	public CompletableFuture<Void> feedPlainPacket(ByteBuffer buffer) {
		try {
			return feedPlainPacketImpl(buffer);
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
	}

	public CompletableFuture<Void> feedPlainPacketImpl(ByteBuffer buffer) throws SSLException {
		if(mem.getConnectionState() != ConnectionState.CONNECTED)
			throw new IllegalStateException(mem+" SSLEngine is not connected right now");
		else if(!buffer.hasRemaining())
			throw new IllegalArgumentException("your buffer has no readable data");
		
		SSLEngine sslEngine = mem.getEngine();
		log.trace(()->mem+"feedPlainPacket [in-buffer] pos="+buffer.position()+" lim="+buffer.limit());
		
		CompletableFuture<Void> future = encryptionTracker.addBytesToTrack(buffer.remaining());
		
		while(buffer.hasRemaining()) {
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());

			int remainBefore = buffer.remaining();
			int numEncrypted;
			SSLEngineResult result;
			synchronized(wrapLock) {
				result = sslEngine.wrap(buffer, engineToSocketData);
				numEncrypted = remainBefore - buffer.remaining();				
			}
			
			Status status = result.getStatus();
			HandshakeStatus hsStatus = result.getHandshakeStatus();
			if(status != Status.OK)
				throw new RuntimeException("Bug, status="+status+" instead of OK.  hsStatus="+
						hsStatus+" Something went wrong and we could not encrypt the data");

			log.trace(()->mem+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
						" lim="+engineToSocketData.limit()+" hsStatus="+hsStatus+" status="+status);
			
			engineToSocketData.flip();
			listener.packetEncrypted(engineToSocketData).handle( (v, t) -> {
				if(t != null)
					log.error("Exception from ssl listener", t);
				encryptionTracker.ackBytes(numEncrypted);
				return null;
			});
		}

		pool.releaseBuffer(buffer);
		
		return future;
	}
	
	@Override
	public void close() {
		clientInitiated = true;
		if(mem.getConnectionState() == ConnectionState.NOT_STARTED) {
			listener.closed(true);
			return;
		}
		
		mem.compareSet(ConnectionState.CONNECTED, ConnectionState.DISCONNECTING);
		
		SSLEngine engine = mem.getEngine();
		engine.closeOutbound();
		
		HandshakeStatus status = engine.getHandshakeStatus();
		switch (status) {
			case NEED_WRAP:
				doHandshakeLoop();
				break;
			case NOT_HANDSHAKING:
				if(ConnectionState.DISCONNECTED != mem.getConnectionState())
					throw new IllegalStateException("state="+mem.getConnectionState()+" hsStatus="+status+" should not be able to occur");
				break;
			default:
				//we WILL hit this and need to fix if other end closes...try closing both ends!!!
				throw new RuntimeException(mem+"bug, status not handled in close="+status);
		}
	}

	@Override
	public ConnectionState getConnectionState() {
		return mem.getConnectionState();
	}

}
