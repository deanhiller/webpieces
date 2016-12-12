package org.webpieces.router.api.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.routing.RouteId;
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

	public static Redirect redirectFlashAll2(RouteId routeId, RequestContext ctx, Object ... args) {
		ctx.moveFormParamsToFlash(new HashSet<>());
		ctx.getFlash().keep();
		ctx.getValidation().keep();
		return redirect(routeId, args);
	}
	
	public static Redirect redirectToUrl(String url) {
		RawRedirect redirect = new RawRedirect(url);
		return redirect;
	}

	public static void redirectFlashAllAddEdit(RouteId addRoute, RouteId editRoute,
			RequestContext context, String idKey, Integer idValue, Object ... args) {
		if(idValue == null) {
			redirectFlashAll2(addRoute, context, args);
		} else {
			Object[] allArgs = new Object[args.length+2];
			allArgs[0] = idKey;
			allArgs[1] = idValue;
			System.arraycopy(args, 0, allArgs, 2, args.length);
			
			redirectFlashAll2(editRoute, context, allArgs);
		}
	}

}
