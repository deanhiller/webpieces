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
		//DO NOT CALL Classname.getClass().getName() so that we don't pre-load it from the default classloader and
		//instead just tediously form the String ourselves...
		String controller = getPackageFilter()+".SomeController";
		LOG.info("loading class SomeController");
		Class c = compiler.loadClass(controller);

		LOG.info("loaded");
		int retVal = invokeMethodReturnInt(c, "someMethod");
		
		Assert.assertEquals(6, retVal);
		
		cacheAndMoveFiles();
		
		Class c2 = compiler.loadClass(controller);
		
		int retVal2 = invokeMethodReturnInt(c2, "someMethod");
		
		Assert.assertEquals(9, retVal2);
	}


}
