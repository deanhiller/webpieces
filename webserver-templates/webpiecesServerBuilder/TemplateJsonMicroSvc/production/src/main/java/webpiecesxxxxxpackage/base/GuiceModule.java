package webpiecesxxxxxpackage.base;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.microsvc.client.api.HttpsConfig;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.SSLConfiguration;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.plugin.secure.sslcert.WebSSLFactory;
import org.webpieces.recorder.api.ApiRecorderCreator;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.ClientAssertions;
import webpiecesxxxxxpackage.db.DbCredentials;
import webpiecesxxxxxpackage.deleteme.remoteapi.RemoteApi;
import webpiecesxxxxxpackage.deleteme.remoteapi.RemoteServiceSimulator;
import webpiecesxxxxxpackage.deleteme.service.SimpleStorageImpl;

import javax.inject.Singleton;
import java.util.function.Supplier;

public class GuiceModule implements Module {

	private static final Logger log = LoggerFactory.getLogger(GuiceModule.class);
	//private final Supplier<Integer> secureToken;

	private final Supplier<String> jdbcUrl;
	private final Supplier<String> user;
	private final Supplier<String> password;

	public GuiceModule(Arguments args) {
		//secureToken = args.createRequiredEnvVar("DEANS_SECURITY_TOKEN", 12345, "Some help", (s) -> Integer.parseInt(s));

		//MODIFY these to createRequiredEnvVar so if not supplied, server will not startup.
		//set to optional so server starts with in-memory database out of the box
		jdbcUrl = args.createOptionalEnvVar("DB_URL", "jdbc:log4jdbc:h2:mem:test", "JDBC url including host, port, database", (s) -> s);
		user = args.createOptionalEnvVar("DB_USER", "sa", "user to login to database", (s) -> s);
		password = args.createOptionalEnvVar("DB_PASSWORD", "", "password to login to database", (s) -> s);

	}

	@Provides
	@Singleton
	public DbCredentials dbCredentials() {
		log.info("running in new world");
		return new DbCredentials(jdbcUrl.get(), user.get(), password.get());
	}

	//This is where you would put the guice bindings you need though generally if done
	//right, you won't have much in this file.
	
	//If you need more Guice Modules as you want to scale, just modify ServerMeta which returns
	//the list of all the Guice Modules in your application
	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Binder binder) {
		
		log.info("running module");
		
		//all modules have access to adding their own Startable objects to be run on server startup
		Multibinder<Startable> uriBinder = Multibinder.newSetBinder(binder, Startable.class);
	    uriBinder.addBinding().to(PopulateDatabase.class);

		Multibinder<AddPlatformHeaders> headers = Multibinder.newSetBinder(binder, AddPlatformHeaders.class);
		headers.addBinding().to(AddCompanyHeaders.class);

	    //Must bind a SimpleStorage for plugins to read/save data and render their html pages
	    binder.bind(SimpleStorage.class).to(SimpleStorageImpl.class).asEagerSingleton();

	    //since GlobalAppContext is a singleton, ApplicationContext will be to and will be the same
		binder.bind(ApplicationContext.class).to(GlobalAppContext.class).asEagerSingleton();

		binder.bind(HttpsConfig.class).toInstance(new HttpsConfig(true));
		binder.bind(ClientServiceConfig.class).toInstance(new ClientServiceConfig(new HeadersCtx(), "fakeSvc"));

		binder.bind(SSLEngineFactory.class).to(WebSSLFactory.class);

		//2 bugs,
		// 1. Guice not respecting null injection
		// 2. webpieces requiring a null to be injected or it fails that we did not bind a 'something' and need to bind null :(
		binder.bind(SSLEngineFactory.class).annotatedWith(Names.named(SSLConfiguration.BACKEND_SSL)).to(WebSSLFactory.class);

		binder.bind(ClientAssertions.class).to(ClientAssertionsImpl.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	public RemoteApi createRemoteSvc(ApiRecorderCreator recorderCreator, RESTClientCreator factory) {
		return recorderCreator.createClient(RemoteApi.class, new RemoteServiceSimulator());

		//normally you would do something like this....
//		InetSocketAddress addr = new InetSocketAddress(9091);
//		return factory.createClient(RemoteService.class, addr);
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
