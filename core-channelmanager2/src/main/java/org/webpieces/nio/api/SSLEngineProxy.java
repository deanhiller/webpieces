package org.webpieces.nio.api;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import org.eclipse.jetty.alpn.ALPN;

public class SSLEngineProxy {

	private List<String> protocols;
	private boolean unsupported;
	private String protocol;
	private boolean isClient;
	private SSLEngine engine;

	public SSLEngineProxy(SSLEngine engine, boolean isClient, String ... protocols) {
		this.engine = engine;
		this.protocols = Arrays.asList(protocols);
		this.isClient = isClient;
	}
	
	public SSLEngine getSslEngine() {
		
		
		if(isClient) {
			ALPN.put(engine, new ALPN.ClientProvider() {
				@Override
				public List<String> protocols() {
					return protocols;
				}
				@Override
				public void selected(String protocol) throws SSLException {
					SSLEngineProxy.this.protocol = protocol;
					ALPN.remove(engine);
				}
				@Override
				public void unsupported() {
					unsupported = true;
					ALPN.remove(engine);
				}
			});
		} else {
			ALPN.put(engine, new ALPN.ServerProvider()
			{
			    @Override
			    public void unsupported()
			    {
			    	unsupported = true;
			        ALPN.remove(engine);
			    }
			    @Override
			    public String select(List<String> protocols)
			    {
			        ALPN.remove(engine);
			        return protocols.get(0);
			    }
			});
		}
		
		return engine;
	}

	public String getResolvedProtocol() {
		if(unsupported)
			return null;
		else if(protocol == null)
			throw new IllegalStateException("called this method too early, handshake probably not complete yet");
		return protocol;
	}
}
