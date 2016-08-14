package org.webpieces.router.api.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderHtmlImpl;
import org.webpieces.router.impl.ctx.RequestLocalCtx;
import org.webpieces.router.impl.ctx.ResponseProcessor;

public class Actions {

	public static RenderHtml renderView(String view, Object ... pageArgs) {
		throw new UnsupportedOperationException("This is not quite supported yet");
	}

	public static RenderHtml renderThis(Object ... pageArgs) {
		RenderHtmlImpl renderHtml = new RenderHtmlImpl(pageArgs);
		ResponseProcessor processor = RequestLocalCtx.get();
		if(processor != null) {
			//If there is a context (the controller method is synchronous), then we do all calculations on their thread so if there
			//is an exception, their code is in the stack trace pointing the developer to the code lines to fix
			return processor.createRenderResponse(renderHtml);
		}
		return renderHtml;
	}

	public static Redirect redirect(RouteId routeId, Object ... args) {
		RedirectImpl redirect = new RedirectImpl(routeId, args);
		
		ResponseProcessor processor = RequestLocalCtx.get();
		if(processor != null) {
			//If there is a context (the controller method is synchronous), then we do all calculations on their thread so if there
			//is an exception, their code is in the stack trace pointing the developer to the code lines to fix
			return processor.createFullRedirect(redirect);
		}
		
		//If async, we pass back Redirect though stack trace will still include the user code in this case since we don't put our
		//futures on another thread and keep them resolving on the client code's thread so the stack trace still points to the issue
		return redirect;
	}

	public static Redirect redirectFlashAll(RouteId routeId, RequestContext ctx, String ... secureFieldNames) {
		Set<String> mySet = new HashSet<>(Arrays.asList(secureFieldNames));
		ctx.moveFormParamsToFlash(mySet);
		ctx.getFlash().keep();
		ctx.getValidation().keep();
		return redirect(routeId);
	}
	
//	public static Redirect redirectWithErrors(RouteId routeId, RequestContext ctx) {
//		
//	}
}
