package org.webpieces.frontend.impl;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.Http2Settings;
import com.webpieces.http2parser.impl.SettingsMarshaller;

import org.webpieces.data.api.DataWrapper;
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

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.SERVER;


class HttpServerSocketImpl implements HttpServerSocket {
    private Channel channel;
    private DataListener dataListener;
    private ResponseSender responseSender;
    private Http2Parser http2Parser;
    private TimedRequestListener timedRequestListener;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private Http2ServerEngine http2ServerEngine;
    private FrontendConfig frontendConfig;
    private static final Logger log = LoggerFactory.getLogger(HttpServerSocket.class);

    HttpServerSocketImpl(Channel channel, DataListener http11DataListener, ResponseSender http11ResponseSender,
                         Http2Parser http2Parser,
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
    public void upgradeHttp2(Optional<ByteBuffer> maybeSettingsPayload) {
        http2ServerEngine = Http2EngineFactory.createHttp2ServerEngine(http2Parser, channel, channel.getRemoteAddress(), frontendConfig.getHttp2Settings());
        http2ServerEngine.setRequestListener(timedRequestListener);
        dataListener = http2ServerEngine.getDataListener();
        responseSender = http2ServerEngine.getResponseSender();

        maybeSettingsPayload.ifPresent(settingsPayload ->
        {
            try {
                Http2Settings settingsFrame = new Http2Settings();
                SettingsMarshaller settingsMarshaller = (SettingsMarshaller) http2Parser.getMarshaller(Http2Settings.class);
                Optional<DataWrapper> maybePayload;
                if(settingsPayload.hasRemaining())
                    maybePayload = Optional.of(dataGen.wrapByteBuffer(settingsPayload));
                else
                    maybePayload = Optional.empty();
                settingsMarshaller.unmarshalFlagsAndPayload(settingsFrame, (byte) 0x0, maybePayload);

                http2ServerEngine.setRemoteSettings(settingsFrame, false);
            } catch (Exception e) {
                log.error("Unable to parse initial settings payload: 0x" + DatatypeConverter.printHexBinary(settingsPayload.array()), e);
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
}
