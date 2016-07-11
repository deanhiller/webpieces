package PACKAGE;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.Assert;
import org.junit.Test;

public class TestBasicStart {

	//This exercises full startup with no mocking in place whatsoever BUT as you add remote systems to 
	//talk to, you will need to change this test and pass in appOverridesModule to override those 
	//pieces.  In this test, we literally bind a port.  We only do this in one or two tests just to
	//ensure full server basic functionality is working.  All other tests create a server and pass
	//in http requests directly.  This test can use http client to send requests in which exercises
	//our http parser and other pieces (which sometimes can catch bugs when you upgrade webpieces
	// so in some cases, this can be valuable)
	@Test
	public void testBasicStartup() throws InterruptedException, IOException, ClassNotFoundException {
		testWasCompiledWithParamNames("test");
		
		//really just making sure we don't throw an exception...which catches quite a few mistakes
		CLASSNAMEServer server = new CLASSNAMEServer(null, null, true, null);
		//In this case, we bind a port
		server.start();

		System.out.println("bound port="+server.getUnderlyingHttpChannel().getLocalAddress());
		//we should depend on http client and send a request in to ensure operation here...
		
		server.stop();
		
		//ALSO, it is completely reasonable to create a brand new instance(ie. avoid statics and avoid
		// non-guice singletons).  A guice singleton is only a singleton within the scope of a server
		//while a java singleton....well, pretty much sucks.  Google "Singletons are evil".
		
		CLASSNAMEServer server2 = new CLASSNAMEServer(null, null, true, null);
		//In this case, we bind a port
		server2.start();
		System.out.println("bound port="+server.getUnderlyingHttpChannel().getLocalAddress());
		
		//we should depend on http client and send a request in to ensure operation here...
		
		server2.stop();
	}

	public static void testWasCompiledWithParamNames(String param) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(TestBasicStart.class.getName());
		Method[] method = clazz.getDeclaredMethods();
		Method target = null;
		for(Method m : method) {
			if("testWasCompiledWithParamNames".equals(m.getName()))
				target = m;
		}
		
		Assert.assertNotNull(target);
		Parameter[] parameters = target.getParameters();
		String name = parameters[0].getName();
		Assert.assertEquals("Compiler option is not on so we can't run this test", "param", name);
	}
}
