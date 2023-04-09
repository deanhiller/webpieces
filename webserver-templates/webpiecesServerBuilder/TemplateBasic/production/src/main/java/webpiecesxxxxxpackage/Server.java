package webpiecesxxxxxpackage;

import java.util.ArrayList;

import org.webpieces.webserver.api.ServerConfig;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import webpiecesxxxxxpackage.basesvr.YourCompanyServer;

/**
 * Changes to any class in this 'package' (or any classes that classes in this 
 * package reference) WILL require a restart when you are running the DevelopmentServer.  
 * This class should try to remain pretty thin and you should avoid linking any 
 * classes in this package to classes outside this package(This is only true if 
 * you want to keep using the development server).  In production, we do not 
 * play any classloader games at all(unlike play framework) avoiding any prod issues.
 */
public class Server extends YourCompanyServer {

	/*******************************************************************************
	 * When running the dev server, changes to this file AND to any files in this package
	 * require a server restart(you can try not to but it won't work)
	 *******************************************************************************/
	public static final String APP_NAME = "WEBPIECESxAPPNAME";
	
	/**
	 * Welcome to YOUR main method as webpieces webserver is just a LIBRARY you use that you can
	 * swap literally any piece of
	 */
	public static void main(String[] args) throws InterruptedException {
		//We typically move this to the command line so staging can have
		//-hibernate.persistenceunit=stagingdb instead but to help people startup, we add the arg
		String[] newArgs = addArgs(new String[] {"-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsProd"});
		
		YourCompanyServer.main( (config) -> new Server(null, null, config, newArgs));
	}


	/**
	 * @param platformOverrides For a few things, for DevelopmentServer to swap in pieces with compilers that can compile on demand OR 
	 *                           For fixing bugs in any classes by swapping them so you don't have to fork git and fix(Please do submit fixes though)
	 *                           For tests to compile the html on demand at least so tests run in the IDE without needing a gradle build to compile html files
	 * @param appOverrides For Unit testing your app so you can swap out remote clients with mocks
	 */
	public Server(
		Module platformOverrides,
		Module appOverrides, 
		ServerConfig svrConfig, 
		String ... args
	) {
		super(APP_NAME, platformOverrides,
				appOverrides, svrConfig, args);
	}
	
	/**
	 * Delete this as it is only used for providing args until you add stuff to the real command line
	 */
	private static String[] addArgs(String[] originalArgs, String ... additionalArgs) {
		ArrayList<String> listArgs = Lists.newArrayList(originalArgs);
		for(String arg : additionalArgs) {
			listArgs.add(arg);
		}
		return listArgs.toArray(new String[0]);
	}

}
