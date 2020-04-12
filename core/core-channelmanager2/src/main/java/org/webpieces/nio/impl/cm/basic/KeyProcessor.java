package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.jdk.JdkSelect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;


public final class KeyProcessor {

	private static final Logger apiLog = LoggerFactory.getLogger(DataListener.class);
	private static final Logger log = LoggerFactory.getLogger(KeyProcessor.class);
	//private static BufferHelper helper = ChannelManagerFactory.bufferHelper(null);
	private static boolean logBufferNextRead = false;
	private JdkSelect selector;
	private BufferPool pool;

	private Counter connectionClosed;
	private Counter connectionOpen;
	private Counter connectionErrors;
	private Counter specialConnectionErrors;
	private DistributionSummary payloadSize;
	private Counter unregisteredSocket;
	private Counter registeredSocket;
	private DistributionSummary backupSize;
	private Counter readErrorType1Close;
	private Counter readErrorType2Close;

	public KeyProcessor(String id, JdkSelect selector, BufferPool pool, MeterRegistry metrics) {
		this.selector = selector;
		this.pool = pool;
		connectionClosed = metrics.counter(id+".connections.closed");
		connectionOpen = metrics.counter(id+".connections.opened");
		connectionErrors = metrics.counter(id+".connections.errors");
		specialConnectionErrors = metrics.counter(id+".connections.errors2");
		unregisteredSocket = metrics.counter(id+".connections.unregistered");
		registeredSocket = metrics.counter(id+".connections.registered");
		readErrorType1Close = metrics.counter(id+".connections.readerror1");
		readErrorType2Close = metrics.counter(id+".connections.readerror2");

		payloadSize = DistributionSummary
			    .builder(id+".bytes.read.size")
			    .distributionStatisticBufferLength(1)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.50, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);
		
		backupSize = DistributionSummary
			    .builder(id+".bytes.read.size")
			    .distributionStatisticBufferLength(1)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.50, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);	
	}
	
	public void processKeys(Set<SelectionKey> keySet) {
		Iterator<SelectionKey> iter = keySet.iterator();
		while (iter.hasNext()) {
			SelectionKey key = null;
			RegisterableChannel channel = null;
			try {
				key = iter.next();
				ChannelInfo struct = (ChannelInfo)key.attachment();
				channel = struct.getChannel();

				final SelectionKey current = key;
				final RegisterableChannel finalChannel = channel;
				if(log.isTraceEnabled())
				log.trace(finalChannel+" ops="+OpType.opType(current.readyOps())
											+" acc="+current.isAcceptable()+" read="+current.isReadable()+" write"+current.isWritable());
				processKey(key, struct);
				
			} catch(IOException e) {
				connectionErrors.increment();
				
				log.error(channel+"Processing of key failed, closing channel", e);
				try {
					if(key != null) 
						key.channel().close();
				} catch(Throwable ee) {
					log.error(channel+"Close of channel failed", ee);
				}
			} catch(CancelledKeyException e) {
				connectionErrors.increment();
				
				//TODO: get rid of this if...else statement by fixing
				//CancelledKeyException on linux so the tests don't fail
				RegisterableChannel fChannel = channel;
				if(log.isTraceEnabled())
					log.trace(fChannel+"Processing of key failed, but continuing channel manager loop", e);
			} catch(Throwable e) {
				connectionErrors.increment();
				
				log.error(channel+"Processing of key failed, but continuing channel manager loop", e);
				try {
					key.cancel();
				} catch(Throwable ee) {
					log.info(channel+"cancelling key failed.  exception type="+ee.getClass()+" msg="+ee.getMessage());
				}
			}
		}
		//clear the whole keySet as we processed them all in the while loop.

		//If you do not clear the keySet, keys that have been already processed stay
		//in the selected Key set.  If another key gets added to this set, the selector
		//goes off again and has the stale key plus the new key and the stale key
		//is processed again.
		keySet.clear();
	}
	
	private void processKey(SelectionKey key, ChannelInfo info) throws IOException, InterruptedException {
		if(log.isTraceEnabled())
			log.trace(key.attachment()+"proccessing");

		//This is code to try to avoid the CancelledKeyExceptions as it makes the chances tighter
		if(!selector.isChannelOpen(key) || !key.isValid())
			return;
		
		//if isAcceptable, than is a ServerSocketChannel
		if(key.isAcceptable()) {
			acceptSocket(key, info);
		} 
		
		if(key.isConnectable())
			connect(key, info);
		
		if(key.isWritable()) {
			write(key, info);
		}
            
		//The read MUST be after the write as a call to key.isWriteable is invalid if the
		//read resulted in the far end closing the socket.
		if(key.isReadable()) {
			read(key, info);
		}                   
	}
	
	//each of these functions should be a handler for a new type that we set up
	//on the outside of this thing.  The signature is the same thing every time
	// and we pass key and the Channel.  We can cast to the proper one.
	private void acceptSocket(SelectionKey key, ChannelInfo info) throws IOException {
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+"Incoming Connection="+key);
		
		BasTCPServerChannel channel = (BasTCPServerChannel)info.getChannel();
		channel.accept(channel.getChannelCount());
	}
	
	private void connect(SelectionKey key, ChannelInfo info) throws IOException {
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+"finishing connect process");
		
		CompletableFuture<Channel> callback = info.getConnectCallback();
		BasTCPChannel channel = (BasTCPChannel)info.getChannel();

		try {
			//must change the interests to not interested in connect anymore
			//since we are connected otherwise selector seems to keep firing over
			//and over again with 0 keys wasting cpu like a while(true) loop
			int interests = key.interestOps();
			key.interestOps(interests & (~SelectionKey.OP_CONNECT));
		
			connectionOpen.increment();
			
			channel.finishConnect();
			callback.complete(channel);
		} catch(Exception e) {
			connectionErrors.increment();
			
            log.error(key.attachment()+"Could not open connection", e);
            callback.completeExceptionally(e);
		}
	}

	private void read(SelectionKey key, ChannelInfo info) throws IOException {
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+"reading data");
		
		DataListener in = info.getDataHandler();
		BasChannelImpl channel = (BasChannelImpl)info.getChannel();
		
		ByteBuffer chunk = pool.nextBuffer(1024);
		
		try {
            if(logBufferNextRead)
            	log.info(channel+"buffer="+chunk);
            int bytes = channel.readImpl(chunk);
            if(logBufferNextRead) {
            	logBufferNextRead = false;
            	log.info(channel+"buffer2="+chunk);                	
            }

            processBytes(key, info, chunk, bytes);
            
		} catch(PortUnreachableException e) {
			connectionErrors.increment();
			
            //this is a normal occurence when some writes out udp to a port that is not
            //listening for udp!!!  log as finer and fire to client to deal with it.
			if(log.isTraceEnabled())
				log.trace("Client sent data to a host or port that is not listening " +
			                    "to udp, or udp can't get through to that machine", e);
			in.failure(channel, null, e);
        } catch(NotYetConnectedException e) {
        	connectionErrors.increment();
        	
            //this happens in udp when I disconnect after someone had already been streaming me
            //data.  It is supposed to stop listening but selector keeps firing.
            log.error("Can't read until UDPChannel is connected", e);
            in.failure(channel, null, e);
		} catch(IOException e) {
            //kept getting the following exception so I added this as protection
            //NOTE: this exceptionn should never occur.  read should be returning -1
            //but for some reason is returning hte exception instead.
//        WARNING: [server] Processing of key failed, closing channel
//        java.io.IOException: An established connection was aborted by the software in your host machine
//            at sun.nio.ch.SocketDispatcher.read0(Native Method)
//            at sun.nio.ch.SocketDispatcher.read(SocketDispatcher.java:25)
//            at sun.nio.ch.IOUtil.readIntoNativeBuffer(IOUtil.java:233)
//            at sun.nio.ch.IOUtil.read(IOUtil.java:206)
//            at sun.nio.ch.SocketChannelImpl.read(SocketChannelImpl.java:207)
//            at biz.xsoftware.impl.nio.cm.basic.TCPChannelImpl.readImpl(TCPChannelImpl.java:168)
//            at biz.xsoftware.impl.nio.cm.basic.Helper.read(Helper.java:128)
//            at biz.xsoftware.impl.nio.cm.basic.Helper.processKey(Helper.java:77)
//            at biz.xsoftware.impl.nio.cm.basic.Helper.processKeys(Helper.java:43)
//            at biz.xsoftware.impl.nio.cm.basic.SelectorManager2.runLoop(SelectorManager2.java:262)
//            at biz.xsoftware.impl.nio.cm.basic.SelectorManager2$PollingThread.run
//        (SelectorManager2.java:224)     
            
			//another one that landes here is the Connection reset by peer"....
//			Jan 18, 2012 1:00:42 PM biz.xsoftware.impl.nio.cm.basic.Helper read
//			INFO: [stserver] Exception
//			java.io.IOException: Connection reset by peer
//			        at sun.nio.ch.FileDispatcher.read0(Native Method)
//			        at sun.nio.ch.SocketDispatcher.read(SocketDispatcher.java:39)
//			        at sun.nio.ch.IOUtil.readIntoNativeBuffer(IOUtil.java:251)
//			        at sun.nio.ch.IOUtil.read(IOUtil.java:224)
//			        at sun.nio.ch.SocketChannelImpl.read(SocketChannelImpl.java:254)
//			        at biz.xsoftware.impl.nio.cm.basic.chanimpl.SocketChannelImpl.read(SocketChannelImpl.java:65)
//			        at biz.xsoftware.impl.nio.cm.basic.BasTCPChannel.readImpl(BasTCPChannel.java:108)
//			        at biz.xsoftware.impl.nio.cm.basic.Helper.read(Helper.java:162)
//			        at biz.xsoftware.impl.nio.cm.basic.Helper.processKey(Helper.java:104)
//			        at biz.xsoftware.impl.nio.cm.basic.Helper.processKeys(Helper.java:51)
//			        at biz.xsoftware.impl.nio.cm.basic.SelectorManager2.selectorFired(SelectorManager2.java:253)
//			        at biz.xsoftware.impl.nio.cm.basic.nioimpl.SelectorImpl.runLoop(SelectorImpl.java:126)
//			        at biz.xsoftware.impl.nio.cm.basic.nioimpl.SelectorImpl$PollingThread.run(SelectorImpl.java:107)
			
			//One other exception starts with "An existing connection was forcibly closed"
			
            //in the case of SSL over TCP only, sometimes instead of reading off a -1, this will
            //throw an IOException: "An existing connection was forcibly closed by the remote host"
            //we also close UDPChannels as well on IOException.  Not sure if this is good or not.
			
			specialConnectionErrors.increment();
			
			process(key, in, info, chunk, e);
		} catch(NioException e) {
			connectionErrors.increment();
			
			Throwable cause = e.getCause();
			if(cause instanceof IOException) {
				specialConnectionErrors.increment();

				IOException ioExc = (IOException) cause;
				process(key, in, info, chunk, ioExc);
			} else
				throw e;
		}
	}

	private void process(SelectionKey key, DataListener in, ChannelInfo info,
			ByteBuffer chunk, IOException e) throws IOException {
        //Channel channel = (Channel)info.getChannel();

		String msg = e.getMessage();
		if(msg != null && 
			(msg.contains("An existing connection was forcibly closed")
				|| msg.contains("Connection reset by peer")
				|| msg.contains("An established connection was aborted by the software in your host machine"))) {
		        if(log.isTraceEnabled())
					log.trace("Exception 2", e);
		        
		        readErrorType1Close.increment();
		        processBytes(key, info, chunk, -1);
		} else {
			//log.error("IO Exception unexpected", e);
			//in.failure(channel, null, e);
			readErrorType2Close.increment();
			processBytes(key, info, chunk, -1);
		}
	}

    /**
     * @throws IOException
     */
    private void processBytes(SelectionKey key, ChannelInfo info, ByteBuffer data, int bytes) throws IOException
    {
        DataListener in = info.getDataHandler();
        BasChannelImpl channel = (BasChannelImpl)info.getChannel();
        
        ByteBuffer b = data;
        b.flip();
        
		if(bytes < 0) {
			if(apiLog.isTraceEnabled())
				apiLog.trace(channel+"far end closed, cancel key, close socket");

			connectionClosed.increment();

			channel.serverClosed();
			
			in.farEndClosed(channel);
		} else if(bytes > 0) {
			if(apiLog.isTraceEnabled())
				apiLog.trace(channel+"READ bytes="+bytes);
			fireIncomingRead(key, bytes, in, channel, b);
		}
    }

	private void fireIncomingRead(SelectionKey key, int bytes, DataListener in, BasChannelImpl channel, ByteBuffer b) {
		payloadSize.record(bytes);
		
		CompletableFuture<Void> future = in.incomingData(channel, b);
		boolean unregister = false;
		int unackedByteCnt = 0;
		synchronized(channel) {
			if(channel.getMaxUnacked() != null) {
				unackedByteCnt = channel.addUnackedByteCount(bytes);				
				backupSize.record(unackedByteCnt);
				
				if(channel.isOverMaxUnacked()) {
					unregister = true;
				}
			}
		}

		if(unregister) {
			unregisteredSocket.increment();
			log.warn(channel+"Overloaded channel.  unregistering until YOU catch up you slowass(lol). num="+unackedByteCnt+" max="+channel.getMaxUnacked());
			unregisterSelectableChannel(channel, SelectionKey.OP_READ);
		}

		future.handle((v, t) -> {
			boolean register = false;
			int unackedCnt;
			synchronized(channel) {
				unackedCnt = channel.addUnackedByteCount(-bytes);
				backupSize.record(unackedCnt);
				if(!isReading(key) && channel.isUnderThreshold()) {
					register = true;
				}
			}

			if(register) {
				registeredSocket.increment();
				log.warn(channel+"BOOM. you caught back up, reregistering for reads now. unackedCnt="+unackedCnt+" readThreshold="+channel.getReadThreshold());
				channel.registerForReads();
			}
			
			if(t != null)
				apiLog.error(channel+" Exception on incoming data", t);
			return null;
		});
	}

    private boolean isReading(SelectionKey key) {
    	if((key.interestOps() & SelectionKey.OP_READ) > 0)
    		return true;
    	return false;
    }
    
	private void write(SelectionKey key, ChannelInfo info) throws IOException, InterruptedException {
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+"writing data");
		
		BasChannelImpl channel = (BasChannelImpl)info.getChannel();		
		
		if(log.isTraceEnabled())
			log.trace(channel+"notifying channel of write");

        channel.writeAll();  
	}
	
	void unregisterSelectableChannel(RegisterableChannelImpl channel, int ops) {
		SelectorManager2 mgr = channel.getSelectorManager();		
		if(!Thread.currentThread().equals(mgr.getThread()))
			throw new RuntimeException(channel+"Bug, changing selector keys can only be done " +
					"on registration thread because there is not synchronization");

		//this could be dangerous and result in deadlock....may want
		//to move this to the selector thread from jdk bugs!!!
		//but alas, follow KISS, move on...
        SelectionKey key = channel.keyFor();
        if(key == null || !key.isValid()) //no need to unregister, key is cancelled
            return;

		int previous = key.interestOps();
		int opsNow = previous & ~ops; //subtract out the operation
		key.interestOps(opsNow);
		
		//log.info("unregistering="+Helper.opType(opsNow)+" opToSubtract="+Helper.opType(ops)+" previous="+Helper.opType(previous)+" type="+type);
		
		//make sure we remove the appropriate listener and clean up
		if(key.attachment() != null) {
			ChannelInfo struct = (ChannelInfo)key.attachment();
			struct.removeListener(ops);
		}
	}
}
