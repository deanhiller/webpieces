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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.RuntimeInterruptedException;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.testutil.nioapi.ChannelRegistrationListener;
import org.webpieces.nio.api.testutil.nioapi.Select;
import org.webpieces.nio.api.testutil.nioapi.SelectorListener;
import org.webpieces.nio.api.testutil.nioapi.SelectorProviderFactory;


public class SelectorManager2 implements SelectorListener {
//--------------------------------------------------------------------
//	FIELDS/MEMBERS
//--------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(SelectorManager2.class);
	
    private Select selector;
    private SelectorProviderFactory factory;

    private Object id;
	private EventListenerList listenerList = new EventListenerList();
    //flag for logs so we know that the selector was woken up to close a socket.
    //this is needed as we want to see in the logs the reason of every selector fire clearly
	private boolean needCloseOrRegister;

	private boolean stopped;

	private BufferCreationPool pool;

//--------------------------------------------------------------------
//	CONSTRUCTORS
//--------------------------------------------------------------------

	public SelectorManager2(SelectorProviderFactory factory, Object id, BufferCreationPool pool) {
	  	this.id = id;
        this.factory = factory;
        this.pool = pool;
	}
//--------------------------------------------------------------------
//	BUSINESS METHODS
//--------------------------------------------------------------------
	public synchronized void start() {        
        selector = factory.provider(id+"");
        
        selector.startPollingThread(this);

        selector.setRunning(true);
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
            log.warn("Exception stopping selector", e);
        }
	}
	
	Select getSelector() {
		return selector;
	}
	
	public void registerServerSocketChannel(BasTCPServerChannel s, ConnectionListener listener) 
					throws IOException, InterruptedException {
		waitForRegister(s, SelectionKey.OP_ACCEPT, listener, true);
	}
	public void registerChannelForConnect(final RegisterableChannelImpl s, CompletableFuture<Channel> promise)
					throws IOException, InterruptedException {
		final ConnectionListener listener = new FutureConnectImpl(promise);
		registerSelectableChannel(s, SelectionKey.OP_CONNECT, listener, true);
	}
	public void registerChannelForRead(final RegisterableChannelImpl s, final DataListener listener)
					throws IOException, InterruptedException {
		registerSelectableChannel(s, SelectionKey.OP_READ, listener, true);
	}
	public void unregisterChannelForRead(BasChannelImpl c) throws IOException, InterruptedException {
		unregisterSelectableChannel(c, SelectionKey.OP_READ);
	}
	
	private void unregisterSelectableChannel(RegisterableChannelImpl channel, int ops) 
											throws IOException, InterruptedException {
		if(stopped) 
			return; //do nothing if stopped
		else if(!isRunning())
			throw new IllegalStateException("ChannelMgr is not running, call ChannelManager.starimport " +
                    "biz.xsoftware.api.nio.test.nioapi.SelectKey;t first");
		else if(Thread.currentThread().equals(selector.getThread()))
			Helper.unregisterSelectableChannel(channel, ops);
		else
			asynchUnregister(channel, ops);			
	}
	
	void registerSelectableChannel(final RegisterableChannelImpl s, final int validOps, final Object listener, boolean needWait) {	
		if(stopped) 
			return; //do nothing if stopped
		if(!isRunning())
			throw new IllegalStateException("ChannelMgr is not running, call ChannelManager.start first");
		else if(Thread.currentThread().equals(selector.getThread()))
			registerChannelOnThisThread(s, validOps, listener);
		else
			waitForRegister(s, validOps, listener, needWait);
	}

	private void registerChannelOnThisThread(
			RegisterableChannelImpl channel, int validOps, Object listener) {
		if(channel == null)
			throw new IllegalArgumentException("cannot register a null channel");
		else if(!Thread.currentThread().equals(selector.getThread()))
			throw new IllegalArgumentException("This function can only be invoked on PollingThread");
		else if(channel.isClosed())
			return; //do nothing if the channel is closed
		else if(!selector.isRunning())
			return; //do nothing if the selector is not running
		
		WrapperAndListener struct;
		SelectableChannel s = channel.getRealChannel();
		
		int previousOps = 0;
		if (log.isTraceEnabled())
			log.trace(channel+"registering2="+s+" ops="+Helper.opType(validOps));
        
		SelectionKey previous = channel.keyFor(selector);
		if(previous == null) {
			struct = new WrapperAndListener(channel);
		}else if(previous.attachment() == null) {
			struct = new WrapperAndListener(channel);
			previousOps = previous.interestOps();
		} else {
			struct = (WrapperAndListener)previous.attachment();
			previousOps = previous.interestOps();
		}
		struct.addListener(id, listener, validOps);
		int allOps = previousOps | validOps;
		SelectionKey key = channel.register(selector, allOps, struct);
		channel.setKey(key);
		if (log.isTraceEnabled())
			log.trace(channel+"registered2="+s+" allOps="+Helper.opType(allOps));		
	}

	private void asynchUnregister(final RegisterableChannelImpl s, final int validOps) 
									throws IOException, InterruptedException {
		if (s.isBlocking())
			throw new IllegalArgumentException(s+"Only non-blocking selectable channels can be used.  " +
					"please call SelectableChannel.configureBlocking before passing in the channel");

		// in SelctorImpl.java(check sun's SCSL code), SelectorImpl grabs
		// a lock in select() method. This register tries to grab the same
		// lock, so we need to wakeup the select method here and have it do
		// the processing of this request so it can release the lock so
		// the register can grab the lock.
		Runnable r = new Runnable() {
			public void run() {
				Helper.unregisterSelectableChannel(s, validOps);
			}

			public String toString() {
				return Helper.opType(validOps);
			}
		};
        
        ChannelRegistrationListener regRequest = selector.createRegistrationListener(s, r, this);

		listenerList.add(ChannelRegistrationListener.class, regRequest);

		regRequest.waitForFinish(true);
	}
	
	private void waitForRegister(final RegisterableChannelImpl s, final int validOps, final Object listener, boolean needWait) {
		if(s.isBlocking()) 
			throw new IllegalArgumentException(s+"Only non-blocking selectable channels can be used.  " +
					"please call SelectableChannel.configureBlocking before passing in the channel");

		//in SelectorImpl.java(check sun's SCSL code), SelectorImpl grabs
		//a lock in select() method.  This register tries to grab the same
		//lock, so we need to wakeup the select method here and have it do
		//the processing of this request so it can release the lock so
		//the register can grab the lock.
		Runnable r = new Runnable() {
			public void run() {
				registerChannelOnThisThread(s, validOps, listener);
			}
			public String toString() {
				return Helper.opType(validOps);
			}
		};
		ChannelRegistrationListener regRequest = selector.createRegistrationListener(s, r, this);
		
		listenerList.add(ChannelRegistrationListener.class, regRequest);
		
		try {
			regRequest.waitForFinish(needWait);
		} catch (ClosedChannelException e) {
			throw new NioClosedChannelException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e.getMessage(), e);
		}
	}
    
    public void selectorFired()
    {
        //NOTE: we have to process registrations as close to waitOnSelector as possible.
        //otherwise, we could end up waiting on the selector when there are registrations to
        //process!!!!
        //currently, this processes the registration requests.
        fireToListeners();
        
        waitOnSelector();  
        
        //processKeys may will go through all keys and do some work, but 
        //may not do all work it could on each key.  This is to keep from any
        //client starving and from registrations starving in the case where one
        //client just overloads the server and keeps sending a stream of data.
        //he will be served with all the others in an equal priority round robin
        //fashion.
        Set<SelectionKey> keySet = selector.selectedKeys();
        if(log.isTraceEnabled())
        	log.trace(id+"keySetCnt="+keySet.size()+" registerCnt="+listenerList.getListenerCount()
        			+" needCloseOrRegister="+needCloseOrRegister+" wantShutdown="+selector.isWantShutdown());
        needCloseOrRegister = false;
        if(keySet.size() > 0) {
        	Helper.processKeys(id, keySet, this, pool);
        }
    }
	
	protected int waitOnSelector() {
		int numNewKeys = 0;
		if(log.isTraceEnabled())
			log.trace(id+"coming into select");
		numNewKeys = selector.select();
//should assert we are not stopping, have listeners, or have keys to process...

		if(log.isTraceEnabled())
			log.trace(id+"coming out of select with newkeys="+numNewKeys+
					" regCnt="+listenerList.getListenerCount()+" needCloseOrRegister="+needCloseOrRegister+
					" wantShutdown="+selector.isWantShutdown());

//			assert numNewKeys > 0 || listenerList.getListenerCount() > 0 || selector.isWantShutdown() : 
//				"Should only wakeup when we have stuff to do";
			
		return numNewKeys;
	}

	private void fireToListeners() {
		ChannelRegistrationListener[] listeners = listenerList.getListeners(ChannelRegistrationListener.class);
		for(int i = 0; i < listeners.length; i++) {
			listenerList.remove(ChannelRegistrationListener.class, listeners[i]);
			listeners[i].processRegistrations();
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
			log.trace(id+"Wakeup selector to enable close or registers");
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