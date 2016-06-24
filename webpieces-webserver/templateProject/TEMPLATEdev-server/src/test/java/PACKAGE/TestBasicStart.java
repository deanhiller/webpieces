package PACKAGE;
import org.junit.Test;

import PACKAGE.CLASSNAMEDevServer;

public class TestBasicStart {

	//We normally don't need to test the development server, but we at least make sure developers are 
	//not breaking the startup of the development server here...
	@Test
	public void testBasicStartup() throws InterruptedException {
		//really just making sure we don't throw an exception...which catches quite a few mistakes
		CLASSNAMEDevServer server = new CLASSNAMEDevServer(true);
		//In this case, we bind a port
		server.start();

		//we should depend on http client and send a request in to ensure operation here...
		
		server.stop();
	}
}
