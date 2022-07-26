package org.webpieces.webserver.api;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;

public class ServerConfig {

	/**
	 * The bootstrap file that separates code that can't be recompiled from all the code
	 * that can in your DevelopmentServer.  This file references the Meta file needed to
	 * load your web application.  The default is the 'production' appmeta.txt meta file
	 * while the DevelopmentServer uses appmetadev.txt such that it can load a few more
	 * plugins.
	 */
	private VirtualFile metaFile= new VirtualFileClasspath("appmeta.txt", WebpiecesServer.class.getClassLoader());

	/**
	 * This is not used in production so we default it to false, but a special test that we
	 * already wrote for you sets this to true such that we validate all route ids in your
	 * html files on startup.  Go us!!!
	 */
	private boolean validateRouteIdsOnStartup = false;
	
	/**
	 * This sets the cache-control: max-age={time} for production server.  The DevelopmentServer
	 * completely avoids caching.  Also, as long as you use the %%{ }%% tag, webpieces will change
	 * the url of the updated file for you.  ie. update, the file and the hash changes on all pages
	 * referencing that file.   This means you should ALWAYS cache at the max time which is 1 year
	 * since as you update your app, customers avoid the old cached version anyways.
	 */
	private Long staticFileCacheTimeSeconds = TimeUnit.SECONDS.convert(255, TimeUnit.DAYS);
	
	/**
	 * On startup, webpieces compresses all text/css/js resources such that when it sends it to
	 * a browser, it does not have to do compression but can just send the compressed file to the
	 * browser for better speed.
	 */
	private File compressionCacheDir;
	
	/**
	 * For testing, it's easier to turn this off.  When rendering the webpieces #{form}# tag,
	 * webpieces sticks a special security token as one of the fields that it will verify on the
	 * POST request for higher security.  This is very annoying for testing and is turned off
	 * here by flipping this flag to false
	 */
	private boolean tokenCheckOn = true;
	
	private final boolean isRunningServerMainMethod;
	
	private boolean isValidateFlash = false;

	public ServerConfig(File compressionCache, boolean isServerMainMethod) {
		this.compressionCacheDir = compressionCache;
		this.isRunningServerMainMethod = isServerMainMethod;
		
		if(!isServerMainMethod) {
			//We enter here for tests, DevelopmentServer, ProdServerForIDE and should validate flashed scopes..
			
			//We validate this so your customers/users of the website can type in stuff and error validations, they won't
			//lose what they typed in as you hopefully call flash.keep and validation.keep
			isValidateFlash = true; //For tests, validate flash methods are ALWAYS invoked in post methods
		}
	}
	
	public ServerConfig(File compressionCache) {
		//For tests, we need to bind to port 0, then lookup the port after that...
		this(compressionCache, false);
		tokenCheckOn = false;
	}

	//8080, 8443
	public ServerConfig(boolean isServerMainMethod) {
		this(FileFactory.newBaseFile("webpiecesCache/precompressedFiles"), isServerMainMethod);
	}
	
	public VirtualFile getMetaFile() {
		return metaFile;
	}
	public void setMetaFile(VirtualFile metaFile) {
		this.metaFile = metaFile;
	}
	public boolean isValidateRouteIdsOnStartup() {
		return validateRouteIdsOnStartup;
	}
	public void setValidateRouteIdsOnStartup(boolean validateRouteIdsOnStartup) {
		this.validateRouteIdsOnStartup = validateRouteIdsOnStartup;
	}

	public Long getStaticFileCacheTimeSeconds() {
		return staticFileCacheTimeSeconds ;
	}

	public void setStaticFileCacheTimeSeconds(Long staticFileCacheTimeSeconds) {
		this.staticFileCacheTimeSeconds = staticFileCacheTimeSeconds;
	}

	public File getCompressionCacheDir() {
		return compressionCacheDir;
	}

	public void setCompressionCacheDir(File compressionCacheDir) {
		this.compressionCacheDir = compressionCacheDir;
	}

	public boolean isTokenCheckOn() {
		return tokenCheckOn;
	}
	public ServerConfig setTokenCheckOn(boolean tokenCheckOff) {
		this.tokenCheckOn = tokenCheckOff;
		return this;
	}

	public boolean isRunningServerMainMethod() {
		return isRunningServerMainMethod;
	}

	public boolean isValidateFlash() {
		return isValidateFlash;
	}

	public ServerConfig setValidateFlash(boolean isValidateFlash) {
		this.isValidateFlash = isValidateFlash;
		return this;
	}
}
