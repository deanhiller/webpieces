package org.webpieces.router.api;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Module;

public class RouterConfig {

	private VirtualFile metaFile;
	
	private Charset fileEncoding = StandardCharsets.UTF_8;
	private Charset urlEncoding = StandardCharsets.UTF_8;
	private Charset defaultResponseBodyEncoding = StandardCharsets.UTF_8;

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
	
	//location of precompressed static files(css, js, html, etc. etc....no jpg, png compressed)
	private File cachedCompressedDirectory;
	//compression type to put in cachedCompressedDirectory
	private String startupCompression = "gzip";

	private Map<String, String> webAppMetaProperties;

	private PortConfigCallback portConfigCallback = () -> new PortConfig(8080, 8443);

	private File workingDirectory;

	private NeedsSimpleStorage needsStorage;

	public RouterConfig(File workingDirectory) {
		if(!workingDirectory.isAbsolute())
			throw new IllegalArgumentException("baseDirectory must be absolute and can typically be FileFactory.getBaseDirectory()");
		this.workingDirectory = workingDirectory;
		cachedCompressedDirectory = FileFactory.newFile(workingDirectory, "webpiecesCache/precompressedFiles");
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
	public RouterConfig setWebAppMetaProperties(Map<String, String> webAppMetaProperties) {
		this.webAppMetaProperties = webAppMetaProperties;
		return this;
	}
	public Map<String, String> getWebAppMetaProperties() {
		return webAppMetaProperties;
	}
	public RouterConfig setPortConfigCallback(PortConfigCallback callback) {
		this.portConfigCallback = callback;
		return this;
	}
	public PortConfigCallback getPortConfigCallback() {
		return portConfigCallback;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public RouterConfig setNeedsSimpleStorage(NeedsSimpleStorage needsStorage) {
		this.needsStorage = needsStorage;
		return this;
	}

	public NeedsSimpleStorage getNeedsStorage() {
		return needsStorage;
	}

}
