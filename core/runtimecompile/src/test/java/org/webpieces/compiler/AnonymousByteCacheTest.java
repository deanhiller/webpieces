package org.webpieces.compiler;

import java.io.File;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemandFactory;
import org.webpieces.compiler.impl.test.ForTestRouteId;
import org.webpieces.util.file.FileFactory;

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

	String packageStr = "org.webpieces.compiler.anonymous";
	File byteCodeControllerFile = FileFactory.newFile(byteCodeCacheDir, packageStr + ".AnonymousController");
	File byteCodeEnumFile = FileFactory.newFile(byteCodeCacheDir, packageStr + ".AnonymousRouteId");
	
	@Override
	protected String getPackageFilter() {
		return packageStr;
	}
	
	@Override
    @After
	public void tearDown() {
		byteCodeControllerFile.delete();
		byteCodeEnumFile.delete();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testByteCodeExistsAtCorrectTime() throws Exception {
		
		Assert.assertFalse(byteCodeControllerFile.exists());
		Assert.assertFalse(byteCodeEnumFile.exists());
		
		//DO NOT CALL Classname.getClass().getName() so that we don't pre-load it from the default classloader and
		//instead just tediously form the String ourselves...
		String controller = getPackageFilter()+".AnonymousController";
		LOG.info("loading class "+controller);
		Class c = compiler.loadClass(controller);

		Assert.assertTrue(byteCodeControllerFile.exists());
		//The enum is not compiled yet...it is on-demand compiled later...
		Assert.assertFalse(byteCodeEnumFile.exists());		
		
		LOG.info("loaded");
		Callable<ForTestRouteId> callable = (Callable<ForTestRouteId>) invokeMethod(c, "getRunnable");
		ForTestRouteId value = callable.call();
		
		LOG.info("test route id="+value);
		
		CompileConfig config = createCompileConfig();
		//now create a new compileOnDemand which will use the bytecode cache...
		compiler = CompileOnDemandFactory.createCompileOnDemand(config);
		
		compiler.loadClass(controller, true);
		Assert.assertTrue(byteCodeControllerFile.exists());
		Assert.assertTrue(byteCodeEnumFile.exists());
		
	}

}
