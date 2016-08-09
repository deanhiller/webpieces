package PACKAGE;

import org.webpieces.util.file.VirtualFile;

public class ServerConfig {

	private VirtualFile metaFile;
	private boolean validateRouteIdsOnStartup = false;
	private int httpPort = 8080;
	private int httpsPort = 8443;
	
	public ServerConfig(int httpPort, int httpsPort) {
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
	}
	
	public ServerConfig() {
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
	
}
