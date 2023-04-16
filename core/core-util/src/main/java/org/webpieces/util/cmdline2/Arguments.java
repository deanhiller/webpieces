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
	 * @param converter Validator and converter that throws exception if invalid or converts it string to string or string to another type
	 * @return a function that will be called later to get the cmd line argument
	 */
	<T> Supplier<T> createOptionalArg(String argumentKey, String defaultValue, String help, Function<String, T> converter);

	/**
	 * Do not use if you need to validate the string coming in.  This function just
	 * call createOptionaArg(......, (s) -> s) doing no validation and no conversion
	 *
	 * @param argumentKey The cmdLine key as in db.port=0 (db.port is the key)
	 * @param defaultValue Default value if argumentKey is not found on command line
	 * @param help The help to show the user when missing arguments (or too many arguments)
	 * @return a function that will be called later to get the cmd line argument
	 */
	Supplier<String> createOptionalArg(String argumentKey, String defaultValue, String help);

	/**
	 * Environment variables are more secure in that if a hacker gets on the system and lists the
	 * processes, environment variables are not shown with the command.  Passwords/SecureTokens are
	 * best passed in as environment variables
	 */
	<T> Supplier<T> createOptionalEnvVar(String envVarName, String defaultValue, String help, Function<String, T> converter);

	/**
	 * Does NO validation and simply calls createOptionalEnvVar(envVarName, help, (s) -> s);
	 * Normally, you should pass in a function that validates and throws exception to fail startup.
	 */
	<T> Supplier<T> createOptionalEnvVar(String envVarName, String defaultValue, String help);

	/**
	 * @param <T> The type the argument will be converted to
	 * @param argumentKey The cmdLine key as in db.port=0 (db.port is the key)
	 * @param help The help to show the user when missing arguments (or too many arguments)
	 * @param converter Validator and converter that throws exception if invalid or converts it string to string or string to another type
	 * @return a function that will be called later to get the cmd line argument
	 */
	<T> Supplier<T> createRequiredArg(String argumentKey, String help, Function<String, T> converter);

	/**
	 * Do not use if you need to validate the string coming in.  This function just
	 * call createRequiredArg(......, (s) -> s) doing no validation and no conversion
	 *
	 * @param argumentKey The cmdLine key as in db.port=0 (db.port is the key)
	 * @param help The help to show the user when missing arguments (or too many arguments)
	 * @return a function that will be called later to get the cmd line argument
	 */
	Supplier<String> createRequiredArg(String argumentKey, String help);

	/**
	 * Environment variables are more secure in that if a hacker gets on the system and lists the
	 * processes, environment variables are not shown with the command.  Passwords/SecureTokens are
	 * best passed in as environment variables
	 */
	<T> Supplier<T> createRequiredEnvVar(String envVarName, String help, Function<String, T> converter);

	/**
	 * Does NO validation and simply calls createRequiredEnvVar(envVarName, help, (s) -> s);
	 * Normally, you should pass in a function that validates and throws exception to fail startup.
	 */
	Supplier<String> createRequiredEnvVar(String envVarName, String help);

	/**
	 * @deprecated READ createRequiredEnvVar instead or use createRequiredArg
	 */
	@Deprecated
	<T> Supplier<T> createRequiredArgOrEnvVar(String argumentKey, String envVarName, String help, Function<String, T> converter);


	/**
	 * Special case convience method converting :{port} or {host}:{port} to InetSocketAddress
	 */
	Supplier<InetSocketAddress> createOptionalInetArg(String argumentKey, String defaultValue, String help);

	/**
	 * See if an optional key exists or not.  
	 */
	Supplier<Boolean> createDoesExistArg(String key, String help);

}
