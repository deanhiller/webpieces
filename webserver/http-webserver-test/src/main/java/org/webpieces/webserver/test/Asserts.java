package org.webpieces.webserver.test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.webpieces.util.SneakyThrow;

public class Asserts {

	public static void assertWasCompiledWithParamNames(String param) {
		//This is here for those that forget to configure their IDE to compile with param names
		//We do this on eclipse generation and need to try to do it for intellij generation too
		Class<?> clazz;
		try {
			clazz = Class.forName(Asserts.class.getName());
		} catch (ClassNotFoundException e) {
			throw SneakyThrow.sneak(e);
		}
		Method[] method = clazz.getDeclaredMethods();
		Method target = null;
		for(Method m : method) {
			if("assertWasCompiledWithParamNames".equals(m.getName()))
				target = m;
		}
		
		if(target == null) 
			throw new IllegalStateException("method not found...you must have changed the method in this class");
		Parameter[] parameters = target.getParameters();
		String name = parameters[0].getName();
		if(!"param".equals(name))
			throw new IllegalStateException("Compiler option for compiling with param names is not on so we can't run this test.  arg name="+name+" when it should be 'param'");
	}


	
}
