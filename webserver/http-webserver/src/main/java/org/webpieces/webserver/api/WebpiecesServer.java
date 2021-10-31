package org.webpieces.webserver.api;

import java.io.File;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Module;

public class WebpiecesServer {

	private static final Logger log = LoggerFactory.getLogger(WebpiecesServer.class);

	private final WebServer webServer;

	private final boolean isRunningServerMainMethod;

	@Deprecated
	public WebpiecesServer(
		String projectName,
		String base64Key,
		Module platformOverrides, 
		Module appOverrides, 
		ServerConfig svrConfig, 
		String ... args
	) {
		this(projectName, base64Key, null, platformOverrides, appOverrides, svrConfig, args);
	}
	
	/**
	 * @param platformOverrides For tests, DevelopmentServer to swap pieces out and for YOU so you can bug fix by just swapping a class and filing a ticket with the class you used to fix the bug!
	 * @param appOverrides For unit testing so you can swap remote clients with mocks and simulate remote systems
	 */
	public WebpiecesServer(
		String projectName,
		String base64Key,
		Module coreModule, //core additionalModule
		Module platformOverrides, 
		Module appOverrides, 
		ServerConfig svrConfig, 
		String ... args
	) {
		
		//ALWAYS install a catch all on all threads
		Thread.setDefaultUncaughtExceptionHandler(new WebpiecesExceptionHandler());
		
		isRunningServerMainMethod = svrConfig.isRunningServerMainMethod();
		//read here and checked for correctness on last line of server construction
		Arguments arguments = new CommandLineParser().parse(args);

		File baseWorkingDir = modifyUserDirForManyEnvironments(projectName);

		//Different pieces of the server have different configuration objects where settings are set
		//You could move these to property files but definitely put some thought if you want people 
		//randomly changing those properties and restarting the server without going through some testing
		//by a QA team.  We leave most of these properties right here so changes get tested by QA.
		
		//A SECOND note is that webpieces strives to default most configuration and expose it through an
		//amazing properties plugin that not only has a web page for making changes BUT persists those
		//changes across the cluster so they are re-applied at startup
		RouterConfig routerConfig = new RouterConfig(baseWorkingDir, projectName)
											.setMetaFile(svrConfig.getMetaFile())
											.setWebappOverrides(appOverrides)
											.setSecretKey(new SecretKeyInfo(fetchKey(base64Key), "HmacSHA1"))
											.setCachedCompressedDirectory(svrConfig.getCompressionCacheDir())
											.setTokenCheckOn(svrConfig.isTokenCheckOn())
											.setValidateFlash(svrConfig.isValidateFlash())
											.setStaticFileCacheTimeSeconds(svrConfig.getStaticFileCacheTimeSeconds());

		WebServerConfig config = new WebServerConfig()
										.setCorePlatformModule(coreModule)
										.setPlatformOverrides(platformOverrides)
										.setValidateRouteIdsOnStartup(svrConfig.isValidateRouteIdsOnStartup());
										

		TemplateConfig templateConfig = new TemplateConfig();
		
		//Notice that there is a WebServerConfig, a RouterConfig, and a TemplateConfig making up
		//3 of the major pieces of webpieces.
		webServer = WebServerFactory.create(config, routerConfig, templateConfig, arguments);

		//Before this line, every module calls into arguments telling it help and required or not and ZERO
		//arguments can be read in this phase.  After this is called, all arguments can be read
		arguments.checkConsumedCorrectly();
	}

	private byte[] fetchKey(String base64Key) {
		//This 'if' statement is purely so it works before template creation
		//NOTE: our build runs all template tests that are generated to make sure we don't break template 
		//generation but for that to work pre-generation, we need this code but you are free to delete it...
		if(base64Key.startsWith("__SECRETKEY"))  //This does not get replaced (user can remove it from template)
			return base64Key.getBytes();
		
		//This code must stay so we translate the base64 back into bytes...
		return Base64.getDecoder().decode(base64Key);
	}

	private File modifyUserDirForManyEnvironments(String projectName) {
		String filePath = System.getProperty("user.dir");
		File absPath = FileFactory.newAbsoluteFile(filePath);
		log.info("original user.dir before modification="+absPath);
		
		File finalUserDir = modifyUserDirForManyEnvironmentsImpl(projectName, absPath);
		log.info("RECONFIGURED 333 working directory(based off user.dir)="+finalUserDir.getAbsolutePath()+" previous user.dir="+filePath);
		return finalUserDir;
	}

	/**
	 * I like things to work seamlessly but user.dir is a huge issue in multiple environments...and Intellij makes it
	 * harder by giving servers a different user.dir than tests even though they are in the same subproject!!
	 *
	 * Format of comments BELOW in if/else statements is like this
	 *
	 * {type}-{isWebpieces}-{IDE or Container}-{subprojectName}
	 *
	 * where type=Test or MainApp (Intellij changes the user.dir for tests vs. mainapp!!  DAMNIT Intellij)
	 * IDE=Intellij, Eclipse, Gradle, Production Script
	 * isWebpieces is whether it was a generated project or is the template itself.  ie. you can run tests
	 *     if you clone https://github.com/deanhiller/webpieces inside the IDE without needing to
	 *     generate a fake project BUT we need to know which directory it runs for (MAINLY Intellij screwup again)
	 *     isWebpieces is a major convenience for webpieces developers to test changes to templates and
	 *     debug them but DOES NOT need to be part of your project actually so could be deleted.
	 */
	private File modifyUserDirForManyEnvironmentsImpl(String projectName, File filePath) {
		if(!filePath.isAbsolute())
			throw new IllegalArgumentException("If filePath is not absolute, you will have trouble working in all environments in the comment above. path="+filePath.getPath());

		String name = filePath.getName();

		File locatorFile1 = FileFactory.newFile(filePath, "locatorFile.txt");
		File locatorFile2 = FileFactory.newFile(filePath, "xLocatorFile.txt");

		File bin = FileFactory.newFile(filePath, "bin");
		File lib = FileFactory.newFile(filePath, "lib");
		File config = FileFactory.newFile(filePath, "config");
		File publicFile = FileFactory.newFile(filePath, "public");
		File jibClasspath = FileFactory.newFile(filePath, "jib-classpath-file");
		if((bin.exists() && lib.exists() && config.exists() && publicFile.exists()) ||  (jibClasspath.exists())) {
			//For ->
			//	 {type}   |{isWebpieces} | {IDE or Container} | {subprojectName}
			//    MainApp | NO  | Production | N/A
			log.info("Running in production environment");
			return filePath;
		} else if((projectName+"-dev").equals(name)) {
			//	 {type}   |{isWebpieces} | {IDE or Container} | {subprojectName}			
			//    Test    | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | YES | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | NO  | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | YES | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    MainApp | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    MainApp | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			//    Test    | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			log.info("You appear to be running test from Intellij, Eclipse or Gradle(xxxx-dev subproject), or the DevelopmentServer.java/ProdServerForIDE.java from eclipse");
			File parent = filePath.getParentFile();
			return FileFactory.newFile(parent, projectName+"/src/dist");
		} else if(projectName.equals(name)) {
			//	 {type}   |{isWebpieces} | {IDE or Container} | {subprojectName}
			//    Test    | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | YES | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | NO  | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | YES | Gradle     | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | NO  | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    Test    | YES | Eclipse    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			if(isRunningServerMainMethod) {
				log.info("You appear to be running Server.java from Eclipse");
				throw new NoRunningServerMainInIDEException(); 
			} else {	
				log.info("You appear to be running test from Intellij, Eclipse or Gradle(main subproject)");
				return FileFactory.newFile(filePath, "src/dist");
			}
		} else if(locatorFile1.exists()) {
			//DAMNIT Intellij...FIX THIS STUFF!!!
			//For ->
			//	 {type}   |{isWebpieces} | {IDE or Container} | {subprojectName}
			//    MainApp | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | NO  | Intellij   | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			if(isRunningServerMainMethod) {
				log.info("You appear to be running Server.java from Intellij");
				throw new NoRunningServerMainInIDEException(); 
			} else {
				log.info("You appear to be running DevelopmentServer.java/ProdServerForIDE.java from Intellij");
				return FileFactory.newFile(filePath, projectName+"/src/dist");
			}
		} else if(locatorFile2.exists()) {
			//DAMNIT Intellij...FIX THIS STUFF!!!
			//
			//   This section is only for webpieces use and can safely be deleted for your project if you want to reduce clutter
			//
			//	 {type}   |{isWebpieces} | {IDE or Container} | {subprojectName}
			//    MainApp | YES | Intellij    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME
			//    MainApp | YES | Intellij    | WEBPIECESxAPPNAME-all/WEBPIECESxAPPNAME-dev
			if(isRunningServerMainMethod) {
				log.info("You appear to be running "+projectName+".Server.java from webpieces in Intellij");
				throw new NoRunningServerMainInIDEException(); 
			} else {
				log.info("You appear to be running DevelopmentServer.java/ProdServerForIDE.java in webpieces project from Intellij");
				return FileFactory.newFile(filePath, "webserver/webpiecesServerBuilder/templateProject/"+projectName+"/src/dist");
			}
		}

		throw new IllegalStateException("bug, we must have missed an environment="+name+" full path="+filePath);
	}

	private class NoRunningServerMainInIDEException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public NoRunningServerMainInIDEException() {
			super("Please do one of the following:\n"
					+ "1. run DevelopmentServer.java or ProdServerForIDE.java instead of Server.java from IDE OR\n"
					+ "2. run ./gradle assembleDist and run the full blown prod server which is temporarily setup with H2 in-memory and will work\n"
					+ "NOTE: Running Server.java NEVER will work in the IDE as it needs pre-compiled *.html -> *.class files which only happen in gradle.\n"
					+ "The ProdServerForIDE compiles html files for you as does the DevelopmentServer(DevelopmentServer also hot compiles *.java files as you change them");
		}
	}

	public void start() {
		webServer.startSync();
	}

	public void stop() {
		webServer.stop();
	}

	public TCPServerChannel getUnderlyingHttpChannel() {
		return webServer.getUnderlyingHttpChannel();
	}

	public TCPServerChannel getUnderlyingHttpsChannel() {
		return webServer.getUnderlyingHttpsChannel();
	}

}

