package org.webpieces.microsvc.client.impl;

import com.orderlyhealth.api.monitoring.OrderlyMonitoring;
import com.orderlyhealth.api.util.HttpsConfig;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.util.futures.FutureHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class HttpsClientHelper extends AbstractHttpsClientHelper {

    protected String keyStoreLocation;
    protected String keyStorePassword;

    @Inject
    public HttpsClientHelper(JacksonJsonConverter jsonMapper, Http2Client client, HttpsConfig config, FutureHelper helper, ScheduledExecutorService scheduledSvc, OrderlyMonitoring monitoring) {

        super(jsonMapper, client, helper, scheduledSvc, monitoring);

        keyStoreLocation = config.getKeyStoreLocation();
        keyStorePassword = config.getKeyStorePassword();

    }

    @Override
    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    @Override
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

}
