package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ssl.api.Action;
import org.webpieces.ssl.api.ActionState;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLEngineException;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.ssl.api.SslMemento;

import com.webpieces.data.api.BufferPool;

public class AsyncSSLEngine2Impl implements AsyncSSLEngine {

	private static final Logger log = LoggerFactory.getLogger(AsyncSSLEngine2Impl.class);
	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	
	private BufferPool pool;

	public AsyncSSLEngine2Impl(BufferPool pool) {
		this.pool = pool;
	}

	@Override
	public SslMemento createMemento(String id, SSLEngine engine) {
		ByteBuffer cachedOutBuffer = pool.nextBuffer(engine.getSession().getApplicationBufferSize());
		return new SslMementoImpl(id, engine, cachedOutBuffer);
	}

	@Override
	public SslMemento beginHandshake(SslMemento memento) {
		
		SslMementoImpl mem = (SslMementoImpl) memento;
		mem.clear();
		mem.setConnectionState(ConnectionState.CONNECTING);
		SSLEngine sslEngine = mem.getEngine();
		
		if(log.isTraceEnabled())
			log.trace(mem+"start handshake");
		try {
			sslEngine.beginHandshake();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
		HandshakeStatus status = sslEngine.getHandshakeStatus();
		continueHandShake(mem, status);
		return mem;
	}
	
	private void continueHandShake(SslMementoImpl mem, HandshakeStatus status) {
		
		switch (status) {
		case FINISHED:
			mem.setConnectionState(ConnectionState.CONNECTED);
			mem.setActionToTake(new Action(ActionState.CONNECTED));
			break;
		case NEED_TASK:
			createRunnable(mem);
			break;
		case NEED_UNWRAP:				
			if(log.isTraceEnabled())
				log.trace(mem+"need unwrap so wait");
			mem.setActionToTake(new Action(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET));
			break;
		case NEED_WRAP:
			if(log.isTraceEnabled())
				log.trace(mem+"need wrap");
			sendHandshakeMessage(mem);
			break;
		case NOT_HANDSHAKING:
			if(log.isTraceEnabled())
				log.trace(mem+"not handshaking");
			break;
		default:
			log.warn(mem+"Bug, should never end up here");
			break;
		}
	}
	
	private void createRunnable(SslMementoImpl mem) {
		SSLEngine sslEngine = mem.getEngine();
		Runnable r = sslEngine.getDelegatedTask();
		if(r == null)
			mem.setActionToTake(new Action(ActionState.WAITING_ON_RUNNABLE_COMPLETE_CALL));
		else
			mem.setActionToTake(new Action(r));
	}
	
	@Override
	public SslMemento runnableComplete(SslMemento memento) {
		SslMementoImpl mem = (SslMementoImpl) memento;
		mem.clear();
		
		SSLEngine sslEngine = mem.getEngine();
		
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		if(hsStatus == HandshakeStatus.NEED_TASK)
			throw new IllegalStateException("Client still did not run the Runnable returned in a previous call, yet client is calling runnableComplete");

		List<ByteBuffer> toProcess = mem.getCachedToProcess();
		if(toProcess.size() > 0) {
			for(ByteBuffer buf : toProcess) {
				if(log.isTraceEnabled()) {
					log.trace(mem+"[AfterRunnable][socketToEngine] refeeding myself pos="+buf.position()+" lim="+buf.limit());
				}
				feedEncryptedPacketImpl(mem, buf);
			}
		} else {
			if(log.isTraceEnabled())
				log.trace(mem+"[Runnable]continuing handshake");
			continueHandShake(mem, hsStatus);
		}
		return mem;
	}

	private void sendHandshakeMessage(SslMementoImpl mem) {
		try {
			sendHandshakeMessageImpl(mem);
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
	}
	
	private void sendHandshakeMessageImpl(SslMementoImpl mem) throws SSLException {
		SSLEngine sslEngine = mem.getEngine();
		if(log.isTraceEnabled())
			log.trace(mem+"sending handshake message");
		//HELPER.eraseBuffer(empty);

		List<ByteBuffer> buffersToSend = new ArrayList<>();
		HandshakeStatus hsStatus = HandshakeStatus.NEED_WRAP;
		while(hsStatus == HandshakeStatus.NEED_WRAP) {
//			HELPER.eraseBuffer(engineToSocketData);
//			if(log.isTraceEnabled())
//				log.trace(id+"prepare packet pos="+engineToSocketData.position()+" lim="+engineToSocketData.limit());
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());
			
			SSLEngineResult result = sslEngine.wrap(EMPTY, engineToSocketData);
			Status status = result.getStatus();
			hsStatus = result.getHandshakeStatus();
			
			engineToSocketData.flip();
			if(log.isTraceEnabled())
				log.trace(mem+"write packet pos="+engineToSocketData.position()+" lim="+
						engineToSocketData.limit()+" status="+status+" hs="+hsStatus);
			if(status == Status.BUFFER_OVERFLOW || status == Status.BUFFER_UNDERFLOW)
				throw new RuntimeException("status not right, status="+status+" even though we sized the buffer to consume all?");
			
			if(log.isTraceEnabled())
				log.trace(mem+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
						" lim="+engineToSocketData.limit()+" status="+status+" hs="+hsStatus);
			buffersToSend.add(engineToSocketData);
//			fireEncryptedPacketToListener(null);
//TODO: dhiller			
//			fireEncryptedPacketToListener(null);
//			if(status == Status.CLOSED) {
//				isClosed = true;
//				closeInbound();
//				sslEngine.closeOutbound();
//				sslListener.closed(clientInitiated);
//			}
		}

		if(hsStatus == HandshakeStatus.NEED_WRAP || hsStatus == HandshakeStatus.NEED_TASK)
			throw new RuntimeException(mem+"BUG, need to implement more here status="+hsStatus);			

		
		
//TODO: dhiller		
		if(log.isTraceEnabled())
			log.trace(mem+"status="+hsStatus+" isConn="+mem.getConnectionState());
		if(hsStatus == HandshakeStatus.FINISHED) {
			mem.setConnectionState(ConnectionState.CONNECTED);
			//this is a sslserver side connect
			//sslserver may be client side :)
			mem.setActionToTake(new Action(ActionState.CONNECTED_AND_SEND_TO_SOCKET, buffersToSend));
		} else {
			mem.setActionToTake(new Action(buffersToSend));	
		}
	}
	
	/**
	 * This is synchronized as the socketToEngineData2 buffer is modified in this method
	 * and modified in other methods that are called on other threads.(ie. the put is called)
	 * 
	 */
	public SslMemento feedEncryptedPacket(SslMemento memento, ByteBuffer b) {
		if(memento.getConnectionState() == ConnectionState.CLOSED)
			throw new IllegalStateException(memento+"SSLEngine is closed");
		
		SslMementoImpl mem = (SslMementoImpl) memento;
		mem.clear();
		if(mem.getConnectionState() == ConnectionState.NOT_CONNECTED)
			mem.setConnectionState(ConnectionState.CONNECTING);
		
		feedEncryptedPacketImpl(mem, b);
		return memento;
	}
	
	private void feedEncryptedPacketImpl(SslMementoImpl mem, ByteBuffer encryptedInData) {	
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
					//frequently the out buffer is not used so we only ask the pool for buffers AFTER it has been consumed/used
					ByteBuffer newCachedOut = pool.nextBuffer(sslEngine.getSession().getApplicationBufferSize());
					mem.setCachedOut(newCachedOut);
					
					throw new UnsupportedOperationException("not done here yet"); //need to move outBuf into Action
				}
			}
			status = result.getStatus();
			hsStatus = result.getHandshakeStatus();
			if(log.isTraceEnabled())
				log.trace(mem+"[sockToEngine] unwrap done pos="+encryptedData.position()+" lim="+
						encryptedData.limit()+" status="+status+" hs="+hsStatus);
			
//			if(out.position() != 0 && status != Status.CLOSED) { 
//				//hsStatus == HandshakeStatus.NOT_HANDSHAKING && status != Status.BUFFER_UNDERFLOW) {
//				HELPER.doneFillingBuffer(out);
//				if(log.isTraceEnabled())
//					log.trace(id+"packetUnencrypted pos="+out.position()+" lim="+
//							out.limit()+" hs="+hsStatus+" status="+status);
//				action = PacketAction.DECRYPTED_AND_FEDTOLISTENER;
//				fireUnencryptedPackToListener(passthrough, out);
//				if(out.hasRemaining())
//					log.warn(id+"Discarding unread data");
//				out.clear();
//			}
			
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
//		if(log.isTraceEnabled() && mem.isConnected() && status != Status.CLOSED) {
//			log.trace(mem+"[out-buffer] pos="+out.position()+" lim="+out.limit());
//		}
		
		//First if avoids case where the close handshake is still going on so we are not closed
		//yet I think(I am writing this from memory)...
		if(status == Status.CLOSED && hsStatus == HandshakeStatus.NOT_HANDSHAKING) {
//			isClosed = true;
//			closeInbound();
//			sslEngine.closeOutbound();
//			sslListener.closed(clientInitiated);			
		} else //TODO: add else if(!hsStatus == HandshakeStatus.NOT_HANDSHAKING)
			continueHandShake(mem, hsStatus);
		
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
	public SslMemento feedPlainPacket(SslMemento memento, ByteBuffer buffer) {
		if(memento.getConnectionState() != ConnectionState.CONNECTED)
			throw new IllegalStateException(memento+" SSLEngine is not connected right now");
		else if(!buffer.hasRemaining())
			throw new IllegalArgumentException("your buffer has no readable data");
		
		SslMementoImpl mem = (SslMementoImpl) memento;
		SSLEngine sslEngine = mem.getEngine();
		if(log.isTraceEnabled())
			log.trace(mem+"feedPlainPacket [in-buffer] pos="+buffer.position()+" lim="+buffer.limit());
		
		List<ByteBuffer> buffersToSend = new ArrayList<>();
		while(buffer.hasRemaining()) {
			ByteBuffer engineToSocketData = pool.nextBuffer(sslEngine.getSession().getPacketBufferSize());
			SSLEngineResult result;
			try {
				result = sslEngine.wrap(buffer, engineToSocketData);
			} catch (SSLException e) {
				throw new AsyncSSLEngineException(e);
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
			buffersToSend.add(engineToSocketData);
		}

		mem.setActionToTake(new Action(buffersToSend));
		
		return mem;
	}
}
