package org.webpieces.util.cmdline2;

public interface ArgumentsCheck extends Arguments {

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
