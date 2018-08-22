package org.webpieces.plugins.hsqldb;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

@Singleton
public class H2DbController {

	private ServerConfig config;
	private String path;

	@Inject
	public H2DbController(ServerConfig config, H2DbConfig hdConfig) {
		this.config = config;
		this.path = hdConfig.getUrlPath();
	}
	
	public Action databaseGui() {
		RouterRequest request = Current.request();
		if(request.isHttps)
			return Actions.redirectToUrl("http://localhost:8080"+path);
		
		return Actions.renderThis("port", config.getPort());
	}
}
