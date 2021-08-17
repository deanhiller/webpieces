package org.webpieces.util.file;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class TestUserDirProperty {

	/**
	 * Test demonstrating the issue http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4117557
	 * BUT they should really throw an exception then when modifying user.dir property to tell us that.
	 */
	@Test
	public void testUserDirProp() {

		int deanVar = 0;
		System.out.println("deanVar="+deanVar);

		File f = new File("src/test/resources/logback-test.xml");

		//assert absolute path is correct
		//Assert.assertEquals("/Library/Workflow/webpieces/core/core-util/src/test/resources/logback-test.xml", f.getAbsolutePath());
		Assert.assertTrue(f.exists());

		//NOW, change user.dir
		System.setProperty("user.dir", "/Library/Workflow/webpieces/core/core-util/src");
				
		//Now, f2 is relative to NEW user.dir property
		File f2 = new File("test/resources/logback-test.xml");
		
		//verify absolute path is still the full correct path for f2 and it is
		//Assert.assertEquals("/Library/Workflow/webpieces/core/core-util/src/test/resources/logback-test.xml", f2.getAbsolutePath());
		
		//since absolute path was correct, it should exist 
		Assert.assertFalse(f2.exists());

		int deanVar = 0;
		System.out.println("deanVar="+deanVar);
	}
}
