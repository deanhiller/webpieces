package org.webpieces.util.cmdline2;

import java.net.InetSocketAddress;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Arguments {

	//IF multiple things read the same key, we will print out multiple help arguments
	
	/** 
	 * @param <T> The type the argument will be converted to
	 * @param argumentKey The cmdLine key as in db.port=0 (db.port is the key)
	 * @param defaultValue Default value if argumentKey is not found on command line
	 * @param help The help to show the user when missing arguments (or too many arguments)
	 * @param converter Converts the cmd line String into the correct type or fails with the Help message to the user
	 * @return a function that will be called later to get the cmd line argument
	 */
	<T> Supplier<T> createOptionalArg(String argumentKey, String defaultValue, String help, Function<String, T> converter);

	/** 
	 * @param <T> The type the argument will be converted to
	 * @param argumentKey The cmdLine key as in db.port=0 (db.port is the key)
	 * @param help The help to show the user when missing arguments (or too many arguments)
	 * @param converter Converts the cmd line String into the correct type or fails with the Help message to the user
	 * @return a function that will be called later to get the cmd line argument
	 */
	<T> Supplier<T> createRequiredArg(String argumentKey, String help, Function<String, T> converter);

	/**
	 * Special case convience method converting :{port} or {host}:{port} to InetSocketAddress
	 */
	Supplier<InetSocketAddress> createOptionalInetArg(String argumentKey, String defaultValue, String help);
	
	/**
	 * See if an optional key exists or not.  
	 */
	Supplier<Boolean> createDoesExistArg(String key, String help);
	
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
