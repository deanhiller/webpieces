package org.webpieces.nio.impl.cm.basic;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.impl.cm.basic.udp.DatagramChannelImpl;
import org.webpieces.nio.impl.cm.basic.udp.UDPChannelImpl;

import io.micrometer.core.instrument.MeterRegistry;



/**
 * @author Dean Hiller
 */
class BasChannelService implements ChannelManager {

	private SelectorManager2 selMgr;
    private JdkSelect selector;
	private boolean started;
	private BufferPool pool;
	private KeyProcessor processor;
	private BackpressureConfig config;
	private String name;

	BasChannelService(String name, JdkSelect apis, BufferPool pool, BackpressureConfig config, MeterRegistry metrics) {
		this.name = name;
		if(config == null)
			throw new IllegalArgumentException("config must be supplied");
		this.pool = pool;
		this.config = config;
		processor = new KeyProcessor(name, apis, pool, metrics);
		selMgr = new SelectorManager2(apis, processor, name);
        this.selector = apis;
        start();
	}
	
	@Override
    public TCPServerChannel createTCPServerChannel(String id, ConnectionListener listener) {
        preconditionChecks(id);
        if(listener == null)
        	throw new IllegalArgumentException("connectionListener cannot be null");
        String fullId = name+"."+id;
        return new BasTCPServerChannel(fullId, selector, selMgr, processor, listener, pool, config);
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		throw new UnsupportedOperationException("SSL not supported at this level.");
	}
	
	@Override
	public TCPServerChannel createTCPUpgradableChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		throw new UnsupportedOperationException("SSL not supported at this level.");		
	}
	
	private void preconditionChecks(String id) {
		if(id == null)
            throw new IllegalArgumentException("id cannot be null");
		else if(!started)
			throw new IllegalStateException("Call start() on the ChannelManagerService first");
	}
	@Override
    public TCPChannel createTCPChannel(String id) {
        preconditionChecks(id);
        String fullId = name+"."+id;
        return new BasTCPChannel(fullId, selector, selMgr, processor, pool, config);
	}

	@Override
	public TCPChannel createTCPChannel(String id, SSLEngine engine) {
		throw new UnsupportedOperationException("SSL not supported at this level.");
	}
	
	@Override
    public UDPChannel createUDPChannel(String id) {
        preconditionChecks(id);
        String fullId = name+"."+id;
        return new UDPChannelImpl(fullId, selector, selMgr, processor, pool, config);
    }
    
	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener dataListener) {
        if(dataListener == null)
        	throw new IllegalArgumentException("dataListener cannot be null");
        String fullId = name+"."+id;
        return new DatagramChannelImpl(fullId, bufferSize, dataListener);
    }
    
	public void start() {
		started = true;
		selMgr.start();
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManager#shutdown()
	 */
	public void stop() {
		started = false;
		selMgr.stop();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
}
