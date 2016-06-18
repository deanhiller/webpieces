package org.webpieces.compiler;

import org.junit.Assert;
import org.junit.Test;


public class ChildModifyTest extends AbstractCompileTest {

	@Override
	protected String getPackageFilter() {
		return "org.webpieces.compiler.child";
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testSimpleChangeMethodNameAndRetVal() {
		//DO NOT CALL Classname.getClass().getName() so that we don't pre-load it from the default classloader and
		//instead just tediously form the String ourselves...
		String controller = getPackageFilter()+".GrandfatherController";
		log.info("loading class "+controller);
		
		Class c = compiler.loadClass(controller);

		log.info("loaded");
		int retVal = invokeMethodReturnInt(c, "someMethod");
		
		Assert.assertEquals(88, retVal);
		
		cacheAndMoveFiles();
		
		Class c2 = compiler.loadClass(controller);
		
		int retVal2 = invokeMethodReturnInt(c2, "someMethod");
		
		Assert.assertEquals(99, retVal2);
	}


}
