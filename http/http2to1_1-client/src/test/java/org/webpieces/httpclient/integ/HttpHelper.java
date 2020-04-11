package org.webpieces.httpclient.integ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.plugins.json.JsonError;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;

public class HttpHelper {
    private Http2Client client;
    private InetSocketAddress authHost = new InetSocketAddress("monitoring.googleapis.com", 443);
    private String key; //YOUR_API_KEY
    private String accessToken; //YOUR_ACCESS_TOKEN
    private String keyStoreLocation = "/prodKeyStore.jks";
    private String keyStorePassword = "lP9Ow1uYXZr9zgt6";
    private static final DataWrapperGenerator wrapperGenerator = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private ObjectMapper jsonMapper = new ObjectMapper();

    public HttpHelper(String key, String accessToken) {
        this.key = key;
        this.accessToken = accessToken;
        BackpressureConfig config = new BackpressureConfig();
        //config.setMaxBytes(null);
        client = Http2to11ClientFactory.createHttpClient("httpclient", 10, config, new SimpleMeterRegistry());
    }

    public CompletableFuture<FullResponse> sendHttpRequest(String method, String path) {
        SSLEngine engine = createEngine(authHost.getHostName(), authHost.getPort());
        Http2Socket httpSocket = client.createHttpsSocket(engine);
        CompletableFuture<Void> connect = httpSocket.connect(authHost);

        return connect.thenCompose(voidd -> {
            //System.out.println("Connected to address:" + authHost.getHostName());
            Http2Request httpReq = new Http2Request();
            DataWrapper emptyBody = DataWrapperGeneratorFactory.EMPTY;
            FullRequest fullRequest = new FullRequest(httpReq, emptyBody, null);
            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, "0"));
            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.METHOD, method));
            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.PATH, path + key));

            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.AUTHORITY, authHost.getHostString()));
            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "Orderly Metric Deleter"));
            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.AUTHORIZATION, "Bearer " + accessToken));//Http2HeaderName.AUTHORIZATION
            fullRequest.getHeaders().addHeader(new Http2Header(Http2HeaderName.ACCEPT, "application/json"));//Http2HeaderName.ACCEPT
            System.out.println("Request: " + fullRequest.toString());
            return httpSocket.send(fullRequest);

        });
    }

    private SSLEngine createEngine(String host, int port) {
        try {

            InputStream in = this.getClass().getResourceAsStream(keyStoreLocation);

            //char[] passphrase = password.toCharArray();
            // First initialize the key and trust material.
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(in, keyStorePassword.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");

            //****************Client side specific*********************
            // TrustManager's decide whether to allow connections.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            sslContext.init(null, tmf.getTrustManagers(), null);
            //****************Client side specific*********************

            SSLEngine engine = sslContext.createSSLEngine(host, port);
            engine.setUseClientMode(true);
            return engine;
        } catch (Exception e) {
            throw new RuntimeException("Could not create SSLEngine", e);
        }
    }

    public byte[] marshal(Object authRequest) {
        byte[] reqAsBytes;
        try {
            reqAsBytes = jsonMapper.writeValueAsBytes(authRequest);

        } catch (Exception e) {
            throw new RuntimeException("Bug in marshalling to json", e);
        }
        return reqAsBytes;
    }

    public <T> T unmarshal(FullResponse httpResp, String contents, Class<T> type) {
        try {
            if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_200_OK) {
                return this.jsonMapper.readValue(contents, type);
            } else {
                JsonError error = (JsonError)this.jsonMapper.readValue(contents, JsonError.class);
                throw new RuntimeException("Error received from auth service:" + error.getError());
            }
        } catch (Exception var5) {
            throw new RuntimeException("Bug in unmarshalling from json.  json=" + contents + " fullResp=" + httpResp, var5);
        }
    }

}
