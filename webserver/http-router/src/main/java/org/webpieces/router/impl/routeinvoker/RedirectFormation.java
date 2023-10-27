package org.webpieces.router.impl.routeinvoker;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.PortConfigLookup;
import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.routers.MatchInfo;

@Singleton
public class RedirectFormation {

	private PortConfigLookup portConfigLookup;

	@Inject
	public RedirectFormation(PortConfigLookup portConfigLookup) {
		this.portConfigLookup = portConfigLookup;
	}

	public PortAndIsSecure calculateInfo(MatchInfo matchInfo, HttpPort requestedPort, RouterRequest request) {
		boolean isHttpsOnly = matchInfo.getExposedPorts() == Port.HTTPS;

		//if the request is https, stay in https as everything is accessible on https
		//if the request is http, then convert to https IF new route is secure
		boolean isSecure = request.isHttps || isHttpsOnly;
		Integer port = request.port;
		//if need to change port to https port, this is how we do it...
		if(!request.isHttps && isHttpsOnly)
			port = calculateHttpsPort(request);
		
		//lastly override to requests http or https port if requested
		if(requestedPort == HttpPort.HTTP && isHttpsOnly)
			throw new IllegalArgumentException("Your controller is trying to direct to http for a page only served over https");
		else if(requestedPort != null) {
			if(requestedPort == HttpPort.HTTPS) {
				port = calculateHttpsPort(request);
				isSecure = true;
			} else if(requestedPort == HttpPort.HTTP) {
				port = calculateHttpPort(request);
				isSecure = false;
			} else
				throw new IllegalStateException("Bug, developer did not add a new enum value here="+requestedPort);
		}
		
		return new PortAndIsSecure(port, isSecure);
	}

	private int calculateHttpPort(RouterRequest request) {
		if(request.port == 80 || request.port == 443) {
			//we must be using a firewall
			return 80;
		}
		
		PortConfig ports = portConfigLookup.getPortConfig();
		return ports.getHttpPort();
	}

	public Integer calculateHttpsPort(RouterRequest request) {
		//OK, to get redirect business correct, we have to account for firewalls where clients
		//send requests to port 80 and the request comes into our server in a different
		//port than that...we still need to redirect back to 443 and not the port that
		//our server is bound to
		if(request.port == 80) {
			//in this case, it's a firewall OR you just have your server on standard ports
			return null;
		}

		if(request.getSingleHeader("x-forwarded-proto") != null)
			return null;

		//Orrrr, since it was not port 80, it's a test or other and we need to redirect
		//to the port that our webserver bound to in https
		PortConfig ports = portConfigLookup.getPortConfig();
		Integer httpsPort = ports.getHttpsPort();
		return httpsPort;
	}
}
