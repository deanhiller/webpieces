package WEBPIECESxPACKAGE.base.examples;

import org.webpieces.router.api.routing.RouteId;

//You can have many RoutId files extending from RouteId so you can group RouteIds together like per package
public enum ExamplesRouteId implements RouteId {
	
	//all of these reference the same controller but with different example of controller definitions
	MAIN_ROUTE, MAIN_ROUTE2, MAIN_ROUTE3,
	
	LIST_EXAMPLES, 

	//controller type basic examples
	REDIRECT_PAGE, ASYNC_ROUTE, HOME, TAGS
	
	//using different tag examples
	
	
	//other examples are in CrudUserRouteId.java for doing CRUD on any entity
}
