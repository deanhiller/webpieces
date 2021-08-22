package org.webpieces.microsvc.client.impl;

import com.orderlyhealth.api.Endpoint;
import com.orderlyhealth.api.monitoring.OrderlyMonitoring;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.util.futures.FutureHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class ExternalJsonClient extends AbstractHttpsClientHelper {

    @Inject
    public ExternalJsonClient(JacksonJsonConverter jsonMapper, Http2Client client, FutureHelper helper, ScheduledExecutorService schedulerSvc, OrderlyMonitoring monitoring) {
        super(jsonMapper, client, helper, schedulerSvc, monitoring);
    }

    @Override
    public String getKeyStoreLocation() {
        return "/prodKeyStore.jks";
    }

    @Override
    public String getKeyStorePassword() {
        return "lP9Ow1uYXZr9zgt6";
    }

    @Override
    public <T> CompletableFuture<T> sendHttpRequest(Method method, Object request, Endpoint endpoint, Class<T> responseType, List<Http2Header> extraHeaders) {
        return super.sendHttpRequest(method, request, endpoint, responseType, extraHeaders);
    }

}
