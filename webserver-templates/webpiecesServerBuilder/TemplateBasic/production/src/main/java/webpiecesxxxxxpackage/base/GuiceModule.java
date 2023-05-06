package webpiecesxxxxxpackage.base;

import com.google.inject.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.microsvc.client.api.HttpsConfig;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.plugin.backend.login.BackendLogin;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.context.ClientAssertions;
import webpiecesxxxxxpackage.Server;
import webpiecesxxxxxpackage.db.DbCredentials;
import webpiecesxxxxxpackage.db.EducationEnum;
import webpiecesxxxxxpackage.db.RoleEnum;
import webpiecesxxxxxpackage.service.RemoteService;
import webpiecesxxxxxpackage.service.RemoteServiceSimulator;
import webpiecesxxxxxpackage.service.SimpleStorageImpl;
import webpiecesxxxxxpackage.service.SomeLibrary;
import webpiecesxxxxxpackage.service.SomeLibraryImpl;
import webpiecesxxxxxpackage.web.login.BackendLoginImpl;
import webpiecesxxxxxpackage.web.tags.MyHtmlTagCreator;

import javax.inject.Singleton;
import java.util.function.Supplier;

public class GuiceModule implements Module {

	private static final Logger log = LoggerFactory.getLogger(GuiceModule.class);
	private final Supplier<String> optionalArg;
	private final Supplier<String> jdbcUrl;
	private final Supplier<String> user;
	private final Supplier<String> password;

	public GuiceModule(Arguments args) {
		optionalArg = args.createOptionalArg("optionalArg", "default", "Your help message", (s) -> s);

		//MODIFY these to createRequiredEnvVar so if not supplied, server will not startup.
		//set to optional so server starts with in-memory database out of the box
		jdbcUrl = args.createOptionalEnvVar("DB_URL", "jdbc:log4jdbc:h2:mem:test", "JDBC url including host, port, database", (s) -> s);
		user = args.createOptionalEnvVar("DB_USER", "sa", "user to login to database", (s) -> s);
		password = args.createOptionalEnvVar("DB_PASSWORD", "", "password to login to database", (s) -> s);
		/**
		 * Args are LAZY checked so in the cloud, we can fail ONCE and tell you ALL the errors regarding missing
		 * arguments at once, rather than this ->
		 * 1. boot up
		 * 2. fail on arg1 and print arg1 is required
		 * 3. boot up
		 * 4. fail on arg2 and pring arg2 is required or format is wrong
		 * 5. boot up..fail again
		 *
		 * Instead, we do this
		 * 1. boot up
		 * 2. fail on arg1, arg2, arg3, all failed and the reasons they all failed, missing, conversion error, etc.
		 */
		//reqArg = cmdLineArguments.createRequiredArg("reqArg", "Your help", (s) -> s);
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

		Multibinder<ObjectStringConverter> conversionBinder = Multibinder.newSetBinder(binder, ObjectStringConverter.class);
		conversionBinder.addBinding().to(EducationEnum.WebConverter.class);
		conversionBinder.addBinding().to(RoleEnum.WebConverter.class);

		Multibinder<HtmlTagCreator> htmlTagCreators = Multibinder.newSetBinder(binder, HtmlTagCreator.class);
		htmlTagCreators.addBinding().to(MyHtmlTagCreator.class);
		
	    binder.bind(SomeLibrary.class).to(SomeLibraryImpl.class);

	    //Must bind a SimpleStorage for plugins to read/save data and render their html pages
	    binder.bind(SimpleStorage.class).to(SimpleStorageImpl.class).asEagerSingleton();
	    
	    //Must bind a BackendLogin for the backend plugin(or remove the backend plugin)
	    binder.bind(BackendLogin.class).to(BackendLoginImpl.class).asEagerSingleton();

	    //since GlobalAppContext is a singleton, ApplicationContext will be to and will be the same
		binder.bind(ApplicationContext.class).to(GlobalAppContext.class).asEagerSingleton();

		binder.bind(HttpsConfig.class).toInstance(new HttpsConfig(true));

		binder.bind(ClientServiceConfig.class).toInstance(HeadersCtx.createConfig(Server.APP_NAME));

		binder.bind(ClientAssertions.class).to(ClientAssertionsImpl.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	public DbCredentials dbCredentials() {
		log.info("running in new world");
		return new DbCredentials(jdbcUrl.get(), user.get(), password.get());
	}

	@Provides
	@Singleton
	public RemoteService createRemoteSvc(RESTClientCreator factory) {
		return new RemoteServiceSimulator();

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
