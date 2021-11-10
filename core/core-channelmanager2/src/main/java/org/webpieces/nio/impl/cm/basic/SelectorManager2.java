/*
Copyright (c) 2002, Dean Hiller
All rights reserved.

*****************************************************************
IF YOU MAKE CHANGES TO THIS CODE AND DO NOT POST THEM, YOU 
WILL BE IN VIOLATION OF THE LICENSE I HAVE GIVEN YOU.  Contact
me at deanhiller@users.sourceforge.net if you need a different
license.
*****************************************************************

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Set;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.api.jdk.Keys;
import org.webpieces.nio.api.jdk.SelectorListener;


public class SelectorManager2 implements SelectorListener {
//--------------------------------------------------------------------
//	FIELDS/MEMBERS
//--------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SelectorManager2.class);
	
    private JdkSelect selector;

	private ConcurrentLinkedDeque<ChannelRegistrationListener> listenerList = new ConcurrentLinkedDeque<>();
    //flag for logs so we know that the selector was woken up to close a socket.
    //this is needed as we want to see in the logs the reason of every selector fire clearly
	private boolean needCloseOrRegister;

	private boolean stopped;

	private String threadName;

	private KeyProcessor helper;

//--------------------------------------------------------------------
//	CONSTRUCTORS
//--------------------------------------------------------------------

	public SelectorManager2(JdkSelect select, KeyProcessor helper, String threadName) {
        this.selector = select;
		this.helper = helper;
        this.threadName = threadName;
	}
//--------------------------------------------------------------------
//	BUSINESS METHODS
//--------------------------------------------------------------------
	public synchronized void start() {        
        selector.startPollingThread(this, threadName);
	}

	/**
	 */
	public synchronized void stop() {
        try {
            if(!isRunning())
                return;
            stopped = true;
            selector.stopPollingThread();
        } catch(Throwable e) {
            //there is nothing a client can do to a recover from this so swallow it for them
            log.error("Exception stopping selector", e);
        }
	}
	
	JdkSelect getSelector() {
		return selector;
	}
	
	public XFuture<Void> registerServerSocketChannel(BasTCPServerChannel s, ConnectionListener listener) 
					throws IOException, InterruptedException {
		return asyncRegister(s, SelectionKey.OP_ACCEPT, listener, () -> true);
	}
	public XFuture<Channel> registerChannelForConnect(final RegisterableChannelImpl s)
					throws IOException, InterruptedException {
		XFuture<Channel> future = new XFuture<Channel>();
		registerSelectableChannel(s, SelectionKey.OP_CONNECT, future, () -> true);
		return future;
	}
	
	public XFuture<Void> registerChannelForRead(final RegisterableChannelImpl s, final DataListener listener, Supplier<Boolean> shouldRegister)
					throws IOException, InterruptedException {
		return registerSelectableChannel(s, SelectionKey.OP_READ, listener, shouldRegister);
	}
	public XFuture<Void> unregisterChannelForRead(BasChannelImpl c) throws IOException, InterruptedException {
		return unregisterSelectableChannel(c, SelectionKey.OP_READ);
	}
	
	private XFuture<Void> unregisterSelectableChannel(RegisterableChannelImpl channel, int ops) 
											throws IOException, InterruptedException {
		if(stopped) 
			return XFuture.completedFuture(null); //do nothing if stopped
		else if(!isRunning())
			throw new IllegalStateException("ChannelMgr is not running, call ChannelManager.start first");
		else if(Thread.currentThread().equals(selector.getThread())) {
			helper.unregisterSelectableChannel(channel, ops);
			return XFuture.completedFuture(null);
		} else
			return asynchUnregister(channel, ops);			
	}
	
	XFuture<Void> registerSelectableChannel(final RegisterableChannelImpl s, final int validOps, final Object listener, Supplier<Boolean> shouldRegister) {	
		if(stopped)  {
			XFuture<Void> future = new XFuture<Void>();
			future.completeExceptionally(new IllegalStateException("This chanMgr is stopped")); //do nothing if stopped
			return future;
		} else if(!isRunning())
			throw new IllegalStateException("ChannelMgr is not running, call ChannelManager.start first");
		else if(Thread.currentThread().equals(selector.getThread())) {
			registerChannelOnThisThread(s, validOps, listener, shouldRegister);
			return XFuture.completedFuture(null);
		} else
			return asyncRegister(s, validOps, listener, shouldRegister);
	}

	private void registerChannelOnThisThread(
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

	private XFuture<Void> asynchUnregister(final RegisterableChannelImpl s, final int validOps) 
									throws IOException, InterruptedException {
		if (s.isBlocking())
			throw new IllegalArgumentException(s+"Only non-blocking selectable channels can be used.  " +
					"please call SelectableChannel.configureBlocking before passing in the channel");

		//log.info("asyn UNregister="+Helper.opType(validOps));
		
		XFuture<Void> future = new XFuture<Void>();
		//This is a 12 year old statement and maybe not true anymore...
		// in SelctorImpl.java(check sun's SCSL code), SelectorImpl grabs
		// a lock in select() method. This register tries to grab the same
		// lock, so we need to wakeup the select method here and have it do
		// the processing of this request so it can release the lock so
		// the register can grab the lock.
		ChannelRegistrationListener r = new ChannelRegistrationListener(future, validOps) {
			public void run() {
				helper.unregisterSelectableChannel(s, validOps);
			}
		};
        
		listenerList.add(r);

		if(log.isTraceEnabled())
			log.trace(s+"call wakeup on selector to register for="+r);
		wakeUpSelector();
		
		return future;
	}
	
	private XFuture<Void> asyncRegister(
			final RegisterableChannelImpl s, final int validOps, final Object listener, final Supplier<Boolean> causedByBackPressureForReads) {
		if(s.isBlocking()) 
			throw new IllegalArgumentException(s+"Only non-blocking selectable channels can be used.  " +
					"please call SelectableChannel.configureBlocking before passing in the channel");

		XFuture<Void> future = new XFuture<Void>();
		//This is a 12 year old statement and maybe not true anymore...
		//in SelectorImpl.java(check sun's SCSL code), SelectorImpl grabs
		//a lock in select() method.  This register tries to grab the same
		//lock, so we need to wakeup the select method here and have it do
		//the processing of this request so it can release the lock so
		//the register can grab the lock.
		ChannelRegistrationListener r = new ChannelRegistrationListener(future, validOps) {
			@Override
			public void run() {
//				if((validOps & SelectionKey.OP_READ) > 0)
//					log.info("really registering for reads");
				registerChannelOnThisThread(s, validOps, listener, causedByBackPressureForReads);
			}
		};
		
		listenerList.add(r);

		if(log.isTraceEnabled())
			log.trace(s+"call wakeup on selector to register for="+r);
		wakeUpSelector();
		
		return future;
	}
    
    public void selectorFired()
    {
        //NOTE: we have to process registrations as close to waitOnSelector as possible.
        //otherwise, we could end up waiting on the selector when there are registrations to
        //process!!!!
        //currently, this processes the registration requests.
        fireToListeners();
        
		if(log.isTraceEnabled())
			log.trace("coming into select");
		
		final Keys keys = selector.select();

		if(log.isTraceEnabled())
		log.trace("coming out of select with newkeys="+keys.getKeyCount()+" setSize="+keys.getSelectedKeys().size()+
							" regCnt="+listenerList.size()+" needCloseOrRegister="+needCloseOrRegister+
							" wantShutdown="+selector.isWantShutdown());
        
        Set<SelectionKey> keySet = keys.getSelectedKeys();  
        
        needCloseOrRegister = false;
        if(keySet.size() > 0) {
        	helper.processKeys(keySet);
        }
    }
	
	private void fireToListeners() {
//		//ChannelRegistrationListener[] listeners = listenerList.getListeners(ChannelRegistrationListener.class);
//		for(int i = 0; i < listeners.length; i++) {
//			listenerList.remove(ChannelRegistrationListener.class, listeners[i]);
//			listeners[i].processRegistrations();
//		}
		
		while(!listenerList.isEmpty()) {
			ChannelRegistrationListener l = listenerList.poll();
			l.processRegistrations();
		}
	}

	/**
	 * Unfortunately, previously, a registered Socket cannot close if the PollingThread is
	 * hung on the selector.  A registered socket is closed on entry to the 
	 * select or selectNow function, therefore, we just wake up the selector
	 * so we reenter him to close the socket.
	 * 
	 * Also, this is used to wakeup the selector to process registrations!!!
	 */
	public void wakeUpSelector() {
		if(log.isTraceEnabled())
			log.trace("Wakeup selector to enable close or registers");
		needCloseOrRegister = true;
		selector.wakeup();
	}
	public Object getThread() {
		return selector.getThread();
	}
	public boolean isRunning() {
        if(selector == null)
            return false;
		return selector.isRunning();
	}
	
}