package org.webpieces.compiler;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;


public class RemoveFileTest extends AbstractCompileTest {

	//modify ONE child class file
	//ADD a file AND use the file from controller (different from before)
	//REMOVE a file AND use the file from controller
	
	@Override
	protected String getPackageFilter() {
		return "org.webpieces.compiler.removefile";
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testAddingFileAndModifyingControllerToUseIt() {
		log.info("loading class RemoveFileController");
		//DO NOT CALL Classname.getClass().getName() so that we don't pre-load it from the default classloader and
		//instead just tediously form the String ourselves...
		String controller = getPackageFilter()+".RemoveFileController";
		Class c = compiler.loadClass(controller);

		log.info("loaded");
		int retVal = invokeMethodReturnInt(c, "someMethod");
		
		Assert.assertEquals(66, retVal);
		
		cacheAndMoveFiles();
		//need to also delete the file we no longer use..
		removeUnusedFile();
		
		Class c2 = compiler.loadClass(controller);
		
		int retVal2 = invokeMethodReturnInt(c2, "someMethod");
		
		Assert.assertEquals(77, retVal2);
	}

	private void removeUnusedFile() {
		String packageFilter = getPackageFilter();
		String path = packageFilter.replace('.', '/');

		File existingDir = new File(myCodePath, path);
		
		File javaFile = new File(existingDir, "ClassToBeRemoved.java");
		//javaFile must exist for test to be run...
		Assert.assertTrue(javaFile.exists());
		Assert.assertTrue(javaFile.delete());
	}


}
