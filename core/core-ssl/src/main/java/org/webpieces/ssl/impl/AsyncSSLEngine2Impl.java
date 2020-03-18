package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncSSLEngine2Impl implements AsyncSSLEngine {

	private static final Logger log = LoggerFactory.getLogger(AsyncSSLEngine2Impl.class);
	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	
	private BufferPool pool;
	private SslMementoImpl mem;
	private SslListener listener;
	private Object wrapLock = new Object();
	//unwrap would be a separate lock but it's better just to 
	//order and not have to be thread safe...responsibility is on the client of this class.
	//This is because you would have to lock unwrap/listener.packetUnencrypted and client would own this
	//lock and for all we know another lock comes into the picture and deadlock occurs.  Instead we do
	//not need to lock anyways.  Just keep ordered and not call from multiple threads AT same time.
	//ie. use something like SessionExecutor 
	//private Object unwrapLock = new Object(); 
	private boolean clientInitiated;
	private AtomicBoolean fireClosed = new AtomicBoolean(false);
	private AtomicBoolean fireConnected = new AtomicBoolean(false);

	private ByteAckTracker encryptionTracker = new ByteAckTracker();
	private ByteAckTracker decryptionTracker = new ByteAckTracker();
	
	public AsyncSSLEngine2Impl(String loggingId, SSLEngine engine, BufferPool pool, SslListener listener) {
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
		
		if(log.isTraceEnabled())
			log.trace(mem+"start handshake");
		try {
			sslEngine.beginHandshake();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
		
		return sendHandshakeMessage();
	}
	
	private CompletableFuture<Void> createRunnable() {
		//KISS here so we do not need to worry about race condition of runnable and
		//incoming handshake packets (also decreases API surface area)
		
		SSLEngine sslEngine = mem.getEngine();
		Runnable r = sslEngine.getDelegatedTask();
//		CompletableFuture<Void> future = new CompletableFuture<Void>();
//		listener.runTask(new Runnable() {
//			@Override
//			public void run() {
//				r.run();
//				
//				runnableComplete().handle((v, t) -> {
//					if(t != null)
//						future.completeExceptionally(t);
//					else
//						future.complete(null);
//					return null;
//				});
//			}
//		});
//		
//		return future;
		r.run();
		return runnableComplete();
	}
	
	private CompletableFuture<Void> runnableComplete() {
		SSLEngine sslEngine = mem.getEngine();
		
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();

		ByteBuffer cached = mem.getCachedToProcess();
		if(hsStatus == HandshakeStatus.NEED_UNWRAP) {
			//unwrap any previously incoming data...
			if(cached != null) {
				mem.setCachedEncryptedData(null); //wipe out the data we are now procesing
				if(log.isTraceEnabled())
					log.trace(mem+"[AfterRunnable][socketToEngine] refeeding myself pos="+cached.position()+" lim="+cached.limit());
				//here the 'cached' was already recorded when fed in...(There is a test for this)
				return feedEncryptedPacketImpl(cached, CompletableFuture.completedFuture(null));
			}
			return CompletableFuture.completedFuture(null);
		} else if(hsStatus == HandshakeStatus.NEED_WRAP) {
			if(log.isTraceEnabled())
				log.trace(mem+"[Runnable]continuing handshake");
			return sendHandshakeMessage();
		} else {
			throw new UnsupportedOperationException("need to support state="+hsStatus);
		}
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
		if(log.isTraceEnabled())
			log.trace(mem+"sending handshake message");
		//HELPER.eraseBuffer(empty);

		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		if(hsStatus != HandshakeStatus.NEED_WRAP)
			throw new IllegalStateException("we should only be calling this method when hsStatus=NEED_WRAP.  hsStatus="+hsStatus);
		
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		while(hsStatus == HandshakeStatus.NEED_WRAP) {
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());
			
			Status lastStatus = null;
			synchronized (wrapLock ) {
				//KEEEEEP This very small.  wrap and then listener.packetEncrypted
				SSLEngineResult result = sslEngine.wrap(EMPTY, engineToSocketData);
				lastStatus = result.getStatus();
				hsStatus = result.getHandshakeStatus();
				
				final Status lastStatus2 = lastStatus;
				final HandshakeStatus hsStatus2 = hsStatus;
				if(log.isTraceEnabled())
				log.trace(mem+"write packet pos="+engineToSocketData.position()+" lim="+
											engineToSocketData.limit()+" status="+lastStatus2+" hs="+hsStatus2);
				if(lastStatus == Status.BUFFER_OVERFLOW || lastStatus == Status.BUFFER_UNDERFLOW)
					throw new RuntimeException("status not right, status="+lastStatus+" even though we sized the buffer to consume all?");
				
				engineToSocketData.flip();
				CompletableFuture<Void> fut = listener.sendEncryptedHandshakeData(engineToSocketData);
				futures.add(fut);
			}
			
			if(lastStatus == Status.CLOSED && !clientInitiated) {
				fireClose();
			}
		}

		if(hsStatus == HandshakeStatus.NEED_WRAP || hsStatus == HandshakeStatus.NEED_TASK)
			throw new RuntimeException(mem+"BUG, need to implement more here status="+hsStatus);			

		final HandshakeStatus hsStatus2 = hsStatus;
		if(log.isTraceEnabled())
			log.trace(mem+"status="+hsStatus2+" isConn="+mem.getConnectionState());
		if(hsStatus == HandshakeStatus.FINISHED) {
			fireLinkEstablished();
		}
		
		CompletableFuture<Void> futureAll = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		return futureAll;
	}

	/**
	 * This is synchronized as the socketToEngineData2 buffer is modified in this method
	 * and modified in other methods that are called on other threads.(ie. the put is called)
	 * @return 
	 * 
	 */
	@Override
	public CompletableFuture<Void> feedEncryptedPacket(ByteBuffer encryptedInData) {
		if(mem.getConnectionState() == ConnectionState.DISCONNECTED)
			throw new IllegalStateException(mem+"SSLEngine is closed");
		
		mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);

		CompletableFuture<Void> future = decryptionTracker.addBytesToTrack(encryptedInData.remaining());

		return feedEncryptedPacketImpl(encryptedInData, future);
	}
	
	private CompletableFuture<Void> feedEncryptedPacketImpl(ByteBuffer encryptedInData, CompletableFuture<Void> byteAcker) {	
		SSLEngine sslEngine = mem.getEngine();
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		Status status = null;

		logTrace1(encryptedInData, hsStatus);
		
		ByteBuffer encryptedData = encryptedInData;
		ByteBuffer cached = mem.getCachedToProcess();
		if(cached != null) {
			encryptedData = combine(cached, encryptedData);
			mem.setCachedEncryptedData(null);
		}
		
		int i = 0;
		//stay in loop while we 
		//1. need unwrap or not_handshaking or need_task AND
		//2. have data in buffer
		//3. have enough data in buffer(ie. not underflow)
		int totalToAck = 0;
		while(encryptedData.hasRemaining() && 
				status != Status.BUFFER_UNDERFLOW && 
				status != Status.CLOSED 
				) {
			i++;
			SSLEngineResult result;
			
			ByteBuffer outBuffer = mem.getCachedOut();
			int remainBeforeDecrypt = encryptedData.remaining();
			try {
				result = sslEngine.unwrap(encryptedData, outBuffer);
				status = result.getStatus();
			} catch(SSLException e) {
				AsyncSSLEngineException ee = new AsyncSSLEngineException("status="+status+" hsStatus="+hsStatus+" b="+encryptedData, e);
				throw ee;
			} finally {
				int totalBytesToAck = remainBeforeDecrypt - encryptedData.remaining();
				if(outBuffer.position() != 0) {
					outBuffer.flip();
					listener.packetUnencrypted(outBuffer).handle((v, t) -> {
						if(t != null)
							log.error("Exception in ssl listener", t);
						
						decryptionTracker.ackBytes(totalBytesToAck);
						return null;
					});

					//frequently the out buffer is not used so we only ask the pool for buffers AFTER it has been consumed/used
					ByteBuffer newCachedOut = pool.nextBuffer(sslEngine.getSession().getApplicationBufferSize());
					mem.setCachedOut(newCachedOut);
				} else {
					totalToAck += totalBytesToAck;
				}
			}

			status = result.getStatus();
			hsStatus = result.getHandshakeStatus();
			if(hsStatus == HandshakeStatus.NEED_TASK || hsStatus == HandshakeStatus.NEED_WRAP) {
				//if status is need task, we need to break to run the task before other handshake
				//messages?  Also, need_wrap happened when firefox was connecting once and this fixed that so it would continue the
				//handshake as well
				break;
			}
			
			logAndCheck(encryptedData, result, outBuffer, status, hsStatus, i);
		}

		if(encryptedData.hasRemaining()) {
			mem.setCachedEncryptedData(encryptedData);
		}
		
		logTrace(encryptedData, status, hsStatus);
		
		if(!encryptedData.hasRemaining())
			pool.releaseBuffer(encryptedData);
		
		int bytesToAck = totalToAck;
		return cleanAndFire(hsStatus, status)
				.thenApply(v -> {
					decryptionTracker.ackBytes(bytesToAck);
					return null;
				})
				.thenCompose(v -> byteAcker);
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

	private void logAndCheck(ByteBuffer encryptedData, SSLEngineResult result, ByteBuffer outBuffer, Status status, HandshakeStatus hsStatus, int i) {
		final ByteBuffer data = encryptedData;

		if(log.isTraceEnabled())
		log.trace(mem+"[sockToEngine] unwrap done pos="+data.position()+" lim="+
							data.limit()+" status="+status+" hs="+hsStatus);
		if(i > 1000) {
			throw new RuntimeException(this+"Bug, stuck in loop, encryptedData="+encryptedData+" outBuffer="+outBuffer+
					" hsStatus="+hsStatus+" status="+status);
		} else if(status == Status.BUFFER_UNDERFLOW) {
			final ByteBuffer data1 = encryptedData;
			if(log.isTraceEnabled())
				log.trace("buffer underflow trace. data="+data1.remaining());
		}
	}

	private CompletableFuture<Void> cleanAndFire(HandshakeStatus hsStatus, Status status) {
		//First if avoids case where the close handshake is still going on so we are not closed
		//yet I think(I am writing this from memory)...
		if(status == Status.CLOSED) {
			if(hsStatus == HandshakeStatus.NEED_WRAP) {
				mem.compareSet(ConnectionState.CONNECTED, ConnectionState.DISCONNECTING);
				return sendHandshakeMessage();
			} else {
				fireClose();
				return CompletableFuture.completedFuture(null);
			}
		} else if(hsStatus == HandshakeStatus.NEED_TASK) {
			return createRunnable();
		} else if(hsStatus == HandshakeStatus.NEED_UNWRAP) {
			//just need to wait for more data
			return CompletableFuture.completedFuture(null);			
		} else if(hsStatus == HandshakeStatus.NEED_WRAP) {
			return sendHandshakeMessage();
		} else if(hsStatus ==HandshakeStatus.FINISHED) {
			fireLinkEstablished();
			return CompletableFuture.completedFuture(null);
		} else if(hsStatus == HandshakeStatus.NOT_HANDSHAKING) {
			//nothing to do.  packet already fed
			return CompletableFuture.completedFuture(null);			
		} else {
			throw new UnsupportedOperationException("need to support state="+hsStatus+" status="+status);
		}
	}

	private void fireLinkEstablished() {
		boolean shouldFire = fireConnected.compareAndSet(false, true);
		if(shouldFire) {
			mem.compareSet(ConnectionState.CONNECTING, ConnectionState.CONNECTED);
			listener.encryptedLinkEstablished();
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
		if(log.isTraceEnabled())
			log.trace(mem+"feedPlainPacket [in-buffer] pos="+buffer.position()+" lim="+buffer.limit());
		
		CompletableFuture<Void> future = encryptionTracker.addBytesToTrack(buffer.remaining());
		
		while(buffer.hasRemaining()) {
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());

			int remainBefore = buffer.remaining();
			synchronized(wrapLock) {
				SSLEngineResult result = sslEngine.wrap(buffer, engineToSocketData);
				int numEncrypted = remainBefore - buffer.remaining();
				Status status = result.getStatus();
				HandshakeStatus hsStatus = result.getHandshakeStatus();
				if(status != Status.OK)
					throw new RuntimeException("Bug, status="+status+" instead of OK.  hsStatus="+
							hsStatus+" Something went wrong and we could not encrypt the data");
	
				if(log.isTraceEnabled())
				log.trace(mem+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
											" lim="+engineToSocketData.limit()+" hsStatus="+hsStatus+" status="+status);
				
				engineToSocketData.flip();
				listener.packetEncrypted(engineToSocketData).handle( (v, t) -> {
					if(t != null)
						log.error("Exception from ssl listener", t);
					encryptionTracker.ackBytes(numEncrypted);
					return null;
				});
			}
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
				sendHandshakeMessage();
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
