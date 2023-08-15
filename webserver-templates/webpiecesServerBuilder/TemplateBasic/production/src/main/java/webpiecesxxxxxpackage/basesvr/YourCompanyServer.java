package webpiecesxxxxxpackage.basesvr;

import java.util.Arrays;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.MaxRequestConfig;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.util.futures.Logging;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.api.WebpiecesServer;

import com.google.inject.Module;

import webpiecesxxxxxpackage.Server;

/**
 * We are designed for doing tons of microservices.  you would put this in a re-usable location and rename it
 * and use it as the base for all servers!!!  In this way, you can modify all company servers in one location
 * 
 * @author dean
 *
 */
public abstract class YourCompanyServer {

	static {
		Logging.setupMDCForLogging();
	}

	private static final Logger log = LoggerFactory.getLogger(Server.class);

	private final WebpiecesServer webServer;

	public static void main(Function<ServerConfig, YourCompanyServer> yourServer) {
		try {
			String jdkVersion = System.getProperty("java.version");
			String user = System.getProperty("user.name");
			log.info("Starting Production Server user="+user+" under java version="+jdkVersion);

			MaxRequestConfig maxRequestConfig = new MaxRequestConfig();

			ServerConfig config = new ServerConfig(true);
			BackpressureConfig backpressureConfig = config.getBackpressureConfig();
			backpressureConfig.setLegacy(false);
			backpressureConfig.setMaxRequestConfig(maxRequestConfig);
			YourCompanyServer server2 = yourServer.apply(config);

			server2.start();

			synchronized (YourCompanyServer.class) {
				//wait forever so server doesn't shut down..
				YourCompanyServer.class.wait();
			}		
		} catch(Throwable e) {
			log.error("Failed to startup.  exiting jvm", e);
			System.exit(1); // should not be needed BUT some 3rd party libraries start non-daemon threads :(
		}		
	}

	public YourCompanyServer(
			String appName,
			Module platformOverrides,
			Module appOverrides, 
			ServerConfig svrConfig, 
			String ... args
	) {
		String base64Key = "__SECRETKEYHERE__";  //This gets replaced with a unique key each generated project which you need to keep or replace with your own!!!		
		
		log.info("Constructing WebpiecesServer with args="+Arrays.asList(args));

		//This is a special platform module, the only CORE module we pass in that can be overriden in platformOverrides as well.
		//If you have 100 microservers, a few of them may override this in platformOverrides for special cases or testing
		//You could pass in an instance id, but this works for now too...
		String instanceId = RandomInstanceId.generate();
		MetricsModule metricsModule = new MetricsModule(instanceId);
		
		//TODO: app name should not affect modification of directory so tests don't need to pass in app name..
		webServer = new WebpiecesServer(appName, base64Key, metricsModule, platformOverrides, appOverrides, svrConfig, args);
	}
	
	public void start() {
		webServer.start();		
	}

	public void stop() {
		webServer.stop();
	}
	
	public TCPServerChannel getUnderlyingHttpChannel() {
		return webServer.getUnderlyingHttpChannel();
	}
	public TCPServerChannel getUnderlyingHttpsChannel() {
		return webServer.getUnderlyingHttpsChannel();
	}
}
