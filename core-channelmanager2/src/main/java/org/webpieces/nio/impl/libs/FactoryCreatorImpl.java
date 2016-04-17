package org.webpieces.nio.impl.libs;

import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.libs.AsyncSSLEngine;
import org.webpieces.nio.api.libs.BufferFactory;
import org.webpieces.nio.api.libs.ChannelSession;
import org.webpieces.nio.api.libs.FactoryCreator;
import org.webpieces.nio.api.libs.StartableExecutorService;
import org.webpieces.nio.api.libs.StartableRouterExecutor;


public class FactoryCreatorImpl extends FactoryCreator {

	private static final byte[] DEFAULT_SEPARATOR = new byte[] { Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE };

	@Override
	public void configure(Map<String, Object> map) {
	}

	@Override
	public BufferFactory createBufferFactory(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("map cannot be null");

		Object direct = map.get(FactoryCreator.KEY_IS_DIRECT);
		boolean isDirect = false;
		if(direct != null && direct instanceof Boolean)
			isDirect = (Boolean)direct;
		
		DefaultByteBufferFactory bufFactory = new DefaultByteBufferFactory();
		bufFactory.setDirect(isDirect);
		return bufFactory;
	}

	@Override
	public StartableExecutorService createExecSvcFactory(Map<String, Object> map) {
		int numThreads = 10;
		if(map != null) {
			Object num = map.get(FactoryCreator.KEY_NUM_THREADS);
			if(num == null) {
			} else if(!(num instanceof Integer))
				throw new IllegalArgumentException("key=FactoryCreator.KEY_NUM_THREADS must contain an Integer");
			else
				numThreads = (Integer)num;
		}
		return new ExecutorServiceProxy(numThreads);
	}

    /**
     * @see org.webpieces.nio.api.libs.FactoryCreator#createAdvancedExecSvc(java.util.Map)
     */
    @Override
    public StartableExecutorService createAdvancedExecSvc(Map<String, Object> map)
    {
        int numThreads = 10;
        if(map != null) {
            Object num = map.get(FactoryCreator.KEY_NUM_THREADS);
            if(num == null) {
            } else if(!(num instanceof Integer))
                throw new IllegalArgumentException("key=FactoryCreator.KEY_NUM_THREADS must contain an Integer");
            else
                numThreads = (Integer)num;
        }
        return new AdvancedExecutorService(numThreads);
    }
    
	@Override
	public AsyncSSLEngine createSSLEngine(Object id, SSLEngine engine, Map<String, Object> map) {
		return new AsynchSSLEngineImpl(id+"", engine);
	}

	@Override
	public ChannelSession createSession(RegisterableChannel channel) {
		return new ChannelSessionImpl(channel);
	}

	@Override
	public StartableRouterExecutor createRoutingExecutor(String name, int numThreads) {
		return new RoutingExecutor(name, numThreads);
	}

}
