package WEBPIECESxPACKAGE.web.main;

import org.webpieces.router.api.routes.RouteId;

//You can have many RoutId files extending from RouteId so you can group RouteIds together like per package
public enum AppRouteId implements RouteId {
	
	MAIN_ROUTE, ASYNC_ROUTE, SYNC_ROUTE
	
}
