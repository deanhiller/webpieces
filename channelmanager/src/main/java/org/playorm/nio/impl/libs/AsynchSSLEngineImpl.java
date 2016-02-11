package org.playorm.nio.impl.libs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.AsyncSSLEngineException;
import org.playorm.nio.api.libs.AsyncSSLEngine;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.PacketAction;
import org.playorm.nio.api.libs.SSLListener;


/**
 * There is synchronization on the close so if two threads call close, they both return
 * normally ending up in the SSLEngine closing.
 * 
 * There is a synchronization on 
 * @author dean.hiller
 *
 */
public class AsynchSSLEngineImpl implements AsyncSSLEngine {

	private static final Logger log = Logger.getLogger(AsyncSSLEngine.class.getName());
	private static final SSLListener NULL_LIST = new NullListener();
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	//private TCPChannel realChannel;
	private boolean isConnected = false;
	private Object id;
	
	//only one of these two should exist.  The other is null...
	private SSLListener sslListener = NULL_LIST;
	private SSLEngine sslEngine;
	private ByteBuffer socketToEngineData2;
	private ByteBuffer engineToAppData;
	private ByteBuffer engineToSocketData;
	private boolean isClosing;
	private boolean runningRunnable;	
	private ByteBuffer empty = ByteBuffer.allocate(1);
	private boolean isClosed;
	private boolean clientInitiated;	
	
	public AsynchSSLEngineImpl(Object id, SSLEngine sslEngine) {		
		this.id = id;
		this.sslEngine = sslEngine;
		
		SSLSession session = sslEngine.getSession();
		socketToEngineData2 = ByteBuffer.allocate(session.getPacketBufferSize());		
		engineToSocketData = ByteBuffer.allocate(session.getPacketBufferSize());
		engineToAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
		
		log.fine("peerNetData: " + socketToEngineData2.capacity() + 
				", peerAppData: " + engineToAppData.capacity() +
				", netData: " + engineToSocketData.capacity());
		
		// The engineToAppData buffer is assumed to be ready to be written,
		// while the other buffers are assumed to be ready to be read from.

		// Change the position of the buffers so that a 
		// call to hasRemaining() returns false. A buffer is considered
		// empty when the position is set to its limit, that is when
		// hasRemaining() returns false.
		HELPER.eraseBuffer(engineToAppData);
		HELPER.eraseBuffer(engineToSocketData);			
	}

	public void setListener(SSLListener l) {
		//this prevents NullPointerExceptions when firing to listener
		//as well as preventing need for synchronization to avoid nullpointerExceptions....
		if(l == null)
			sslListener = NULL_LIST;
		else
			sslListener = l;
	}
	
	private static final class NullListener implements SSLListener {
		public void encryptedLinkEstablished() throws IOException {	}
		public void packetEncrypted(ByteBuffer engineToSocketData, Object passThrough) throws IOException {	}
		public void packetUnencrypted(ByteBuffer out, Object passThrough) {	}
		public void runTask(Runnable r) {}
		public void closed(boolean fromEncryptedPacket) {}
	}
	
	public void beginHandshake() {
		if(isClosing || isClosed) {
			throw new IllegalStateException(id+"SSLEngine is in the process of closing or is closed");
		} else if(runningRunnable)
			throw new IllegalStateException(id+"SSLListener was passed a Runnable object " +
					"that has not completed yet and must complete before encryption can continue");

		if(log.isLoggable(Level.FINE))
			log.fine(id+"start handshake");
		try {
			sslEngine.beginHandshake();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
		HandshakeStatus status = sslEngine.getHandshakeStatus();
		continueHandShake(status);
	}
	
	public void feedPlainPacket(ByteBuffer b, Object passThrough) {	
		if(!isConnected)
			throw new NotYetConnectedException();
		else if(isClosing || isClosed) {
			throw new IllegalStateException(id+"SSLEngine is in the process of closing or is closed");
		} else if(runningRunnable)
			throw new IllegalStateException(id+"SSLListener was passed a Runnable object that" +
					" has not completed yet and must complete before encryption can continue");
		
		if(log.isLoggable(Level.FINE))
			log.fine(id+"feedPlainPacket [in-buffer] pos="+b.position()+" lim="+b.limit());
		
		HELPER.eraseBuffer(engineToSocketData);

		SSLEngineResult result;
		try {
			result = sslEngine.wrap(b, engineToSocketData);
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
		
		Status status = result.getStatus();
		HandshakeStatus hsStatus = result.getHandshakeStatus();
		if(status != Status.OK)
			throw new RuntimeException("Bug, status="+status+" instead of OK.  hsStatus="+
					hsStatus+" Something went wrong and we could not encrypt the data");
		else if(b.hasRemaining())
			throw new RuntimeException(id+"Bug, should read all my data every time");
		
		HELPER.doneFillingBuffer(engineToSocketData);
		if(log.isLoggable(Level.FINE))
			log.fine(id+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
					" lim="+engineToSocketData.limit()+" hsStatus="+hsStatus+" status="+status);
		fireEncryptedPacketToListener(passThrough);
	}
	
	/**
	 * This is synchronized as the socketToEngineData2 buffer is modified in this method
	 * and modified in other methods that are called on other threads.(ie. the put is called)
	 * 
	 */
	public PacketAction feedEncryptedPacket(ByteBuffer b, Object passthrough) {
		if(isClosed) {
			throw new IllegalStateException(id+"SSLEngine is closed");
		} else if(runningRunnable)
			throw new IllegalStateException(id+"SSLListener was passed a Runnable object" +
					" that has not completed yet and must complete before decryption can continue");
		try {
			HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
			if(log.isLoggable(Level.FINE)) {
				log.fine(id+"feedEncryptedPacket [in-buffer] pos="+b.position()+
						" lim="+b.limit()+" hsStatus="+hsStatus+" cap="+b.capacity());
			}
			socketToEngineData2.put(b);
			if(log.isLoggable(Level.FINEST))
				log.finest(id+"[sockToEngine] pos="+socketToEngineData2.position()+" lim="+socketToEngineData2.limit());

			PacketAction result = feedEncryptedPacketImpl(passthrough);
			if(b.hasRemaining())
				throw new RuntimeException(this+"BUG, need to read all data from ByteBuffer.  incoming="+b+" socketToEngineBuf="+socketToEngineData2);
			return result;
		} catch (AsyncSSLEngineException e) {
			//try to close SSLEngine
			throw closeTry(e);
		} catch(NioException e) {
			throw closeTry(e);
		}
	}

	private RuntimeException closeTry(RuntimeException e) {
		try {
			close();
		} catch(Exception ee) {
			log.log(Level.WARNING, "Failure trying to shutdown Link properly.  Encryption Engine is closed", ee);
		}
		throw e;
	}
	
	private PacketAction feedEncryptedPacketImpl(Object passthrough) {	
		PacketAction action = PacketAction.NOT_ENOUGH_BYTES_YET;
		ByteBuffer b = socketToEngineData2;
		if(log.isLoggable(Level.FINEST))
			log.finest(id+"[sockToEngine] finished filling pos="+b.position()+" lim="+b.limit());
		ByteBuffer out = engineToAppData;
		HELPER.eraseBuffer(out);
		HELPER.doneFillingBuffer(b);
		
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		Status status = null;

		if(log.isLoggable(Level.FINEST))
			log.finest(id+"[sockToEngine] going to unwrap pos="+b.position()+
					" lim="+b.limit()+" hsStatus="+hsStatus);

		int i = 0;
		//stay in loop while we 
		//1. need unwrap or not_handshaking or need_task AND
		//2. have data in buffer
		//3. have enough data in buffer(ie. not underflow)
		while(b.hasRemaining() && status != Status.BUFFER_UNDERFLOW && status != Status.CLOSED) {
			i++;
			SSLEngineResult result;
			try {
				result = sslEngine.unwrap(b, out);				
			} catch(SSLException e) {
				AsyncSSLEngineException ee = new AsyncSSLEngineException("status="+status+" hsStatus="+hsStatus+" b="+b, e);
				throw ee;
			}
			status = result.getStatus();
			hsStatus = result.getHandshakeStatus();
			if(log.isLoggable(Level.FINEST))
				log.finest(id+"[sockToEngine] unwrap done pos="+b.position()+" lim="+
						b.limit()+" status="+status+" hs="+hsStatus);
			
			if(out.position() != 0 && status != Status.CLOSED) { 
				//hsStatus == HandshakeStatus.NOT_HANDSHAKING && status != Status.BUFFER_UNDERFLOW) {
				HELPER.doneFillingBuffer(out);
				if(log.isLoggable(Level.FINE))
					log.fine(id+"packetUnencrypted pos="+out.position()+" lim="+
							out.limit()+" hs="+hsStatus+" status="+status);
				action = PacketAction.DECRYPTED_AND_FEDTOLISTENER;
				fireUnencryptedPackToListener(passthrough, out);
				if(out.hasRemaining())
					log.warning(id+"Discarding unread data");
				out.clear();
			}
			
			if(i > 1000)
				throw new RuntimeException(this+"Bug, stuck in loop, bufIn="+b+" bufOut="+out+
						" hsStatus="+hsStatus+" status="+status);
			else if(hsStatus == HandshakeStatus.NEED_TASK) {
				//if status is need task, we need to break to run the task before other handshake
				//messages?
				break;
			}
		}
		resetBuffer(status);
		
		if(log.isLoggable(Level.FINEST))
			log.finest(id+"[sockToEngine] reset pos="+b.position()+" lim="+b.limit()+" status="+status+" hs="+hsStatus);	
		if(log.isLoggable(Level.FINEST) && isConnected && status != Status.CLOSED) {
			log.finest(id+"[out-buffer] pos="+out.position()+" lim="+out.limit());
		}

		//First if avoids case where the close handshake is still going on so we are not closed
		//yet I think(I am writing this from memory)...
		if(status == Status.CLOSED && hsStatus == HandshakeStatus.NOT_HANDSHAKING) {
			isClosed = true;
			closeInbound();
			sslEngine.closeOutbound();
			sslListener.closed(clientInitiated);			
		} else //TODO: add else if(!hsStatus == HandshakeStatus.NOT_HANDSHAKING)
			continueHandShake(hsStatus);
		
		return action;
	}

	private void closeInbound() {
		try {
			sslEngine.closeInbound();
		} catch (SSLException e) {
			throw new AsyncSSLEngineException(e);
		}
	}

	private void fireUnencryptedPackToListener(Object passthrough,
			ByteBuffer out) {
		try {
			sslListener.packetUnencrypted(out, passthrough);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}

	private void resetBuffer(Status status) {
		ByteBuffer b = socketToEngineData2;
		if(!b.hasRemaining()) {
			//if the buffer doesn't have any data in it...clear it...
			b.clear();
		} else {
			//TODO: don't need to move bytes unless the incoming data is bigger than (limit-position)
			//so we could do this one in the feedPacket method and move it there.
			//TODO: rethink using byte array?.......
			byte[] tmp = new byte[b.remaining()];
			b.get(tmp);
			b.clear();
			b.put(tmp); //put the remaining data at the beginning of this buffer
			if(log.isLoggable(Level.FINEST))
				log.finest("[sockToEngine] underflow pos="+b.position()+" lim="+b.limit());			
		}
	}
	

	
	private void continueHandShake(HandshakeStatus status) {
		
		switch (status) {
		case FINISHED:
			fireConnected();
			break;
		case NEED_TASK:
			createRunnable();
			//break;
		case NEED_UNWRAP:				
			if(log.isLoggable(Level.FINE))
				log.fine(id+"need unwrap so wait");
			break;
		case NEED_WRAP:
			if(log.isLoggable(Level.FINEST))
				log.finest(id+"need wrap");
			//if we go to need wrap, I think we can reset the incoming data buffer
			HELPER.eraseBuffer(engineToSocketData);
			sendHandshakeMessage();
			break;
		case NOT_HANDSHAKING:
			if(log.isLoggable(Level.FINEST))
				log.finest(id+"not handshaking");
			break;
		default:
			log.warning("Bug, should never end up here");
			break;
		}
	}
	
	private void createRunnable() {
		Runnable r = sslEngine.getDelegatedTask();
		
		if(r == null) //task has already been retrieved
			return;
		
		Runnable sslRun = new SSLRunnable(r);
		boolean isInitialHandshake = !isConnected;
		if(!isInitialHandshake)
			runningRunnable = true;
		scheduleRunnable(sslRun, isInitialHandshake);
	}
	
	protected void scheduleRunnable(Runnable sslRun, boolean isInitialHandshake) {
		if(log.isLoggable(Level.FINE))
			log.fine(id+"runTask");		
		sslListener.runTask(sslRun);		
	}

	protected void runRunnable(Runnable r) {
		r.run();
		
		try {
			if(log.isLoggable(Level.FINER))
				log.finer(id+"Running actual task now");
			
			continueFromTask();
		} catch(IOException e) {
			log.log(Level.WARNING, id+"not continuing handshake, exception occurred", e);
			initiateClose();
		} finally {
			runningRunnable = false;
		}		
	}
	
	private class SSLRunnable implements Runnable {

		private Runnable r;

		public SSLRunnable(Runnable r) {
			if(r == null)
				throw new IllegalArgumentException("r cannot be null");
			this.r = r;
		}

		public void run() {
			runRunnable(r);
		}
	}
	
	/**
	 * Only called from the task but must be synchronized as other packets may be
	 * in process of a call to sslEngine.unwrap which may change the status of the engine.
	 * ie. This may check the status, while the call to unwrap changes the status and the
	 * if statement needs to be atomic with the method feedEncryptedPacketImpl!!!
	 * 
	 * @throws IOException
	 */
	private void continueFromTask() throws IOException {
		HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
		ByteBuffer b = socketToEngineData2;
		//here be very careful...if this is run while someone is calling 
		//feedEncryptedPacket(which DOES happen), we need to synchronize
		if(hsStatus == HandshakeStatus.NEED_UNWRAP && b.hasRemaining()) {
			if(log.isLoggable(Level.FINER)) {
				log.finer(id+"[Runnable][socketToEngine] refeeding myself pos="+b.position()+" lim="+b.limit());
			}
			feedEncryptedPacketImpl(null);
		} else {
			if(log.isLoggable(Level.FINER))
				log.finer(id+"[Runnable]continuing handshake");					
			continueHandShake(hsStatus);
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
		if(log.isLoggable(Level.FINEST))
			log.finest(id+"sending handshake message");
		HELPER.eraseBuffer(empty);

		if(log.isLoggable(Level.FINEST))
			log.finest(id+"handshake pos="+engineToSocketData.position()+" lim="+engineToSocketData.limit());

		HandshakeStatus hsStatus = HandshakeStatus.NEED_WRAP;
		while(hsStatus == HandshakeStatus.NEED_WRAP) {
			HELPER.eraseBuffer(engineToSocketData);
			if(log.isLoggable(Level.FINEST))
				log.finest(id+"prepare packet pos="+engineToSocketData.position()+" lim="+engineToSocketData.limit());
			engineToSocketData.clear();
			SSLEngineResult result = sslEngine.wrap(empty, engineToSocketData);
			Status status = result.getStatus();
			hsStatus = result.getHandshakeStatus();
			HELPER.doneFillingBuffer(engineToSocketData);
			if(log.isLoggable(Level.FINEST))
				log.finest(id+"write packet pos="+engineToSocketData.position()+" lim="+
						engineToSocketData.limit()+" status="+status+" hs="+hsStatus);
			if(status == Status.BUFFER_OVERFLOW || status == Status.BUFFER_UNDERFLOW)
				throw new RuntimeException("status not right, status="+status);
			
			//realChannel.write(engineToSocketData);
			if(log.isLoggable(Level.FINE))
				log.fine(id+"SSLListener.packetEncrypted pos="+engineToSocketData.position()+
						" lim="+engineToSocketData.limit()+" status="+status+" hs="+hsStatus);			
			fireEncryptedPacketToListener(null);
			if(status == Status.CLOSED) {
				isClosed = true;
				closeInbound();
				sslEngine.closeOutbound();
				sslListener.closed(clientInitiated);
			}
		}

		if(hsStatus == HandshakeStatus.NEED_WRAP || hsStatus == HandshakeStatus.NEED_TASK)
			throw new RuntimeException(id+"BUG, need to implement more here status="+hsStatus);			

		if(log.isLoggable(Level.FINEST))
			log.finest(id+"status="+hsStatus+" isConn="+isConnected);
		if(hsStatus == HandshakeStatus.FINISHED && !isConnected) {
			//this is a sslserver side connect
			//sslserver may be client side :)
			fireConnected(); 
		}
	}

	private void fireEncryptedPacketToListener(Object passthrough) {
		try {
			sslListener.packetEncrypted(engineToSocketData, passthrough);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}

	private void fireConnected() {
		if(!isConnected) {
			//else this is a rehandshake and we don't care!!!!
			isConnected = true;
			if(log.isLoggable(Level.FINE))
				log.fine(id+"SSLListener.encryptedLinkEstablished");			
			try {
				sslListener.encryptedLinkEstablished();
			} catch (IOException e) {
				throw new NioException(e);
			}
		}		
	}

	public synchronized void close() {
		try {
			closeImpl();
		} catch(Exception e) {
			//We expect exceptions when hard closing the SSL Engine as it prefers a handshake.
			log.log(Level.FINE, id+"Exception trying to close channel", e);			
		}
		
		sslListener.closed(clientInitiated);
	}
	
	public synchronized void closeImpl() throws IOException {
		clientInitiated = true;
		if(isClosed)
			return;

		if(log.isLoggable(Level.FINE))
			log.fine(id+"closing AsynchSSLEngine");
		
		sslEngine.closeOutbound(); //close outbound is like intiating a close
		try {
			initiateCloseImpl(); 
		} catch(IOException e) {
			log.log(Level.FINEST, id+"Typical exception as we may be called after farEndClosed", e);
		}
		isClosed = true; //set after as initiateClose check for this too!
		
		try {
			closeInbound();
		} catch(AsyncSSLEngineException e) {
			log.log(Level.FINEST, id+"Normal Expected Exception. Close packet already sent, not waiting for response", e);
		}
	}
	
	public synchronized void initiateClose() {
		try {
			initiateCloseImpl();
		} catch(Exception e) {
			log.log(Level.WARNING, id+"Exception trying to close channel", e);
		}	
	}

	private synchronized void initiateCloseImpl() throws IOException {
		clientInitiated = true;		
		if(isClosing || isClosed)
			return;
		
		if(log.isLoggable(Level.FINE))
			log.fine(id+"closing AsynchSSLEngine");
		isConnected = true;
		isClosing = true;

		sslEngine.closeOutbound();
		
		engineToSocketData.clear();
		if(log.isLoggable(Level.FINER))
			log.finer(id+"pos1="+engineToAppData.position()+" lim="+engineToSocketData.limit());
		sslEngine.wrap(empty, engineToSocketData);
		if(log.isLoggable(Level.FINER))
			log.finer(id+"pos2="+engineToAppData.position()+" lim="+engineToSocketData.limit());
		
		HELPER.doneFillingBuffer(engineToSocketData);
		if(log.isLoggable(Level.FINE))
			log.fine(id+"packetEncrypted pos="+engineToSocketData.position()+" lim="+engineToSocketData.limit());
		fireEncryptedPacketToListener(null);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public boolean isClosing() {
		return isClosing;
	}
	
	public String toString() {
		return id+"";
	}

	public Object getId() {
		return id;
	}
}
