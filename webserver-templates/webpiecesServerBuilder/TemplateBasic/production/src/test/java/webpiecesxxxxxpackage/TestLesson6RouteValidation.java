package webpiecesxxxxxpackage;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.Asserts;

import webpiecesxxxxxpackage.mock.JavaCache;

public class TestLesson6RouteValidation {

	private String[] args = { "-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory", "-hibernate.loadclassmeta=true"};

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
		Server server = new Server(null, null, serverConfig, args);
		
		//Start server to force validation
		server.start();
	}
}
