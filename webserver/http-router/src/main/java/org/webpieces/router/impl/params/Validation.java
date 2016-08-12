package org.webpieces.router.impl.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validation {
	
	private static ThreadLocal<Validation> threadLocal = new ThreadLocal<>();

	public static void set(Validation validator) {
		threadLocal.set(validator);
	}

	public static Validation get() {
		return threadLocal.get();
	}
	
	public static void addError(String name, String error) {
		if(threadLocal.get() == null)
			throw new IllegalStateException("You can only call this method on the thread calling the controller function.  You can call on another thread by saving ref to Validation.get() and calling that later");
		threadLocal.get().addErrorInst(name, error);
	}
	
	public static void addGlobalMessage(String globalMessage) {
		if(threadLocal.get() == null)
			throw new IllegalStateException("You can only call this method on the thread calling the controller function.  You can call on another thread by saving ref to Validation.get() and calling that later");
		threadLocal.get().addGlobalMessageInst(globalMessage);
	}
	
	private Map<String, List<String>> fieldErrors = new HashMap<>();
	private List<String> globalMsgs = new ArrayList<>();

	public void addErrorInst(String name, String error) {
		List<String> list = fieldErrors.get(name);
		if(list == null) {
			list = new ArrayList<>();
			fieldErrors.put(name, list);
		}
		list.add(error);
	}	
	
	private void addGlobalMessageInst(String globalMessage) {
		globalMsgs.add(globalMessage);
	}
}
