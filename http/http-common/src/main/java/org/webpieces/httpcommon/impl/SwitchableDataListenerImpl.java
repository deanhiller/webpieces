package org.webpieces.httpcommon.impl;

import static org.webpieces.httpcommon.api.Protocol.HTTP11;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.httpcommon.api.ServerListener;
import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.Protocol;
import org.webpieces.httpcommon.api.SwitchableDataListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;


public class SwitchableDataListenerImpl implements SwitchableDataListener {
    private static final Logger log = LoggerFactory.getLogger(SwitchableDataListenerImpl.class);

    private Protocol protocol = HTTP11;
    private Map<Protocol, DataListener> dataListenerMap = new HashMap<>();
    private HttpSocket socket;
    private ServerListener closeListener;

    public SwitchableDataListenerImpl(HttpSocket socket, ServerListener closeListener) {
        this.socket = socket;
        this.closeListener = closeListener;
    }

    public SwitchableDataListenerImpl(HttpSocket socket) {
        this.socket = socket;
    }

    @Override
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void put(Protocol protocol, DataListener listener) {
        dataListenerMap.put(protocol, listener);
    }

    @Override
    public DataListener getDataListener(Protocol protocol) {
        return dataListenerMap.get(protocol);
    }

    @Override
    public void incomingData(Channel channel, ByteBuffer b) {
        dataListenerMap.get(protocol).incomingData(channel, b);
    }

    @Override
    public void farEndClosed(Channel channel) {
        log.info("far end closed");
        socket.closeSocket();

        if(closeListener != null)
            closeListener.farEndClosed(socket);

        // call farEndClosed on every protocol
        for(Map.Entry<Protocol, DataListener> entry: dataListenerMap.entrySet()) {
            entry.getValue().farEndClosed(channel);
        }
    }

    @Override
    public void failure(Channel channel, ByteBuffer data, Exception e) {
        log.error("Failure on channel="+channel, e);

        // Call failure on every protocol
        for(Map.Entry<Protocol, DataListener> entry: dataListenerMap.entrySet()) {
            entry.getValue().failure(channel, data, e);
        }
    }

    @Override
    public void applyBackPressure(Channel channel) {
        dataListenerMap.get(protocol).applyBackPressure(channel);
    }

    @Override
    public void releaseBackPressure(Channel channel) {
        dataListenerMap.get(protocol).releaseBackPressure(channel);
    }
}

