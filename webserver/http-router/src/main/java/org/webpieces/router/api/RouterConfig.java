package org.webpieces.router.api;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Module;

public class RouterConfig {

	private VirtualFile metaFile;
	
	private Charset fileEncoding = StandardCharsets.UTF_8;
	private Charset urlEncoding = StandardCharsets.UTF_8;
	private Charset defaultResponseBodyEncoding = StandardCharsets.UTF_8;
	private Charset defaultFormAcceptEncoding = StandardCharsets.UTF_8;

	private Locale defaultLocale = Locale.getDefault();

	private SecretKeyInfo secretKey;
	
	/**
	 * WebApps can override remote services to mock them out for testing or swap prod classes with
	 * an in-memory implementation such that tests can remain single threaded
	 */
	private Module webappOverrides;

	/**
	 * Option to turn token checking off mainly for testing as it disables ALL token checking which usually
	 * you never want to turn all of it off.  Default is for tokenCheck to be on(true)
	 */
	private boolean tokenCheckOn = true;

	//On startup, we protect developers from breaking clients.  In http, all files that change
	//must also change the hash url param automatically and the %%{ }%% tag generates those hashes so the
	//files loaded are always the latest
	//this is what gets put in the cache header for static files...and should be set to the max
	private Long staticFileCacheTimeSeconds = TimeUnit.SECONDS.convert(255, TimeUnit.DAYS);

	//location of precompressed static files(css, js, html, etc. etc....no jpg, png compressed)
	private File cachedCompressedDirectory;
	//compression type to put in cachedCompressedDirectory
	private String startupCompression = "gzip";

	private Map<String, Object> webAppMetaProperties = new HashMap<>();

	private File workingDirectory;

	private boolean isValidateFlash;
	private int scheduledThreadPoolSize = 5;

	public RouterConfig(File workingDirectory, String name) {
		if(!workingDirectory.isAbsolute())
			throw new IllegalArgumentException("baseDirectory must be absolute and can typically be FileFactory.getBaseDirectory()");
		this.workingDirectory = workingDirectory;		
		
		//default location is the user's home directory BUT production server overrides to it's working dir by setting it after
		cachedCompressedDirectory = PrecompressedCache2.getCacheLocation(name);
	}
	public RouterConfig(File workingDirectory, boolean isProduction) {
		if(!workingDirectory.isAbsolute())
			throw new IllegalArgumentException("baseDirectory must be absolute and can typically be FileFactory.getBaseDirectory()");
		else if(!isProduction) 
			throw new IllegalArgumentException("Only use this constructor for production");
		this.workingDirectory = workingDirectory;
	}
	
	public VirtualFile getMetaFile() {
		return metaFile;
	}
	public RouterConfig setMetaFile(VirtualFile routersFile) {
		if(!routersFile.exists())
			throw new IllegalArgumentException("path="+routersFile+" does not exist");
		else if(routersFile.isDirectory())
			throw new IllegalArgumentException("path="+routersFile+" is a directory and needs to be a file");
		this.metaFile = routersFile;
		return this;
	}
	
	public Module getWebappOverrides() {
		return webappOverrides;
	}
	public RouterConfig setWebappOverrides(Module webappOverrides) {
		this.webappOverrides = webappOverrides;
		return this;
	}

	public Charset getFileEncoding() {
		return fileEncoding;
	}
	public RouterConfig setFileEncoding(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
		return this;
	}
	
	public Charset getUrlEncoding() {
		return urlEncoding;
	}
	public RouterConfig setUrlEncoding(Charset urlEncoding) {
		this.urlEncoding = urlEncoding;
		return this;
	}
	
	public SecretKeyInfo getSecretKey() {
		return secretKey;
	}
	public RouterConfig setSecretKey(SecretKeyInfo signingKey) {
		this.secretKey = signingKey;
		return this;
	}

	public Long getStaticFileCacheTimeSeconds() {
		return staticFileCacheTimeSeconds ;
	}

	public RouterConfig setStaticFileCacheTimeSeconds(Long staticFileCacheTimeSeconds) {
		this.staticFileCacheTimeSeconds = staticFileCacheTimeSeconds;
		return this;
	}

	public Charset getDefaultResponseBodyEncoding() {
		return defaultResponseBodyEncoding;
	}
	public RouterConfig setDefaultResponseBodyEncoding(Charset defaultResponseBodyEncoding) {
		this.defaultResponseBodyEncoding = defaultResponseBodyEncoding;
		return this;
	}
	
	public File getCachedCompressedDirectory() {
		return cachedCompressedDirectory;
	}

	public RouterConfig setCachedCompressedDirectory(File cachedCompressedDirectory) {
		if(cachedCompressedDirectory.isAbsolute())
			this.cachedCompressedDirectory = cachedCompressedDirectory;
		else
			this.cachedCompressedDirectory = FileFactory.newFile(workingDirectory, cachedCompressedDirectory.getPath());
		return this;
	}
	
	public String getStartupCompression() {
		return startupCompression;
	}

	public RouterConfig setStartupCompression(String startupCompression) {
		this.startupCompression = startupCompression;
		return this;
	}
	public boolean isTokenCheckOn() {
		return tokenCheckOn;
	}
	public RouterConfig setTokenCheckOn(boolean tokenCheckOff) {
		this.tokenCheckOn = tokenCheckOff;
		return this;
	}
	public RouterConfig setWebAppMetaProperties(Map<String, Object> webAppMetaProperties) {
		this.webAppMetaProperties = webAppMetaProperties;
		return this;
	}
	public Map<String, Object> getWebAppMetaProperties() {
		return webAppMetaProperties;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	public RouterConfig setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
		return this;
	}

	public Charset getDefaultFormAcceptEncoding() {
		return defaultFormAcceptEncoding;
	}

	public RouterConfig setDefaultFormAcceptEncoding(Charset defaultFormAcceptEncoding) {
		this.defaultFormAcceptEncoding = defaultFormAcceptEncoding;
		return this;
	}
	public boolean isValidateFlash() {
		return isValidateFlash;
	}
	public RouterConfig setValidateFlash(boolean isValidateFlash) {
		this.isValidateFlash = isValidateFlash;
		return this;
	}
	public int getScheduledThreadPoolSize() {
		return scheduledThreadPoolSize;
	}
	public RouterConfig setScheduledThreadPoolSize(int scheduledThreadPoolSize) {
		this.scheduledThreadPoolSize = scheduledThreadPoolSize;
		return this;
	}
	
}
