package org.webpieces.router.api.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;

public class Actions {
	
	/**
	 * Renders the html file at templatePath which is relative to the controller or an 
	 * absolute reference from the start of the classpath
	 * 
	 * @param templatePath
	 * @param pageArgs
	 * @return
	 */
	public static Render renderView(String templatePath, Object ... pageArgs) {		
		RenderImpl renderHtml = new RenderImpl(templatePath, pageArgs);
		return renderHtml;
	}

	/**
	 * Renders an html file with the same name as the methodName.html that is being invoked on the controller
	 * but beware when called from filter, it still refers to the same html file.  Filters should generally
	 * be redirecting anyways not rendering.
	 * 
	 * @param pageArgs
	 * @return
	 */
	public static Render renderThis(Object ... pageArgs) {
		return renderView(null, pageArgs);
	}

	public static Redirect redirect(RouteId routeId, Object ... args) {
		RedirectImpl redirect = new RedirectImpl(routeId, args);
		return redirect;
	}
	
	public static AjaxRedirect ajaxRedirect(RouteId routeId, Object ... args) {
		AjaxRedirectImpl redirect = new AjaxRedirectImpl(routeId, args);
		return redirect;
	}

	/**
	 * 
	 * @param routeId
	 * @param ctx
	 * @param secureFieldNames Fields that are secure should be listed so we don't transfer them to flash cookie as they
	 * are visible then in the browser for some amount of time.
	 * 
	 * @return
	 */
	public static Redirect redirectFlashAllSecure(RouteId routeId, RequestContext ctx, String ... secureFieldNames) {
		Set<String> mySet = new HashSet<>(Arrays.asList(secureFieldNames));
		ctx.moveFormParamsToFlash(mySet);
		ctx.getFlash().keep();
		ctx.getValidation().keep();
		return redirect(routeId);
	}
	
	public static Redirect redirectFlashAllSecure(RouteId routeId, RequestContext ctx, Object[] args, String ... secureFieldNames) {
		Set<String> mySet = new HashSet<>(Arrays.asList(secureFieldNames));
		ctx.moveFormParamsToFlash(mySet);
		ctx.getFlash().keep();
		ctx.getValidation().keep();
		return redirect(routeId, args);
	}
	
	public static Redirect redirectToUrl(String url) {
		RawRedirect redirect = new RawRedirect(url);
		return redirect;
	}

	public static Redirect redirectFlashAll(RouteId addRoute, RouteId editRoute, FlashAndRedirect redirect) {
		RequestContext ctx = redirect.getContext();
		ctx.getFlash().setMessage(redirect.getGlobalMessage());

		if(redirect.getIdValue() == null) {
			//If id is null, this is an add(not an edit) so redirect back to add route
			return redirectFlashAllSecure(addRoute, ctx, redirect.getPageArgs(), redirect.getSecureFields());
		} else {
			//If id is not null, this is an edit(not an add) so redirect back to edit route
			String[] args = redirect.getPageArgs();
			Object[] allArgs = new Object[args.length+2];
			allArgs[0] = redirect.getIdField();
			allArgs[1] = redirect.getIdValue();
			System.arraycopy(args, 0, allArgs, 2, args.length);
			
			return redirectFlashAllSecure(editRoute, ctx, allArgs, redirect.getSecureFields());
		}
	}

}
