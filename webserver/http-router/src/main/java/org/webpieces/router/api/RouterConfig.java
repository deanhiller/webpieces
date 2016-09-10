package org.webpieces.router.api;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

	private boolean isCookiesHttpOnly = true;

	private boolean isCookiesSecure = false;

	//location of precompressed static files(css, js, html, etc. etc....no jpg, png compressed)
	private File cachedCompressedDirectory = new File(System.getProperty("java.io.tmpdir")+"/webpiecesCache");
	//compression type to put in cachedCompressedDirectory
	private String startupCompression = "gzip";

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
	
	public boolean getIsCookiesHttpOnly() {
		return isCookiesHttpOnly;
	}
	public RouterConfig setCookiesHttpOnly(boolean isCookiesHttpOnly) {
		this.isCookiesHttpOnly = isCookiesHttpOnly;
		return this;
	}
	
	public boolean getIsCookiesSecure() {
		return isCookiesSecure;
	}
	public RouterConfig setCookiesSecure(boolean isCookiesSecure) {
		this.isCookiesSecure = isCookiesSecure;
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
		this.cachedCompressedDirectory = cachedCompressedDirectory;
		return this;
	}
	
	public String getStartupCompression() {
		return startupCompression;
	}

	public RouterConfig setStartupCompression(String startupCompression) {
		this.startupCompression = startupCompression;
		return this;
	}
}
