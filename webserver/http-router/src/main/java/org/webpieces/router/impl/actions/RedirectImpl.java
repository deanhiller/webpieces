package org.webpieces.router.impl.actions;

import java.util.Arrays;

import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.routes.RouteId;

public class RedirectImpl implements Redirect {
	private RouteId id;
	private Object[] args;

	public RedirectImpl(RouteId id, Object ... args) {
		this.id = id;
		this.args = args;
	}

	public RouteId getId() {
		return id;
	}

	public Object[] getArgs() {
		return args;
	}

	@Override
	public String toString() {
		String enumClazz = id.getClass().getSimpleName();
		return "RedirectImpl [id=" + enumClazz+"."+id + ", args=" + Arrays.toString(args) + "]";
	}

}
