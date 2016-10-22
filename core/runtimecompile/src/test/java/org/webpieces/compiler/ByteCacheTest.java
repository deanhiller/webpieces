package org.webpieces.compiler;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * NOTE: I am not sure we want it to cache byte code only on class loading rather than compiling but then again if we are
 * not using it do we need it in the cache.  However, if our program exits, we lose it from the cache as well over restarts
 * where the source code has not changed.  Then again, we most likely exist AFTER classloading happens anyways as we
 * load the controller and then execute it (but not all paths will be run).  Anyways, this is how we do it for now
 * 
 * @author dhiller
 *
 */
public class ByteCacheTest extends AbstractCompileTest {

	String packageStr = "org.webpieces.compiler.bytecache";
	File byteCodeControllerFile = new File(byteCodeCacheDir, packageStr + ".ByteCacheController");
	File byteCodeEnumFile = new File(byteCodeCacheDir, packageStr + ".ByteCacheRouteId");
	
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
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testByteCodeExistsAtCorrectTime() {
		
		Assert.assertFalse(byteCodeControllerFile.exists());
		Assert.assertFalse(byteCodeEnumFile.exists());
		
		log.info("loading class AddFileController");
		//DO NOT CALL Classname.getClass().getName() so that we don't pre-load it from the default classloader and
		//instead just tediously form the String ourselves...
		String controller = getPackageFilter()+".ByteCacheController";
		Class c = compiler.loadClass(controller);

		Assert.assertTrue(byteCodeControllerFile.exists());
		Assert.assertFalse(byteCodeEnumFile.exists());		
		
		log.info("loaded");
		invokeMethod(c, "createUserForm");
		
		Assert.assertTrue(byteCodeControllerFile.exists());
		Assert.assertTrue(byteCodeEnumFile.exists());
	}

}
