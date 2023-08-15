package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.util.*;

import org.webpieces.nio.api.Throttle;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.jdk.Keys;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.webpieces.data.api.BufferPool;
import org.webpieces.metrics.MetricsCreator;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.exceptions.NioException;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;


public final class KeyProcessor {

	private static final Logger apiLog = LoggerFactory.getLogger(DataListener.class);
	private static final Logger log = LoggerFactory.getLogger(KeyProcessor.class);
	public static final Logger THROTTLE_LOGGER = Throttle.LOG;

	//private static BufferHelper helper = ChannelManagerFactory.bufferHelper(null);
	private static boolean logBufferNextRead = false;
	private JdkSelect selector;
	private BufferPool pool;
	private Throttle throttler;

	private Counter connectionClosed;
	private Counter connectionOpen;
	private Counter connectionErrors;
	private Counter specialConnectionErrors;
	private DistributionSummary payloadSize;
	private Counter backPressureUnregisterSocket;
	private Counter backPressureRegisterSocket;
	private Counter readErrorType1Close;
	private Counter readErrorType2Close;
	private AtomicLong totalBackupCounter = new AtomicLong();

	private long connectionCounter;

	public KeyProcessor(String id, JdkSelect selector, BufferPool pool, MeterRegistry metrics, Throttle throttler) {
		this.selector = selector;
		this.pool = pool;
		this.throttler = throttler;

		connectionClosed = MetricsCreator.createCounter(metrics, id, "connectionsClosed", false);
		connectionOpen = MetricsCreator.createCounter(metrics, id, "connectionsOpened", false);
		connectionErrors = MetricsCreator.createCounter(metrics, id, "connectionErrors1", true);
		specialConnectionErrors = MetricsCreator.createCounter(metrics, id, "connectionErrors2", true);
		backPressureUnregisterSocket = MetricsCreator.createCounter(metrics, id, "backPressureStart", false);
		backPressureRegisterSocket = MetricsCreator.createCounter(metrics, id, "backPressureStop", false);
		readErrorType1Close = MetricsCreator.createCounter(metrics, id, "connectionsReadError1", true);
		readErrorType2Close = MetricsCreator.createCounter(metrics, id, "connectionsReadError2", true);

		payloadSize = MetricsCreator.createSizeDistribution(metrics, id, "rawSocket", "fromsocket");

		MetricsCreator.createGauge(metrics, id+".allChannelsBackPressure", totalBackupCounter, (c) -> c.get());
	}
	
	public void processKeys(DelayedRunnables delayedRunnables, Keys keys) {
		Set<SelectionKey> keySet = keys.getSelectedKeys();
		int initialSize = keySet.size();
		Iterator<SelectionKey> iter = keySet.iterator();
		Counts counts = new Counts();
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
				processKey(delayedRunnables, key, struct, counts);
				
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
					log.trace(fChannel+" Processing of key failed, but continuing channel manager loop", e);
			} catch(Throwable e) {
				connectionErrors.increment();
				
				log.error(channel+" Processing of key failed, but continuing channel manager loop", e);
				try {
					key.cancel();
				} catch(Throwable ee) {
					log.info(channel+" cancelling key failed.  exception type="+ee.getClass()+" msg="+ee.getMessage());
				}
			}
		}
		//clear the whole keySet as we processed them all in the while loop.

		//If you do not clear the keySet, keys that have been already processed stay
		//in the selected Key set.  If another key gets added to this set, the selector
		//goes off again and has the stale key plus the new key and the stale key
		//is processed again.
		keySet.clear();

		if(THROTTLE_LOGGER.isTraceEnabled() && throttler.isThrottling())
			THROTTLE_LOGGER.trace("DONE processing keys. "+counts+" toProcess="+keys.getKeyCount()+" setSize="+initialSize+" now="+keySet.size());
	}
	
	private void processKey(DelayedRunnables delayedRunnables, SelectionKey key, ChannelInfo info, Counts counts) throws IOException, InterruptedException {
		if(log.isTraceEnabled())
			log.trace(key.attachment()+" proccessing");

		//This is code to try to avoid the CancelledKeyExceptions as it makes the chances tighter
		if(!selector.isChannelOpen(key) || !key.isValid())
			return;

		//if isAcceptable, than is a ServerSocketChannel
		if (key.isAcceptable()) {
			throttleOrProcessSvrSocket(delayedRunnables.getDelayedSvrSockets(), new CachedKey(key, info));
			counts.addAccept();
		}

		if(key.isConnectable()) {
			connect(key, info);
			counts.addConnect();
		}
		
		if(key.isWritable()) {
			write(key, info);
			counts.addWrite();
		}
            
		//The read MUST be after the write as a call to key.isWriteable is invalid if the
		//read resulted in the far end closing the socket.
		if(key.isReadable()) {
			throttleOrProcess(delayedRunnables.getDelayedReads(), new CachedKey(key, info));
			counts.addRead();
		}                   
	}

	private void throttleOrProcess(Map<SelectionKey, Runnable> delayedReads, CachedKey cachedKey) throws IOException {
		SelectionKey key = cachedKey.getKey();
		if(throttler.isThrottling()) {
			if(delayedReads.size() < 1) {
				//only log first one each time we turn on..
				THROTTLE_LOGGER.info("START Throttling reads until server caught up on responding. chan="+cachedKey.getInfo().getChannel()+" delReads="+delayedReads.size());
			}

			DataListener dataHandler = cachedKey.getInfo().getDataHandler();
			if(dataHandler == null)
				throw new IllegalStateException("bug, should not be null here");

			RegisterableChannelImpl channel = cachedKey.getInfo().getChannel();
			unregisterSelectableChannel(channel, SelectionKey.OP_READ, key);

			Runnable r = () -> runDelayedTurnOnReading(cachedKey, dataHandler);
			delayedReads.put(key, r);

		} else {
			read(cachedKey);
		}
	}

	private void throttleOrProcessSvrSocket(Map<SelectionKey, Runnable> delayedRunnables, CachedKey cachedKey) throws IOException {
		SelectionKey key = cachedKey.getKey();
		if(throttler.isThrottling()) {
			THROTTLE_LOGGER.info("Throttling incoming connections to load.  not accepting YET. size="
					+ delayedRunnables.size()+" count="+connectionCounter+" chan="+cachedKey.getInfo().getChannel()
					+ " key="+key);

			ConnectionListener connectionListener = cachedKey.getInfo().getConnectionListener();
			if(connectionListener == null)
				throw new IllegalStateException("bug, should have a connection listener");

			RegisterableChannelImpl channel = cachedKey.getInfo().getChannel();
			unregisterSelectableChannel(channel, SelectionKey.OP_ACCEPT, key);

			Runnable r = () -> runDelayedAcceptSocket(cachedKey, connectionListener);
			delayedRunnables.put(key, r);

		} else {
			acceptSocket(cachedKey);
		}
	}

	public void runDelayedTurnOnReading(CachedKey cached, DataListener dataHandler) {
		try {
			RegisterableChannelImpl channel = cached.getInfo().getChannel();
			registerChannelOnThisThread(channel, SelectionKey.OP_READ, dataHandler, () -> true);
		} catch (Throwable e) {
			log.error("Fatal excception trying to turn back on reading from socket: "+cached.getInfo().getChannel(), e);
		}
	}

	public void runDelayedAcceptSocket(CachedKey cached, ConnectionListener connectionListener) {
		try {
			RegisterableChannelImpl channel = cached.getInfo().getChannel();
			registerChannelOnThisThread(channel, SelectionKey.OP_ACCEPT, connectionListener, () -> true);
		} catch (Throwable e) {
			log.error("Fatal excception trying to turn back on accepting connections on server socket", e);
		}

		try {
			acceptSocket(cached);
		} catch (Throwable e) {
			log.error("Excception trying to accept incoming connection. moving on...", e);
		}
	}

	//each of these functions should be a handler for a new type that we set up
	//on the outside of this thing.  The signature is the same thing every time
	// and we pass key and the Channel.  We can cast to the proper one.
	private void acceptSocket(CachedKey cached) throws IOException {
		SelectionKey key = cached.getKey();
		ChannelInfo info = cached.getInfo();
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+" Incoming Connection="+key);

		connectionCounter++;
		if(THROTTLE_LOGGER.isDebugEnabled()) {
			if (connectionCounter % 10 == 0)
				THROTTLE_LOGGER.debug("connection counter=" + connectionCounter);
		}

		BasTCPServerChannel channel = (BasTCPServerChannel)info.getChannel();
		channel.accept(channel.getChannelCount());
	}
	
	private void connect(SelectionKey key, ChannelInfo info) throws IOException {
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+" finishing connect process");
		
		XFuture<Channel> callback = info.getConnectCallback();
		BasTCPChannel channel = (BasTCPChannel)info.getChannel();

		try {
			MDCUtil.setMDC(channel.isServerSide(), channel.getChannelId());

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
			
			//completableFuture has exception so they can log it
            log.debug(key.attachment()+"Could not open connection", e);
            
            callback.completeExceptionally(e);
		} finally {
			MDCUtil.clearMDC(channel.isServerSide());
		}
	}

	private void read(CachedKey cachedKey) throws IOException {
		SelectionKey key = cachedKey.getKey();
		ChannelInfo info = cachedKey.getInfo();
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+"reading data");
		
		DataListener in = info.getDataHandler();
		BasChannelImpl channel = (BasChannelImpl)info.getChannel();

		MDCUtil.setMDC(channel.isServerSide(), channel.getChannelId());

		ByteBuffer chunk = pool.nextBuffer(1024);
		
		try {
            if(logBufferNextRead)
            	log.info(channel+" buffer="+chunk);
            int bytes = channel.readImpl(chunk);
            if(logBufferNextRead) {
            	logBufferNextRead = false;
            	log.info(channel+" buffer2="+chunk);
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
		} finally {
			MDCUtil.clearMDC(channel.isServerSide());
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
		if(channel.isClosed()) {
			//in streaming, we still get data from nic buffer sometimes while socket is closed!  We should not process
			//that since client (or server closed the socket).  ALSO, channel.isClosed returns false so we could 
			log.info(channel+"Socket is closed, discarding data from nic buffer still coming in on this socket");
			return;
		}
		
		payloadSize.record(bytes);

		boolean unregister = false;
		XFuture<Void> future = in.incomingData(channel, b);
		int unackedByteCnt = 0;
		AtomicReference<BackflowState1> connectionState = channel.getCompareSetBackflowState();
		if(channel.getMaxUnacked() != null) {
			//backpressure is ENABLED since it has a value
			AtomicInteger counter = channel.getUnackedBytes();
			unackedByteCnt = counter.addAndGet(bytes);
			totalBackupCounter.addAndGet(bytes);

			if(channel.isOverMaxUnacked(unackedByteCnt)) {
				unregister = connectionState.compareAndSet(BackflowState1.REGISTERED, BackflowState1.UNREGISTERED);
			}
		}

		if(unregister) {
			backPressureUnregisterSocket.increment();
			log.warn(channel + " Overloaded channel.  unregistering until YOU catch up you slowass(lol). num=" + unackedByteCnt + " max=" + channel.getMaxUnacked());
			unregisterSelectableChannel(channel, SelectionKey.OP_READ, key);
		}

		future.handle((v, t) -> {
			AtomicInteger counter = channel.getUnackedBytes();
			int unackedCnt = counter.addAndGet(-bytes);
			totalBackupCounter.addAndGet(-bytes);

			if(channel.isUnderThreshold(unackedCnt) && connectionState.get() == BackflowState1.UNREGISTERED) {
				channel.registerForReads(() -> shouldRegister(channel));
			}
			
			if(t != null)
				apiLog.error(channel+" Exception on incoming data", t);
			return null;
		});
	}

	//We ONLY do registers/unregisters ON the SINGLE selector thread.
	//soooo, this function has to be given to the selector thread to run...
	private Boolean shouldRegister(BasChannelImpl basicChannel) {
		//  Because backpressure register socket is 
		//triggered from another thread.  We need to check the state of things to make sure we still want to register or not
		int unackedBytes = basicChannel.getUnackedBytes().get();
		if(!basicChannel.isUnderThreshold(unackedBytes)) {
			log.warn(basicChannel+" PSYCH....you thought you were caught up and are not!!!!!!   unackedBytes="+unackedBytes+" > readLevel="+basicChannel.getReadThreshold());
			return false; //don't register, we are NOT caught up (not sure how this happens)
		}

		AtomicReference<BackflowState1> connectionState = basicChannel.getCompareSetBackflowState();
		boolean unregister = connectionState.compareAndSet(BackflowState1.UNREGISTERED, BackflowState1.REGISTERED);
		if(!unregister) {
		    log.warn(basicChannel+" NOT catching up since connection state is already registered again");
			//There may be 1-N calls to channel.registerForReads(() -> checkForUnregister(channel)); BUT we only need to process register ONCE 
			return false;
		}

		backPressureRegisterSocket.increment();
		log.warn(basicChannel+" BOOM. you caught back up, reregistering for reads now. unackedCnt="+unackedBytes+" readThreshold="+basicChannel.getReadThreshold());
		return true;
	}
    
	private void write(SelectionKey key, ChannelInfo info) throws IOException, InterruptedException {
		if(log.isTraceEnabled())
			log.trace(info.getChannel()+"writing data");
		
		BasChannelImpl channel = (BasChannelImpl)info.getChannel();

		MDCUtil.setMDC(channel.isServerSide(), channel.getChannelId());

		if(log.isTraceEnabled())
			log.trace(channel+"notifying channel of write");

		try {
			channel.writeAll(key);
		} catch(NioClosedChannelException e) {
			//since it may close while someone is async writing, this is normal behavior so we swallow it and log as info
			log.info(channel+" Channel is closed so discarding the async writes");
		} finally {
			MDCUtil.clearMDC(channel.isServerSide());
		}
	}

	public void registerChannelOnThisThread(
			RegisterableChannelImpl channel, int validOps, Object listener, Supplier<Boolean> shouldRegister) {
		if(channel == null)
			throw new IllegalArgumentException("cannot register a null channel");
		else if(!Thread.currentThread().equals(selector.getThread()))
			throw new IllegalArgumentException("This function can only be invoked on PollingThread");
		else if(channel.isClosed())
			return; //do nothing if the channel is closed
		else if(!selector.isRunning())
			return; //do nothing if the selector is not running

		if(!shouldRegister.get())
			return;

		ChannelInfo struct;

		int previousOps = 0;
		if(log.isTraceEnabled())
			log.trace(channel+" registering ops="+OpType.opType(validOps));

		SelectionKey previous = channel.keyFor();
		if(previous == null) {
			struct = new ChannelInfo(channel);
		}else if(previous.attachment() == null) {
			struct = new ChannelInfo(channel);
			previousOps = previous.interestOps();
		} else {
			struct = (ChannelInfo)previous.attachment();
			previousOps = previous.interestOps();
		}
		struct.addListener(listener, validOps);
		int allOps = previousOps | validOps;
		SelectionKey key = channel.register(allOps, struct);
		channel.setKey(key);

		//log.info("registering="+Helper.opType(allOps)+" opsToAdd="+Helper.opType(validOps)+" previousOps="+Helper.opType(previousOps)+" type="+type);
		//log.info(channel+"registered2="+s+" allOps="+Helper.opType(allOps)+" k="+Helper.opType(key.interestOps()));
		if(log.isTraceEnabled())
			log.trace(channel+"registered2 allOps="+OpType.opType(allOps));
	}

	void unregisterSelectableChannel(RegisterableChannelImpl channel, int ops, SelectionKey theKey) {
		SelectorManager2 mgr = channel.getSelectorManager();
		SelectionKey key = channel.keyFor();
		long now = System.currentTimeMillis();
		long timeFromLastUnreg = now - channel.getLastUnregistrationTime();
		if(!Thread.currentThread().equals(mgr.getThread())) {
			throw new RuntimeException(channel + "Bug, changing selector keys can only be done " +
					"on registration thread because there is not synchronization");
		} else if(theKey != null) {
			if(theKey != key)
				throw new IllegalStateException("these keys should match");
		} else if(timeFromLastUnreg < 100) {
			throw new IllegalStateException("Time since last unrgister="+timeFromLastUnreg+" which should not be possible, possible bug?");
		}

		//this could be dangerous and result in deadlock....may want
		//to move this to the selector thread from jdk bugs!!!
		//but alas, follow KISS, move on...
        if(key == null || !key.isValid()) //no need to unregister, key is cancelled
            return;

		int previous = key.interestOps();
		int opsNow = previous & ~ops; //subtract out the operation
		key.interestOps(opsNow);

		//log.info(channel+" unregistering="+Helper.opType(opsNow)+" opToSubtract="+Helper.opType(ops)+" previous="+Helper.opType(previous)+" type="+type);
		
		//make sure we remove the appropriate listener and clean up
		if(key.attachment() != null) {
			ChannelInfo struct = (ChannelInfo)key.attachment();
			struct.removeListener(ops);
		}
	}
}
