package WEBPIECESxPACKAGE;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class TestBasicDevStart {

	//We normally don't need to test the development server, but we at least make sure developers are 
	//not breaking the startup of the development server here...
	@Test
	public void testBasicDevStartup() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
		testArgSetup("test");
		
		//really just making sure we don't throw an exception...which catches quite a few mistakes
		DevelopmentServer server = new DevelopmentServer(true);
		//In this case, we bind a port
		server.start();
		//we should depend on http client and send a request in to ensure operation here...
		
		server.stop();
	}
	
	public static void testArgSetup(String param) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(TestBasicDevStart.class.getName());
		Method[] method = clazz.getDeclaredMethods();
		Method target = null;
		for(Method m : method) {
			if("testArgSetup".equals(m.getName()))
				target = m;
		}
		
		Assert.assertNotNull(target);
		Parameter[] parameters = target.getParameters();
		String name = parameters[0].getName();
		Assert.assertEquals("Compiler option is not on so we can't run this test", "param", name);
	}
}
