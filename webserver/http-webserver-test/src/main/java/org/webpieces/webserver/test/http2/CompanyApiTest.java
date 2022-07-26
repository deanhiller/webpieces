package org.webpieces.webserver.test.http2;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.microsvc.client.api.HttpsConfig;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.plugin.json.ConverterConfig;
import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.context.Context;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.webserver.test.Asserts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class CompanyApiTest extends AbstractHttp2Test {

    private final static Logger log = LoggerFactory.getLogger(CompanyApiTest.class);

    protected InetSocketAddress serverHttpsAddr;
    protected InetSocketAddress serverHttpAddr;

    private RESTClientCreator restClientCreator;
    private boolean initialized;

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public void initialize() {
        log.info("Setting up test");
        //Necessary to avoid errors that could be confusing to developers...(fail early instead with what is wrong)
        Asserts.assertWasCompiledWithParamNames("test");

        try {
            startServer();
        }
        catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

        if(serverHttpAddr == null || serverHttpsAddr == null)
            throw new IllegalStateException("startServer method forgot to set the serverHttpAddr and serverHttpsAddr");

        simulateContext();

        Http2Client http2Client = getClient();

        Injector injector = Guice.createInjector(new ClientTestModule(http2Client));
        restClientCreator = injector.getInstance(RESTClientCreator.class);

        initialized = true;
    }

    private static class ClientTestModule implements Module {
        private Http2Client http2Client;

        public ClientTestModule(Http2Client http2Client) {
            this.http2Client = http2Client;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(Http2Client.class).toInstance(http2Client);
            binder.bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(1));
            binder.bind(HttpsConfig.class).toInstance(new HttpsConfig(true));
            binder.bind(ConverterConfig.class).toInstance(new ConverterConfig(true));

            binder.bind(ClientAssertions.class).toInstance(new ClientAssertions() {
                @Override
                public void throwIfCannotGoRemote() {

                }
            });

        }
    }

    public <T> T createRestClient(Class<T> apiOfService) {
        if(!initialized)
            throw new IllegalStateException("call initialize method first");

        log.info("Setting up client for " + apiOfService.getSimpleName());

        return restClientCreator.createClient(apiOfService, serverHttpsAddr);
    }

    public <T> T createPubSubClient(Class<T> apiOfService) {
        log.info("Setting up PubSub client for " + apiOfService.getSimpleName());
        throw new UnsupportedOperationException("not ported yet");
    }

    public <T> T createCloudTasksClient(Class<T> apiOfService) {
        log.info("Setting up CloudTasks client for " + apiOfService.getSimpleName());
        throw new UnsupportedOperationException("not ported yet");

    }

    protected abstract void startServer() throws IOException;

    protected void simulateContext() {
        Context.put(Context.HEADERS, new HashMap<String, String>());
    }

    @Override
    protected TestMode getTestMode() {
        //This mode slows things down but you can step through client and server http2 parser/hpack/engine etc.
        //If you are a beginner run in EMBEDDED_DIRET_NO_PARSING mode as it's faster AND you can step through
        //the main stuff you care about(not the http2 protocol stuff).
        return TestMode.EMBEDDED_DIRET_NO_PARSING;
    }
}

