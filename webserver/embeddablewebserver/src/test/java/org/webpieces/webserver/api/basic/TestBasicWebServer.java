package org.webpieces.webserver.api.basic;

import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.webserver.test.PlatformOverridesForTest;

public class TestBasicWebServer {

	@Test
	public void testBasic() {
		BasicWebserver webserver = new BasicWebserver(new PlatformOverridesForTest(), null);
		HttpRequestListener server = webserver.start();

		
	}

}
