package org.webpieces.asyncserver.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyDataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(ProxyDataListener.class);
	private static final String EXISTING_PROXY_CHANNEL = "_existingProxyChannel";
	private ConnectedChannels connectedChannels;
	private AsyncDataListener dataListener;

	public ProxyDataListener(ConnectedChannels connectedChannels, AsyncDataListener dataListener) {
		this.connectedChannels = connectedChannels;
		this.dataListener = dataListener;
	}

	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		TCPChannel proxy = lookupExistingOrCreateNew(channel);
		return dataListener.incomingData(proxy, b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		log.info("async server far end closed");
		if(log.isDebugEnabled())
			log.debug(channel+"far end closed");
		connectedChannels.removeChannel((TCPChannel) channel);
		TCPChannel proxy = lookupExistingOrCreateNew(channel);
		dataListener.farEndClosed(proxy);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		TCPChannel proxy = lookupExistingOrCreateNew(channel);
		dataListener.failure(proxy, data, e);
	}

	/** 
	 * We have two choices here.
	 * 1. implement equals and hashCode in ProxyTCPChannel to delegate to TCPChannel so as we
	 *    create new ones, they are equal if the Channel they wrap is equal
	 * 2. re-use the same proxy we created for this channel by sticking it in the channel session
	 *    which avoids creating new objects that need to be garbage collected every time data
	 *    comes in
	 *       
	 * @param channel
	 * @return
	 */
	private TCPChannel lookupExistingOrCreateNew(Channel channel) {
		ChannelSession session = channel.getSession();
		//This is garbage collected when the TCPChannel and it's ChannelSession are garbage
		//collected...
		ProxyTCPChannel existingProxy = (ProxyTCPChannel) session.get(EXISTING_PROXY_CHANNEL);
		if(existingProxy == null) {
			existingProxy = new ProxyTCPChannel((TCPChannel) channel, connectedChannels);
			session.put(EXISTING_PROXY_CHANNEL, existingProxy);
		}
		
		return existingProxy;
	}

	public void connectionOpened(Channel channel, boolean isReadyForWrites) {
		if(log.isDebugEnabled())
			log.debug("connection opened");
		TCPChannel proxy = lookupExistingOrCreateNew(channel);
		dataListener.connectionOpened(proxy, isReadyForWrites);
	}

}
