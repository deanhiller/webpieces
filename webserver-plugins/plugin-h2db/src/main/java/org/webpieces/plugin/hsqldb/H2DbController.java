package org.webpieces.plugin.hsqldb;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class H2DbController {

	private ServerConfig config;
	private H2DbConfig h2DbConfig;

	@Inject
	public H2DbController(ServerConfig config, H2DbConfig h2DbConfig) {
		this.config = config;
		this.h2DbConfig = h2DbConfig;
	}
	
	public Redirect redirectToDatabaseGui() {
		if(h2DbConfig.getConvertDomain() != null) {
			Function<String, String> function = h2DbConfig.getConvertDomain();
			String newDomain = function.apply(Current.request().domain);
			String url = "http://"+newDomain;
			return Actions.redirectToUrl(url);
		}
		
		//could be https OR could be backend....no matter what, redirect to the http server
		return Actions.redirect(HttpPort.HTTP, H2DbRouteId.DATABASE_GUI_PAGE);
	}
	
	//currently needs to be served over http server but this is only for development anyways
	public Render databaseGui() {
		String url = "http://localhost:"+config.getPort();
		return Actions.renderThis("url", url);
	}
}
