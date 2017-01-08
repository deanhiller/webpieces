package org.webpieces.frontend.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.Http2EngineFactory;
import org.webpieces.httpcommon.api.Http2ServerEngine;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;


class HttpServerSocketImpl implements HttpServerSocket {
    private Channel channel;
    private DataListener dataListener;
    private ResponseSender responseSender;
    private HpackParser http2Parser;
    private TimedRequestListener timedRequestListener;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private Http2ServerEngine http2ServerEngine;
    private FrontendConfig frontendConfig;
    private static final Logger log = LoggerFactory.getLogger(HttpServerSocket.class);

    HttpServerSocketImpl(Channel channel, DataListener http11DataListener, ResponseSender http11ResponseSender,
    		HpackParser http2Parser,
                         TimedRequestListener timedRequestListener,
                         FrontendConfig frontendConfig) {
        this.channel = channel;
        this.dataListener = http11DataListener;
        this.responseSender = http11ResponseSender;
        this.http2Parser = http2Parser;
        this.timedRequestListener = timedRequestListener;
        this.frontendConfig = frontendConfig;
    }

    @Override
    public void upgradeHttp2(Optional<String> maybeSettingsPayload) {
        http2ServerEngine = Http2EngineFactory.createHttp2ServerEngine(http2Parser, channel, channel.getRemoteAddress(), frontendConfig.getHttp2Settings());
        http2ServerEngine.setRequestListener(timedRequestListener);
        dataListener = http2ServerEngine.getDataListener();
        responseSender = http2ServerEngine.getResponseSender();

        maybeSettingsPayload.ifPresent(settingsPayload ->
        {
            try {
            	SettingsFrame settingsFrame = new SettingsFrame();
                List<Http2Setting> headers = http2Parser.unmarshalSettingsPayload(settingsPayload);
                settingsFrame.setSettings(headers);

                http2ServerEngine.setRemoteSettings(settingsFrame, false);
            } catch (Exception e) {
                log.error("Unable to parse initial settings payload: 0x" + settingsPayload, e);
            }
        });
    }

    @Override
    public void sendLocalRequestedSettings() {
        log.info("Sending local requested settings");
        http2ServerEngine.sendLocalRequestedSettings();
    }

    @Override
    public CompletableFuture<Void> closeSocket() {
        // TODO: tell the http engine that we're closing the socket so it can send
        // GoAway and whatnot?
        return channel.close().thenAccept(c -> {});
    }

    @Override
    public Channel getUnderlyingChannel() {
        return channel;
    }

    @Override
    public ResponseSender getResponseSender() {
        return responseSender;
    }

    @Override
    public DataListener getDataListener() {
        return dataListener;
    }
    
	@Override
	public String toString() {
		return "HttpSvrSocket["+channel+"]";
	}
}
