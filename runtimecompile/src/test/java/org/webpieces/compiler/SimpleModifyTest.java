package org.webpieces.compiler;

import org.junit.Assert;
import org.junit.Test;


public class SimpleModifyTest extends AbstractCompileTest {

	@Override
	protected String getPackageFilter() {
		return "org.webpieces.compiler.simple";
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testSimpleChangeMethodNameAndRetVal() {
		log.info("loading class SomeController");
		Class c = compiler.loadClass("org.webpieces.compiler.simple.SomeController");

		log.info("loaded");
		int retVal = invokeMethod(c, "someMethod");
		
		Assert.assertEquals(6, retVal);
		
		cacheAndMoveFiles();
		
		Class c2 = compiler.loadClass("org.webpieces.compiler.simple.SomeController");
		
		int retVal2 = invokeMethod(c2, "someMethod");
		
		Assert.assertEquals(9, retVal2);
	}


}
