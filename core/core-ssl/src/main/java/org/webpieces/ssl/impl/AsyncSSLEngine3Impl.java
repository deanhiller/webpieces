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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.TwoPools;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLEngineException;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.ssl.api.SSLMetrics;
import org.webpieces.ssl.api.SslListener;
import org.webpieces.util.acking.ByteAckTracker;

public class AsyncSSLEngine3Impl implements AsyncSSLEngine {
	private static final Logger log = LoggerFactory.getLogger(AsyncSSLEngine3Impl.class);
	
	private BufferPool pool;
	private SslMementoImpl mem;
	private SslListener listener;
	private Object wrapLock = new Object();
	private boolean clientInitiated;
	private boolean sslEngineIsFarting = false;

	private AtomicBoolean fireClosed = new AtomicBoolean(false);
	private AtomicBoolean fireConnected = new AtomicBoolean(false);
	
	private ByteAckTracker encryptionTracker; 
	private ByteAckTracker decryptionTracker;

	private SSLMetrics metrics;
	
	public AsyncSSLEngine3Impl(String loggingId, SSLEngine engine, BufferPool pool, SslListener listener, SSLMetrics metrics) {
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null");
		encryptionTracker = new ByteAckTracker(metrics.getEncryptionAckMetrics());
		decryptionTracker = new ByteAckTracker(metrics.getDecryptionAckMetrics());
		this.metrics = metrics;
		this.pool = pool;
		this.listener = listener;
		ByteBuffer cachedOutBuffer = pool.nextBuffer(engine.getSession().getApplicationBufferSize());
		this.mem = new SslMementoImpl(loggingId, engine, cachedOutBuffer);
	}
	
	@Override
	public CompletableFuture<Void> beginHandshake() {
		mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);
		SSLEngine sslEngine = mem.getEngine();
		
		if(log.isTraceEnabled())
			log.trace(mem+"start handshake");
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
		metrics.recordEncryptedBytesFromSocket(encryptedInData.remaining());
		CompletableFuture<Void> future = decryptionTracker.addBytesToTrack(encryptedInData.remaining());

		ByteBuffer cached = mem.getCachedToProcess();
		ByteBuffer newEncryptedData = combine(cached, encryptedInData);
		mem.setCachedEncryptedData(newEncryptedData);

		boolean justStarted = mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);


		//This is a bit complex to allow backpressure through the SSL Layer.  If not enough CompletableFutures
		//are resolved, the lower layers turn off the socket(deregister from selector) until quite a few are
		//resolved and we catch up.  This prevents the server from tanking under load ;).  Yes, it's pretty
		//fucking sick!!!  well, that's my opinion since I had fun adding shit that you'll never know about.
		doWork(justStarted);
		
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
	
	private void doWork(boolean justStarted) {
		SSLEngine engine = mem.getEngine();

		//keep doing work NEED_UNWRAP, NEED_WRAP, NEED_TASK until all done and return all the futures as one
		//When all futures are done, they will ack this one message that came in
		while(true && !sslEngineIsFarting) {
			HandshakeStatus hsStatus = engine.getHandshakeStatus();

			if(needUnwrap(justStarted, hsStatus)) {
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

	private boolean needUnwrap(boolean justStarted, HandshakeStatus hsStatus) {
		//1. When engine starts and other end begins handshake, we are in NOT_HANDSHAKING and need to unwrap.
		//2. AFTER handshake, we are in the NOT_HANDSHAKING state and need to unwrap on encrypted data coming in		
		return (hsStatus == HandshakeStatus.NOT_HANDSHAKING || hsStatus == HandshakeStatus.NEED_UNWRAP) && mem.getCachedToProcess().hasRemaining();
	}

	private CompletableFuture<Void> doHandshakeWork() {
		SSLEngine engine = mem.getEngine();
		HandshakeStatus hsStatus = engine.getHandshakeStatus();
		if(hsStatus == HandshakeStatus.NEED_TASK) {
			return createRunnable();	
		} else if(hsStatus == HandshakeStatus.NEED_WRAP) {
			return sendHandshakeMessage(engine);
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
			cachedOutBuffer.position(cachedOutBuffer.limit()); //simulate consuming all data
			pool.releaseBuffer(cachedOutBuffer);
			encryptedData.position(encryptedData.limit()); //simulate consuming all data
			pool.releaseBuffer(encryptedData);

			String message = e.getMessage();
			if(message.contains("Received fatal alert: certificate_unknown")) {
				//This is normal for self signed certs, so just return.  Chrome closes the connection with
				//a reason and SSLEngine throws an exception :(
				mem.compareSet(ConnectionState.CONNECTING, ConnectionState.DISCONNECTED);
				return true;
			}

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

		//If we are in a state of NOT_HANDSHAKING, sometimes the bytes coming off are not enough for a FULL packet so BUFFER_UNDERFLOW will occur
		//This is normal
		logTrace(encryptedData, status, hsStatus);

		int totalBytesToAck = remainBeforeDecrypt - encryptedData.remaining();
		if(cachedOutBuffer.position() != 0) {
			firePlainPacketToListener(cachedOutBuffer, totalBytesToAck);
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

	private void firePlainPacketToListener(ByteBuffer cachedOutBuffer, int totalBytesToAck) {
		cachedOutBuffer.flip();
		metrics.recordPlainBytesToClient(cachedOutBuffer.remaining());

		if(pool instanceof TwoPools) {
			//IF we are using two pools(ONE for the large large buffers sslEngine requires even though it only spits
			//out like 1389 bytes, then do this to release the large buffer as we only have so many large buffers
			//(we don't want too many large buffers as that eats up memory very fast)
			if(cachedOutBuffer.remaining() <= pool.getSuggestedBufferSize()) {
				ByteBuffer largeSslBuffer = cachedOutBuffer;
				cachedOutBuffer = pool.nextBuffer(largeSslBuffer.remaining());
				cachedOutBuffer.put(largeSslBuffer);
				cachedOutBuffer.flip();
				pool.releaseBuffer(largeSslBuffer);
			}
		}

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
		if(log.isTraceEnabled())
			log.trace(mem+"[sockToEngine] going to unwrap pos="+encryptedInData.position()+
							" lim="+encryptedInData.limit()+" hsStatus="+hsStatus+" cached="+mem.getCachedToProcess());
	}
	
	private void logTrace(ByteBuffer encryptedData, Status status, HandshakeStatus hsStatus) {
		if(log.isTraceEnabled())
			log.trace(mem+"[sockToEngine] reset pos="+encryptedData.position()+" lim="+encryptedData.limit()+" status="+status+" hs="+hsStatus);
	}

	private CompletableFuture<Void> createRunnable() {
		SSLEngine sslEngine = mem.getEngine();	   
		Runnable r = sslEngine.getDelegatedTask();
		
		//could offload to YET another threadpool but not sure it buys us anything so KISS or YAGNI until later
		r.run(); 
		//after all, we run in a pretty damn nice thread pool already which is N threads to Y sockets and data
		//coming in from any socket 'may' run in any thread without a race condition(don't ask, it's complicated
		//but feel free to go look)
		
		return CompletableFuture.completedFuture(null);
	}
	
	private CompletableFuture<Void> sendHandshakeMessage(SSLEngine engine) {
		try {
			return sendHandshakeMessageImpl(engine);
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
	}
	
	private CompletableFuture<Void> sendHandshakeMessageImpl(SSLEngine engine) throws SSLException {
		SSLEngine sslEngine = mem.getEngine();
		if(log.isTraceEnabled())
			log.trace(mem+"sending handshake message");
		
		ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());
			
		//CLOSE and all the threads that call feedPlainPacket can have contention on wrapping to encrypt and
		//must synchronize on sslEngine.wrap
		Status lastStatus;
		HandshakeStatus hsStatus;
		HandshakeStatus beforeWrapHandshakeStatus;
		synchronized (wrapLock ) {
			//this is in the sync block, so we are synchronized with the engine state!!! and get the actual
			//state before calling wrap so we know the engine can't be switching states on us in another thread.
			beforeWrapHandshakeStatus = sslEngine.getHandshakeStatus();
			HandshakeStatus otherStatus = engine.getHandshakeStatus();
			if (beforeWrapHandshakeStatus != HandshakeStatus.NEED_WRAP)
				throw new IllegalStateException("we should only be calling this method when hsStatus=NEED_WRAP.  hsStatus=" 
							+ beforeWrapHandshakeStatus+" otherStat="+otherStatus+" eng1="+sslEngine+" eng2="+engine);

			//KEEEEEP This very small.  wrap and then listener.packetEncrypted
			SSLEngineResult result = sslEngine.wrap(SslMementoImpl.EMPTY, engineToSocketData);
			lastStatus = result.getStatus();
			hsStatus = result.getHandshakeStatus();
		}
		
		if(log.isTraceEnabled())
		log.trace(mem+"write packet pos="+engineToSocketData.position()+" lim="+
								engineToSocketData.limit()+" status="+lastStatus+" hs="+hsStatus);
			
		if(lastStatus == Status.BUFFER_OVERFLOW || lastStatus == Status.BUFFER_UNDERFLOW)
			throw new RuntimeException("status not right, status="+lastStatus+" even though we sized the buffer to consume all?");

		boolean readNoData = engineToSocketData.position() == 0;
		engineToSocketData.flip();
		try {
			CompletableFuture<Void> sentMsgFuture;
			if(readNoData) {
				if(log.isTraceEnabled())
					log.trace("ssl engine is farting. READ 0 data.  hsStatus="+hsStatus+" status="+lastStatus+" previous="+beforeWrapHandshakeStatus);
				
				//Ok, I updated this thread https://stackoverflow.com/questions/56707024/java-sslengine-says-need-wrap-call-wrap-and-still-need-wrap/56822673#56822673
				//but basicaly, turning on -Djavax.net.debug=ssl:handshake:verbose:keymanager:trustmanager -Djava.security.debug=access:stack
				//REALLY REALLY helps and you see that clients send a warning close_notify but I put logs before and after the call to
				//sslEngine.wrap and between those logs, the sslEngine logged ZERO debug information and did NOT generate it's 
				//response close_notify :(.  crappy sslEngine.  This was in jdk 11.0.3 :(.
				//On top of this, downgrading to jdk1.8.0_111 which uses TLSv1.2 instead of TLSv1.3 works
				//just fine and sends back the close messages while jdk 11 is not doing that
				//so this sslEngineIsFarting is covering up their bug.
				
				sslEngineIsFarting = true;
				sentMsgFuture = CompletableFuture.completedFuture(null);
			} else
				metrics.recordEncryptedToSocket(engineToSocketData.remaining());
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
			metrics.recordPlainBytesFromClient(buffer.remaining());
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
		if(log.isTraceEnabled())
			log.trace(mem+"feedPlainPacket [in-buffer] pos="+buffer.position()+" lim="+buffer.limit());
		
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

			if(log.isTraceEnabled())
			log.trace(mem+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
									" lim="+engineToSocketData.limit()+" hsStatus="+hsStatus+" status="+status);
			
			engineToSocketData.flip();
			metrics.recordEncryptedToSocket(engineToSocketData.remaining());
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
