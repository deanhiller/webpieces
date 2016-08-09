package org.webpieces.router.api.actions;

import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderHtmlImpl;
import org.webpieces.router.impl.ctx.LocalContext;
import org.webpieces.router.impl.ctx.ResponseProcessor;

public class Actions {

	public static RenderHtml renderView(String view, Object ... pageArgs) {
		throw new UnsupportedOperationException("This is not quite supported yet");
	}

	public static RenderHtml renderThis(Object ... pageArgs) {
		RenderHtmlImpl renderHtml = new RenderHtmlImpl(pageArgs);
		ResponseProcessor ctx = LocalContext.getResponseProcessor();
		if(ctx != null) {
			//If there is a context (the controller method is synchronous), then we do all calculations on their thread so if there
			//is an exception, their code is in the stack trace pointing the developer to the code lines to fix
			return ctx.createRenderResponse(renderHtml);
		}
		return renderHtml;
	}

	public static Redirect redirect(RouteId routeId, Object ... args) {
		RedirectImpl redirect = new RedirectImpl(routeId, args);
		
		ResponseProcessor ctx = LocalContext.getResponseProcessor();
		if(ctx != null) {
			//If there is a context (the controller method is synchronous), then we do all calculations on their thread so if there
			//is an exception, their code is in the stack trace pointing the developer to the code lines to fix
			return ctx.createFullRedirect(redirect);
		}
		
		//If async, we pass back Redirect though stack trace will still include the user code in this case since we don't put our
		//futures on another thread and keep them resolving on the client code's thread so the stack trace still points to the issue
		return redirect;
	}
	
}
