package org.webpieces.util.compiling;

public class GroovyCompiling {

	private static ThreadLocal<Boolean> isCompilingGroovy = new ThreadLocal<Boolean>();

	public GroovyCompiling() {
		isCompilingGroovy.set(false);
	}
	
	public static boolean isCompilingGroovy() {
		Boolean boolean1 = isCompilingGroovy.get();
		if(boolean1 == null)
			return false;
		return boolean1;
	}
	
	public static void setCompilingGroovy(boolean compilingGroovy) {
		isCompilingGroovy.set(compilingGroovy);
	}
}
