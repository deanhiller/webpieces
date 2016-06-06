package org.webpieces.compiler;

import java.io.File;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemandFactory;

/**
 * NOTE: I am not sure we want it to cache byte code only on class loading rather than compiling but then again if we are
 * not using it do we need it in the cache.  However, if our program exits, we lose it from the cache as well over restarts
 * where the source code has not changed.  Then again, we most likely exist AFTER classloading happens anyways as we
 * load the controller and then execute it (but not all paths will be run).  Anyways, this is how we do it for now
 * 
 * @author dhiller
 *
 */
public class AnonymousByteCacheTest extends AbstractCompileTest {

//	File byteCodeControllerFile = new File(byteCodeCacheDir, "org.webpieces.compiler.bytecache.ByteCacheController");
//	File byteCodeEnumFile = new File(byteCodeCacheDir, "org.webpieces.compiler.bytecache.SomeRouteId");
	
	@Override
	protected String getPackageFilter() {
		return "org.webpieces.compiler.anonymous";
	}
	
	@After
	public void tearDown() {
//		byteCodeControllerFile.delete();
//		byteCodeEnumFile.delete();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testByteCodeExistsAtCorrectTime() throws Exception {
		
//		Assert.assertFalse(byteCodeControllerFile.exists());
//		Assert.assertFalse(byteCodeEnumFile.exists());
		
		log.info("loading class SomeRouterModule");
		String controller = getPackageFilter()+".SomeRouterModule";
		Class c = compiler.loadClass(controller);

//		Assert.assertTrue(byteCodeControllerFile.exists());
//		Assert.assertFalse(byteCodeEnumFile.exists());		
		
		log.info("loaded");
		Callable<Integer> callable = (Callable<Integer>) invokeMethod(c, "getRunnable");
		Integer value = callable.call();
		Assert.assertEquals(new Integer(55),  value);
		
		CompileConfig config = createCompileConfig();
		//now create a new compileOnDemand which will use the bytecode cache...
		compiler = CompileOnDemandFactory.createCompileOnDemand(config);
		
		compiler.loadClass(controller, true);
//		Assert.assertTrue(byteCodeControllerFile.exists());
//		Assert.assertTrue(byteCodeEnumFile.exists());
		
	}

}
