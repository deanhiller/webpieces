package org.webpieces.frontend.impl;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.Http2Settings;
import com.webpieces.http2parser.impl.SettingsMarshaller;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.Http2Engine;
import org.webpieces.httpcommon.api.Http2EngineFactory;
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


public class HttpServerSocketImpl implements HttpServerSocket {
    private Channel channel;
    private DataListener dataListener;
    private ResponseSender responseSender;
    private Http2Parser http2Parser;
    private TimedRequestListener timedRequestListener;
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private Http2Engine http2Engine;
    private static final Logger log = LoggerFactory.getLogger(HttpServerSocket.class);

    public HttpServerSocketImpl(Channel channel, DataListener http11DataListener, ResponseSender http11ResponseSender,
                                Http2Parser http2Parser,
                                TimedRequestListener timedRequestListener) {
        this.channel = channel;
        this.dataListener = http11DataListener;
        this.responseSender = http11ResponseSender;
        this.http2Parser = http2Parser;
        this.timedRequestListener = timedRequestListener;
    }

    @Override
    public void upgradeHttp2(Optional<ByteBuffer> maybeSettingsPayload) {
        http2Engine = Http2EngineFactory.createHttp2Engine(http2Parser, channel, channel.getRemoteAddress(), SERVER);
        http2Engine.setRequestListener(timedRequestListener);
        dataListener = http2Engine.getDataListener();
        responseSender = http2Engine.getResponseSender();

        maybeSettingsPayload.ifPresent(settingsPayload ->
        {
            try {
                Http2Settings settingsFrame = new Http2Settings();
                SettingsMarshaller settingsMarshaller = (SettingsMarshaller) http2Parser.getMarshaller(Http2Settings.class);
                settingsMarshaller.unmarshalFlagsAndPayload(settingsFrame, (byte) 0x0, Optional.of(dataGen.wrapByteBuffer(settingsPayload)));

                http2Engine.setRemoteSettings(settingsFrame, false);
            } catch (Exception e) {
                log.error("Unable to parse initial settings payload: 0x" + DatatypeConverter.printHexBinary(settingsPayload.array()), e);
            }
        });
    }

    @Override
    public void sendLocalPreferredSettings() {
        http2Engine.sendLocalPreferredSettings();
    }

    @Override
    public CompletableFuture<Void> closeSocket() {
        // TODO: implement this
        return null;
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
