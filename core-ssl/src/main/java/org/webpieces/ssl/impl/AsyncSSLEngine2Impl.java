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
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLEngineException;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.ssl.api.SslListener;

import com.webpieces.data.api.BufferPool;

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
	
	public AsyncSSLEngine2Impl(String loggingId, SSLEngine engine, BufferPool pool, SslListener listener) {
		this.pool = pool;
		this.listener = listener;
		ByteBuffer cachedOutBuffer = pool.nextBuffer(engine.getSession().getApplicationBufferSize());
		this.mem = new SslMementoImpl(loggingId, engine, cachedOutBuffer);
	}

	@Override
	public void beginHandshake() {
		mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);
		SSLEngine sslEngine = mem.getEngine();
		
		if(log.isTraceEnabled())
			log.trace(mem+"start handshake");
		try {
			sslEngine.beginHandshake();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
		
		sendHandshakeMessage();
	}
	
	private void createRunnable() {
		SSLEngine sslEngine = mem.getEngine();
		Runnable r = sslEngine.getDelegatedTask();
		
		listener.runTask(new Runnable() {
			@Override
			public void run() {
				r.run();
				
				runnableComplete();
			}
		});
	}
	
	private void runnableComplete() {
		SSLEngine sslEngine = mem.getEngine();
		
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();

		List<ByteBuffer> toProcess = mem.getCachedToProcess();
		if(hsStatus == HandshakeStatus.NEED_UNWRAP) {
			//unwrap any previously incoming data...
			for(ByteBuffer buf : toProcess) {
				if(log.isTraceEnabled()) {
					log.trace(mem+"[AfterRunnable][socketToEngine] refeeding myself pos="+buf.position()+" lim="+buf.limit());
				}
				feedEncryptedPacketImpl(buf);
			}
		} else if(hsStatus == HandshakeStatus.NEED_WRAP) {
			if(log.isTraceEnabled())
				log.trace(mem+"[Runnable]continuing handshake");
			sendHandshakeMessage();
		} else {
			throw new UnsupportedOperationException("need to support state="+hsStatus);
		}
	}

	private void sendHandshakeMessage() {
		try {
			sendHandshakeMessageImpl();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
	}
	
	private void sendHandshakeMessageImpl() throws SSLException {
		SSLEngine sslEngine = mem.getEngine();
		if(log.isTraceEnabled())
			log.trace(mem+"sending handshake message");
		//HELPER.eraseBuffer(empty);

		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		if(hsStatus != HandshakeStatus.NEED_WRAP)
			throw new IllegalStateException("we should only be calling this method when hsStatus=NEED_WRAP.  hsStatus="+hsStatus);
		
		while(hsStatus == HandshakeStatus.NEED_WRAP) {
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());
			
			Status lastStatus = null;
			synchronized (wrapLock ) {
				//KEEEEEP This very small.  wrap and then listener.packetEncrypted
				SSLEngineResult result = sslEngine.wrap(EMPTY, engineToSocketData);
				lastStatus = result.getStatus();
				hsStatus = result.getHandshakeStatus();
				
				
				if(log.isTraceEnabled())
					log.trace(mem+"write packet pos="+engineToSocketData.position()+" lim="+
							engineToSocketData.limit()+" status="+lastStatus+" hs="+hsStatus);
				if(lastStatus == Status.BUFFER_OVERFLOW || lastStatus == Status.BUFFER_UNDERFLOW)
					throw new RuntimeException("status not right, status="+lastStatus+" even though we sized the buffer to consume all?");
				
				engineToSocketData.flip();
				listener.sendEncryptedHandshakeData(engineToSocketData);
			}
			
			if(lastStatus == Status.CLOSED && !clientInitiated) {
				fireClose();
			}
		}

		if(hsStatus == HandshakeStatus.NEED_WRAP || hsStatus == HandshakeStatus.NEED_TASK)
			throw new RuntimeException(mem+"BUG, need to implement more here status="+hsStatus);			

		if(log.isTraceEnabled())
			log.trace(mem+"status="+hsStatus+" isConn="+mem.getConnectionState());
		if(hsStatus == HandshakeStatus.FINISHED) {
			fireLinkEstablished();
		}
	}

	/**
	 * This is synchronized as the socketToEngineData2 buffer is modified in this method
	 * and modified in other methods that are called on other threads.(ie. the put is called)
	 * 
	 */
	@Override
	public void feedEncryptedPacket(ByteBuffer b) {
		if(mem.getConnectionState() == ConnectionState.DISCONNECTED)
			throw new IllegalStateException(mem+"SSLEngine is closed");
		
		mem.compareSet(ConnectionState.NOT_STARTED, ConnectionState.CONNECTING);
		
		feedEncryptedPacketImpl(b);
	}
	
	private void feedEncryptedPacketImpl(ByteBuffer encryptedInData) {	
		SSLEngine sslEngine = mem.getEngine();
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		Status status = null;

		if(log.isTraceEnabled())
			log.trace(mem+"[sockToEngine] going to unwrap pos="+encryptedInData.position()+
					" lim="+encryptedInData.limit()+" hsStatus="+hsStatus+" cached="+mem.getCachedForUnderFlow());

		ByteBuffer encryptedData = encryptedInData;
		if(mem.getCachedForUnderFlow() != null) {
			encryptedData = combine(mem.getCachedForUnderFlow(), encryptedInData);
			mem.setCachedForUnderFlow(null);
		}
		
		int i = 0;
		//stay in loop while we 
		//1. need unwrap or not_handshaking or need_task AND
		//2. have data in buffer
		//3. have enough data in buffer(ie. not underflow)
		while(encryptedData.hasRemaining() && status != Status.BUFFER_UNDERFLOW && status != Status.CLOSED) {
			i++;
			SSLEngineResult result;
			
			ByteBuffer outBuffer = mem.getCachedOut();
			try {
				result = sslEngine.unwrap(encryptedData, outBuffer);				
			} catch(SSLException e) {
				AsyncSSLEngineException ee = new AsyncSSLEngineException("status="+status+" hsStatus="+hsStatus+" b="+encryptedData, e);
				throw ee;
			} finally {
				if(outBuffer.position() != 0) {
					listener.packetUnencrypted(outBuffer);
					
					//frequently the out buffer is not used so we only ask the pool for buffers AFTER it has been consumed/used
					ByteBuffer newCachedOut = pool.nextBuffer(sslEngine.getSession().getApplicationBufferSize());
					mem.setCachedOut(newCachedOut);
				}
			}
			status = result.getStatus();
			hsStatus = result.getHandshakeStatus();
			if(log.isTraceEnabled())
				log.trace(mem+"[sockToEngine] unwrap done pos="+encryptedData.position()+" lim="+
						encryptedData.limit()+" status="+status+" hs="+hsStatus);
			
			if(i > 1000)
				throw new RuntimeException(this+"Bug, stuck in loop, bufIn="+encryptedData+" bufOut="+outBuffer+
						" hsStatus="+hsStatus+" status="+status);
			else if(hsStatus == HandshakeStatus.NEED_TASK) {
				if(encryptedData.hasRemaining()) {
					mem.addCachedEncryptedData(encryptedData);
				}
				//if status is need task, we need to break to run the task before other handshake
				//messages?
				break;
			} else if(status == Status.BUFFER_UNDERFLOW) {
				mem.setCachedForUnderFlow(encryptedData);
			}
		}

		if(log.isTraceEnabled())
			log.trace(mem+"[sockToEngine] reset pos="+encryptedData.position()+" lim="+encryptedData.limit()+" status="+status+" hs="+hsStatus);

		if(!encryptedData.hasRemaining())
			pool.releaseBuffer(encryptedData);
		
		//First if avoids case where the close handshake is still going on so we are not closed
		//yet I think(I am writing this from memory)...
		if(status == Status.CLOSED) {
			if(hsStatus == HandshakeStatus.NEED_WRAP) {
				mem.compareSet(ConnectionState.CONNECTED, ConnectionState.DISCONNECTING);
				sendHandshakeMessage();
			} else {
				fireClose();
			}
		} else if(hsStatus == HandshakeStatus.NEED_TASK) {
			createRunnable();
		} else if(hsStatus == HandshakeStatus.NEED_UNWRAP) {
			//just need to wait for more data
		} else if(hsStatus == HandshakeStatus.NEED_WRAP) {
			sendHandshakeMessage();
		} else if(hsStatus ==HandshakeStatus.FINISHED) {
			fireLinkEstablished();
		} else if(hsStatus == HandshakeStatus.NOT_HANDSHAKING) {
			//nothing to do.  packet already fed
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

	private ByteBuffer combine(ByteBuffer cachedForUnderFlow, ByteBuffer encryptedInData) {
		int size = cachedForUnderFlow.remaining()+encryptedInData.remaining();
		ByteBuffer nextBuffer = pool.nextBuffer(size);
		nextBuffer.put(cachedForUnderFlow);
		nextBuffer.put(encryptedInData);
		nextBuffer.flip();
		
		pool.releaseBuffer(cachedForUnderFlow);
		pool.releaseBuffer(encryptedInData);
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
	
	@SuppressWarnings("rawtypes")
	public CompletableFuture<Void> feedPlainPacketImpl(ByteBuffer buffer) throws SSLException {
		if(mem.getConnectionState() != ConnectionState.CONNECTED)
			throw new IllegalStateException(mem+" SSLEngine is not connected right now");
		else if(!buffer.hasRemaining())
			throw new IllegalArgumentException("your buffer has no readable data");
		
		SSLEngine sslEngine = mem.getEngine();
		if(log.isTraceEnabled())
			log.trace(mem+"feedPlainPacket [in-buffer] pos="+buffer.position()+" lim="+buffer.limit());
		
		List<CompletableFuture> futures = new ArrayList<>();
		while(buffer.hasRemaining()) {
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());

			synchronized(wrapLock) {
				SSLEngineResult result = sslEngine.wrap(buffer, engineToSocketData);
				
				Status status = result.getStatus();
				HandshakeStatus hsStatus = result.getHandshakeStatus();
				if(status != Status.OK)
					throw new RuntimeException("Bug, status="+status+" instead of OK.  hsStatus="+
							hsStatus+" Something went wrong and we could not encrypt the data");
	
				if(log.isTraceEnabled())
					log.trace(mem+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
							" lim="+engineToSocketData.limit()+" hsStatus="+hsStatus+" status="+status);
				
				engineToSocketData.flip();
				CompletableFuture future = listener.packetEncrypted(engineToSocketData);
				futures.add(future);
			}
		}

		pool.releaseBuffer(buffer);
		
		CompletableFuture[] array = futures.toArray(new CompletableFuture[0]);
		return CompletableFuture.allOf(array);
	}

	@Override
	public void close() {

		clientInitiated = true;
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
