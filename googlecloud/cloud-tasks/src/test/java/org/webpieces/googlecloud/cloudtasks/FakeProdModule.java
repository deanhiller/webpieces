package org.webpieces.googlecloud.cloudtasks;

import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.CloudTasksSettings;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.googlecloud.cloudtasks.api.GCPCloudTaskConfig;
import org.webpieces.googlecloud.cloudtasks.api.QueueClientCreator;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.microsvc.client.api.HttpsConfig;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.plugin.json.ConverterConfig;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.InetSocketAddress;

public class FakeProdModule implements Module{
    @Override
    public void configure(Binder binder) {
        binder.bind(ClientAssertions.class).toInstance(new ClientAssertionsForTest());

        binder.bind(MeterRegistry.class).toInstance(new SimpleMeterRegistry());
        ConverterConfig converterConfig = new ConverterConfig(true);
        binder.bind(ConverterConfig.class).toInstance(converterConfig);
        binder.bind(HttpsConfig.class).toInstance(new HttpsConfig(true));

        binder.bind(GCPCloudTaskConfig.class).toInstance(new GCPCloudTaskConfig("projectId", "us-west1"));
    }

    @Provides
    @Singleton
    private CloudTasksClient createCloudTasksClient() {
        try {
            return CloudTasksClient.create(CloudTasksSettings.newBuilder().build());
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }
    }

    @Provides
    @Singleton
    public DeansApi provideDeansApi(QueueClientCreator creator) {
        return creator.createClient(DeansApi.class, new InetSocketAddress("reqres.in",443));
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
