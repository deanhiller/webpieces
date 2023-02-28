package org.webpieces.googlecloud.storage.biz;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.webpieces.googlecloud.storage.api.DeansCoolApi;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.microsvc.client.api.HttpsConfig;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.plugin.json.ConverterConfig;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RestGuiceModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(2));

        binder.bind(ClientAssertions.class).toInstance(new ClientAssertionsForTest());
        ConverterConfig converterConfig = new ConverterConfig(true);
        binder.bind(ConverterConfig.class).toInstance(converterConfig);
        binder.bind(HttpsConfig.class).toInstance(new HttpsConfig(false));
        binder.bind(MeterRegistry.class).toInstance(new SimpleMeterRegistry());
    }

    @Singleton
    @Provides
    public DeansCoolApi createApi(RESTClientCreator creator) {
        return creator.createClient(DeansCoolApi.class, new InetSocketAddress("www.google.com", 443));
    }

    @Provides
    @Singleton
    public Http2Client createClient(MeterRegistry metrics) {

        BackpressureConfig config = new BackpressureConfig();

        //clients should NOT have backpressure or it could screw the server over when the server does not support backpresssure
        config.setMaxBytes(null);

        //This is an http1_1 client masquerading as an http2 client so we can switch to http2 faster when ready!!
        Http2Client httpClient = Http2to11ClientFactory.createHttpClient("httpclient", 10, config, metrics);

        return httpClient;

    }

}
