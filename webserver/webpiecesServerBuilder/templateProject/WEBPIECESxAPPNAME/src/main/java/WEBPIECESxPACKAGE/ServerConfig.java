package WEBPIECESxPACKAGE;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;

public class ServerConfig {

	private VirtualFile metaFile;
	private boolean validateRouteIdsOnStartup = false;
	private boolean isUseFirewall = false;
	private InetSocketAddress httpAddress = new InetSocketAddress(8080);
	private InetSocketAddress httpsAddress = new InetSocketAddress(8443);
	private Long staticFileCacheTimeSeconds = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS);
	private File compressionCacheDir;
	private Map<String, String> webAppMetaProperties = new HashMap<>();
	private boolean tokenCheckOn = true;
	private InetSocketAddress backendAddress;

	public ServerConfig(int httpPort, int httpsPort, String persistenceUnit, File compressionCache) {
		webAppMetaProperties.put(HibernatePlugin.PERSISTENCE_UNIT_KEY, persistenceUnit);
		this.httpAddress = new InetSocketAddress(httpPort);
		this.httpsAddress = new InetSocketAddress(httpsPort);
		this.compressionCacheDir = compressionCache;
	}
	
	public ServerConfig(String persistenceUnit, File compressionCache) {
		//For tests, we need to bind to port 0, then lookup the port after that...
		this(0, 0, persistenceUnit, compressionCache);
		tokenCheckOn = false;
	}

	//really for production use only...
	public ServerConfig(String persistenceUnit) {
		this(8080, 8443, persistenceUnit, FileFactory.newBaseFile("webpiecesCache/precompressedFiles"));
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
	public InetSocketAddress getHttpAddress() {
		return httpAddress;
	}
	public void setHttpAddress(InetSocketAddress httpAddress) {
		this.httpAddress = httpAddress;
	}
	public InetSocketAddress getHttpsAddress() {
		return httpsAddress;
	}
	public void setHttpsAddress(InetSocketAddress httpsAddress) {
		this.httpsAddress = httpsAddress;
	}
	public Long getStaticFileCacheTimeSeconds() {
		return staticFileCacheTimeSeconds ;
	}

	public void setStaticFileCacheTimeSeconds(Long staticFileCacheTimeSeconds) {
		this.staticFileCacheTimeSeconds = staticFileCacheTimeSeconds;
	}

	public Map<String, String> getWebAppMetaProperties() {
		return webAppMetaProperties;
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

	public boolean isUseFirewall() {
		return isUseFirewall;
	}

	public void setUseFirewall(boolean isUseFirewall) {
		this.isUseFirewall = isUseFirewall;
	}

	public ServerConfig setBackendAddress(InetSocketAddress backendAddress) {
		this.backendAddress = backendAddress;
		return this;
	}

	public InetSocketAddress getBackendAddress() {
		return backendAddress;
	}

}
