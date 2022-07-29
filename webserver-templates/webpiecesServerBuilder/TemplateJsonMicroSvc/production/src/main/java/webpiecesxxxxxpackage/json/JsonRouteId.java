package webpiecesxxxxxpackage.json;

import org.webpieces.router.api.routes.RouteId;

//You can have many RoutId files extending from RouteId so you can group RouteIds together like per package
public enum JsonRouteId implements RouteId {

	/**
	 * You only need json route ids if the json is called from your website so you can use the route id
	 * to reverse it to the url for you
	 */
	STREAMING_ROUTE, READ
	
}
