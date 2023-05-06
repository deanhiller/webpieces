package webpiecesxxxxxpackage;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.Asserts;

import org.webpieces.webserver.test.EnvSimModule;
import webpiecesxxxxxpackage.mock.JavaCache;

public class TestRouteValidation {

	private String[] args = {
			"-http.port=:0",
			"-https.port=:0",
			"-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory",
			"-hibernate.loadclassmeta=true"
	};

	private Map<String, String> simulatedEnv = Map.of(
			"REQ_ENV_VAR", "somevalue"
	);

	//This test you should always keep to run during the gradle build.  It can only be run after the
	//gradle plugin html template compiler is run as it uses a file that is generated to validate all the
	//routeIds in the html files.  
	@Test
	public void testBasicProdStartup() throws InterruptedException, IOException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		String property = System.getProperty("gradle.running");
		if(property == null || !"true".equals(property))
			return; //don't run test except in gradle build
		
		ServerConfig serverConfig = new ServerConfig(JavaCache.getCacheLocation());
		serverConfig.setValidateRouteIdsOnStartup(true);
		//really just making sure we don't throw an exception...which catches quite a few mistakes
		Server server = new Server(new EnvSimModule(simulatedEnv), null, serverConfig, args);
		
		//Start server to force validation
		server.start();
	}
}
