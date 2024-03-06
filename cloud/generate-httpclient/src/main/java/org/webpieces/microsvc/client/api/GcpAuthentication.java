package org.webpieces.microsvc.client.api;

import org.webpieces.microsvc.client.impl.Endpoint;
import org.webpieces.microsvc.client.impl.HttpsJsonClient;
import org.webpieces.util.HostWithPort;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class GcpAuthentication implements Authentication {

    private final HttpsJsonClient jsonClient;
    private final Method method;
    private long expiresAtSeconds = 0;
    private String accessToken;

    @Inject
    public GcpAuthentication(
            HttpsJsonClient jsonClient
    ) {
        this.jsonClient = jsonClient;

        try {
            method = GcpAuthentication.class.getDeclaredMethod("fetchToken");
        } catch (NoSuchMethodException e) {
            throw SneakyThrow.sneak(e);
        }
    }

    @Override
    public Object setupMagic() {
        String previousSetting = Context.getMagic(GcpAuthHeader.AUTH_TOKEN);

        String accessToken = fetchToken();
        Context.putMagic(GcpAuthHeader.AUTH_TOKEN, "Bearer "+accessToken);

        return previousSetting;
    }

    private synchronized String fetchToken() {
        long secondsEpochNow = System.currentTimeMillis() / 1000;
        if(secondsEpochNow < expiresAtSeconds)
            return accessToken;

        try {
            Context.putMagic(GcpAuthHeader.METADATA_FLAVOR, "Google");
            HostWithPort host = new HostWithPort("metadata.google.internal", 80);
            Endpoint endpoint = new Endpoint(host, "GET", "/computeMetadata/v1/instance/service-accounts/default/token");

            XFuture<TokenResponse> mapXFuture = jsonClient.sendHttpRequest(method, null, endpoint, TokenResponse.class, true);
            TokenResponse map = null;
            try {
                map = mapXFuture.get(20, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw SneakyThrow.sneak(e);
            }
            accessToken = map.getAccessToken();
            expiresAtSeconds = secondsEpochNow + map.getExpiresIn() - 30; //subtract 30 seconds so we renew before it expires

            return accessToken;
        } finally {
            Context.removeMagic(GcpAuthHeader.METADATA_FLAVOR);
        }
    }

    @Override
    public void resetMagic(Object state) {
        //restore to previous state or if state was null, remove magic..
        if(state == null) {
            Context.removeMagic(GcpAuthHeader.AUTH_TOKEN);
            return;
        }

        String previousSetting = (String) state;
        Context.putMagic(GcpAuthHeader.AUTH_TOKEN, previousSetting);
    }

}
