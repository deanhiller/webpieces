package org.webpieces.router.api.routes;

public class CrudRouteIds {

	private RouteId listRoute;
	private RouteId addRoute;
	private RouteId editRoute;
	private RouteId postSaveRoute;
	private RouteId confirmDelete;
	private RouteId postDeleteRoute;

	public CrudRouteIds() {}
	
	public CrudRouteIds(RouteId listRoute, RouteId addRoute, RouteId editRoute,
			RouteId saveRoute, RouteId confirmDelete, RouteId deleteRoute) {
				this.listRoute = listRoute;
				this.addRoute = addRoute;
				this.editRoute = editRoute;
				this.postSaveRoute = saveRoute;
				this.confirmDelete = confirmDelete;
				this.postDeleteRoute = deleteRoute;
		
	}

	public RouteId getListRoute() {
		return listRoute;
	}

	public void setListRoute(RouteId listRoute) {
		this.listRoute = listRoute;
	}

	public RouteId getAddRoute() {
		return addRoute;
	}

	public void setAddRoute(RouteId addRoute) {
		this.addRoute = addRoute;
	}

	public RouteId getEditRoute() {
		return editRoute;
	}

	public void setEditRoute(RouteId editRoute) {
		this.editRoute = editRoute;
	}

	public RouteId getPostSaveRoute() {
		return postSaveRoute;
	}

	public void setPostSaveRoute(RouteId saveRoute) {
		this.postSaveRoute = saveRoute;
	}

	public RouteId getConfirmDelete() {
		return confirmDelete;
	}

	public void setConfirmDelete(RouteId confirmDelete) {
		this.confirmDelete = confirmDelete;
	}

	public RouteId getPostDeleteRoute() {
		return postDeleteRoute;
	}

	public void setPostDeleteRoute(RouteId deleteRoute) {
		this.postDeleteRoute = deleteRoute;
	}
	
}
