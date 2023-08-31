package org.webpieces.auth0.impl;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.auth0.client.api.*;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http.StatusCode;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.microsvc.client.api.ClientSSLEngineFactory;
import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLEngine;
import java.nio.charset.Charset;

@Singleton
public class AuthApiImpl implements AuthApi {
    private static final Logger log = LoggerFactory.getLogger(AuthApiImpl.class);
    private final HostWithPort hostWithPort;

    private DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private Http2Client client;
    private ClientSSLEngineFactory sslFactory;
    private JacksonJsonConverter jsonMapper;

    @Inject
    public AuthApiImpl(
            Auth0ApiConfig auth0Config,
            Http2Client client,
            ClientSSLEngineFactory sslFactory,
            JacksonJsonConverter jsonMapper
    ) {
        hostWithPort = new HostWithPort(auth0Config.getAuth0Domain(), 443);
        this.client = client;
        this.sslFactory = sslFactory;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public XFuture<AuthResponse> codeToTokens(AuthRequest request) {
        SSLEngine engine = sslFactory.createEngine(hostWithPort.getHostOrIpAddress(), hostWithPort.getPort());

        String jsonData = jsonMapper.writeValueAsString(request);
        byte[] bytes = jsonData.getBytes(Charset.defaultCharset());
        DataWrapper wrapper = wrapperGen.wrapByteArray(bytes);

        Http2Request headers = new Http2Request();
        headers.addHeader(new Http2Header(Http2HeaderName.SCHEME, "https"));
        headers.addHeader(new Http2Header(Http2HeaderName.METHOD, "POST"));
        headers.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, hostWithPort.getHostOrIpAddress()+":"+hostWithPort.getPort()));
        headers.addHeader(new Http2Header(Http2HeaderName.PATH, "/oauth/token"));
        headers.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "SpamFilter App"));
        headers.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "application/json"));//Http2HeaderName.ACCEPT
        headers.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, "application/json"));
        headers.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, String.valueOf(wrapper.getReadableSize())));

        FullRequest fullRequest = new FullRequest(headers, wrapper, null);

        Http2Socket socket = client.createHttpsSocket(engine, new CloseListener());
        return socket
                    .connect(hostWithPort)
                    .thenCompose((v) -> socket.send(fullRequest))
                    .thenApply( (http2Resp) -> translate(http2Resp, AuthResponse.class));
    }

    @Override
    public XFuture<UserProfile> fetchProfile(FetchProfileRequest request) {
        SSLEngine engine = sslFactory.createEngine(hostWithPort.getHostOrIpAddress(), hostWithPort.getPort());

        Http2Request headers = new Http2Request();
        headers.addHeader(new Http2Header(Http2HeaderName.SCHEME, "https"));
        headers.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
        headers.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, hostWithPort.getHostOrIpAddress()+":"+hostWithPort.getPort()));
        headers.addHeader(new Http2Header(Http2HeaderName.PATH, "/userinfo"));
        headers.addHeader(new Http2Header(Http2HeaderName.AUTHORIZATION, "Bearer "+request.getAccessToken()));
        headers.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "SpamFilter App"));
        headers.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "application/json"));//Http2HeaderName.ACCEPT
        headers.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, "application/json"));
        headers.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, "0"));

        FullRequest fullRequest = new FullRequest(headers, null, null);

        Http2Socket socket = client.createHttpsSocket(engine, new CloseListener());
        return socket
                .connect(hostWithPort)
                .thenCompose((v) -> socket.send(fullRequest))
                .thenApply( (http2Resp) -> translate(http2Resp, UserProfile.class));
    }

    private <T> T translate(FullResponse httpResp, Class<T> type) {
        DataWrapper payload = httpResp.getPayload();
        String contents = payload.createStringFromUtf8(0, payload.getReadableSize());

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_200_OK) {
            return unmarshalJson(contents, type);
        }

        throw new IllegalStateException("Auth failed.  response="+httpResp+" data="+contents);
    }

    private <T> T unmarshalJson(String contents, Class<T> type) {
        return jsonMapper.readValue(contents, type);
    }

    private static class CloseListener implements Http2SocketListener {

        @Override
        public void socketFarEndClosed(Http2Socket socket) {
            log.error("far end closed before we completed");
        }
    }

}
