package org.webpieces.router.api.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.ctx.RequestLocalCtx;
import org.webpieces.router.impl.ctx.ResponseProcessor;

public class Actions {

	/*
	 * For better stack traces to see which controller was involved, use this (only because java
	 * does not support a FutureContext where we could pass state through the customer code
	 */
	public static Render renderView(ResponseProcessor processor, String templatePath, Object ... pageArgs) {
		RenderImpl renderHtml = new RenderImpl(templatePath, pageArgs);
		return processor.createRenderResponse(renderHtml);
	}
	
	/**
	 * Renders the html file at templatePath which is relative to the controller or an 
	 * absolute reference from the start of the classpath
	 * 
	 * @param templatePath
	 * @param pageArgs
	 * @return
	 */
	public static Render renderView(String templatePath, Object ... pageArgs) {
		ResponseProcessor processor = RequestLocalCtx.get();
		if(processor != null) {
			//If there is a context (the controller method is synchronous), then we do all calculations on their thread so if there
			//is an exception, their code is in the stack trace pointing the developer to the code lines to fix
			return renderView(processor, templatePath, pageArgs);
		}
		
		RenderImpl renderHtml = new RenderImpl(templatePath, pageArgs);
		return renderHtml;
	}

	/**
	 * Renders an html file with the same name as the methodName.html
	 * 
	 * @param pageArgs
	 * @return
	 */
	public static Render renderThis(Object ... pageArgs) {
		return renderView(null, pageArgs);
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
	
}
