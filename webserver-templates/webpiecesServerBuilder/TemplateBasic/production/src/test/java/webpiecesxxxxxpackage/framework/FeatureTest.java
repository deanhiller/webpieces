package webpiecesxxxxxpackage.framework;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.util.HostWithPort;
import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.http2.CompanyApiTest;
import webpiecesxxxxxpackage.Server;
import webpiecesxxxxxpackage.base.AddCompanyHeaders;
import webpiecesxxxxxpackage.base.CompanyHeaders;
import webpiecesxxxxxpackage.base.HeadersCtx;
import webpiecesxxxxxpackage.json.ExampleRestAPI;
import webpiecesxxxxxpackage.json.SearchApi;
import webpiecesxxxxxpackage.mock.JavaCache;
import webpiecesxxxxxpackage.mock.MockRemoteService;
import webpiecesxxxxxpackage.service.RemoteService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 *
 * @author dhiller
 *
 */
public class FeatureTest extends CompanyApiTest {

    private final static Logger log = LoggerFactory.getLogger(FeatureTest.class);
    private String[] args = {
            "-http.port=:0",
            "-https.port=:0",
            "-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory",
            "-hibernate.loadclassmeta=true"
    };

    @Override
    public Map<String, String> initEnvironmentVars() {
        return Map.of(
                //use a different in-memory db each test class so we can be multi-threaded
                "DB_URL","jdbc:log4jdbc:h2:mem:"+getClass().getSimpleName(),
                "DB_USER", "sa",
                "DB_PASSWORD", ""
        );
    }

    protected SearchApi saveApi;
    protected ExampleRestAPI exampleRestAPI;
    protected MockRemoteService mockRemoteService = new MockRemoteService();

    @BeforeEach
    public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
        log.info("Setting up test");
        super.initialize();
        saveApi = super.createRestClient(SearchApi.class);
        exampleRestAPI = super.createRestClient(ExampleRestAPI.class);
    }

    @AfterEach
    public void tearDown() {
        //do not leak context between tests
        Context.clear();
    }

    @Override
    protected void startServer() {
        metrics = new SimpleMeterRegistry();
        Server webserver = new Server(getOverrides(),new AppOverridesModule(),
                new ServerConfig(JavaCache.getCacheLocation()), args
        );
        webserver.start();

        serverHttpsAddr = new HostWithPort("localhost", webserver.getUnderlyingHttpsChannel().getLocalAddress().getPort());
        serverHttpAddr = new HostWithPort("localhost", webserver.getUnderlyingHttpChannel().getLocalAddress().getPort());
    }

    @Override
    protected ClientServiceConfig getConfig() {
        return HeadersCtx.createConfig(Server.APP_NAME);
    }

    @Override
    protected List<AddPlatformHeaders> fetchEnums() {
        List<AddPlatformHeaders> classes = super.fetchEnums();
        classes.add(new AddCompanyHeaders());
        return classes;
    }

    private class AppOverridesModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(RemoteService.class).toInstance(mockRemoteService);
        }
    }

}
