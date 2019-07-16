package org.webpieces.util.cmdline2;

import java.net.InetSocketAddress;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Arguments {

	//IF multiple things read the same key, we will print out multiple help arguments
	
	<T> Supplier<T> consumeOptional(String argumentKey, String defaultValue, String help, Function<String, T> converter);
	<T> Supplier<T> consumeRequired(String argumentKey, String help, Function<String, T> converter);

	/**
	 * Special case convience method converting :{port} or {host}:{port} to InetSocketAddress
	 */
	Supplier<InetSocketAddress> consumeOptionalInet(String argumentKey, String defaultValue, String help);
	
	/**
	 * See if an optional key exists or not.  
	 */
	Supplier<Boolean> consumeDoesExist(String key, String help);

	/**
	 * After all the modules/routesFiles/plugins call the above methods, the main program then
	 * can call this method to check that the command line had no errors and all arguments that
	 * need to exist actually exist
	 */
	void checkConsumedCorrectly();
	
	/**
	 * If you run just construction to call all the consumeXXX methods, then you can call this
	 * to print commandline help
	 */
	String commandLineHelpMessage();
}
