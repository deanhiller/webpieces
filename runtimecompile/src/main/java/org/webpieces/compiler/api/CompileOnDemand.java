package org.webpieces.compiler.api;

public interface CompileOnDemand {

	/**
	 * Loads the class recompiling if necessary into a new classloader.  You 'can' also call
	 * 
	 * Thread.currentThread().setContextClassLoader(clazz.getClassLoader()); where clazz is the
	 * Class returned from this method.  You ONLY need to do this IF you are using the 
	 * ContextClassLoader and most likely you do not need to...unless you are using classloaders
	 * in your code specifically (3rd party jars do not count and are not loaded from our 
	 * classloader) 
	 * 
	 * @param clazzName The fully qualified java class name such as org.webpieces.MyController
	 * @return
	 */
	Class<?> loadClass(String clazzName);

}
