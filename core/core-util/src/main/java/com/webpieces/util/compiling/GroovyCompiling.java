package com.webpieces.util.compiling;

public class GroovyCompiling {

	private static ThreadLocal<Boolean> isCompilingGroovy = new ThreadLocal<Boolean>();

	public GroovyCompiling() {
		isCompilingGroovy.set(false);
	}
	
	public static boolean isCompilingGroovy() {
		return isCompilingGroovy.get();
	}
	
	public static void setCompilingGroovy(boolean compilingGroovy) {
		isCompilingGroovy.set(compilingGroovy);
	}
}
