package WEBPIECESxPACKAGE;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.util.file.VirtualFile;

public class ServerConfig {

	private VirtualFile metaFile;
	private boolean validateRouteIdsOnStartup = false;
	private int httpPort = 8080;
	private int httpsPort = 8443;
	private int httpsFirewallPort = 8443; // for real production should generally be 443 but this makes it work locally
	private Long staticFileCacheTimeSeconds = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS);
	private Map<String, String> webAppMetaProperties = new HashMap<>();

	public ServerConfig(int httpPort, int httpsPort, String persistenceUnit) {
		this(persistenceUnit);
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
		this.httpsFirewallPort = httpsPort;
	}
	
	public ServerConfig(String persistenceUnit) {
		webAppMetaProperties.put(HibernatePlugin.PERSISTENCE_UNIT_KEY, persistenceUnit);
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
	public int getHttpPort() {
		return httpPort;
	}
	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
	public int getHttpsPort() {
		return httpsPort;
	}
	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
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

	public int getHttpsFirewallPort() {
		return httpsFirewallPort;
	}

	public void setHttpsFirewallPort(int httpsFirewallPort) {
		this.httpsFirewallPort = httpsFirewallPort;
	}
	
}
