package org.webpieces.router.api.plugins;

import org.webpieces.router.api.routes.RouteId;

import java.util.Map;

public interface ReverseRouteLookup {

	boolean isGetRequest(RouteId routeId);

	/**
	 * In many cases, you simply want the postfix like embedding into a website so the domain and port
	 * and scheme (https/http://{domain}:{port} are all re-used without this library doing anything.
	 *
	 * In other cases, you need a full url to give a vendor to redirect back in which case fullPathRequired=true
	 */
	String convertToUrl(RouteId routeId, boolean fullPathRequired);

	/**
	 * In many cases, you simply want the postfix like embedding into a website so the domain and port
	 * and scheme (https/http://{domain}:{port} are all re-used without this library doing anything.
	 *
	 * In other cases, you need a full url to give a vendor to redirect back in which case fullPathRequired=true
	 *
	 * This method fills in url params in a path like https://{domain}/mypath/{userId} so you can just
	 * feed required path params into the creation of url.
	 */
	String convertToUrl(RouteId routeId, Map<String, Object> params, boolean fullPathRequired);

}
