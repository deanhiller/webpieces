package org.webpieces.router.impl;

import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.*;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.Startable;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.router.impl.ctx.FlashImpl;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.ctx.ValidationImpl;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.util.cmdline2.Arguments;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.webpieces.http2.api.streaming.StreamWriter;

@ImplementedBy(ProdRouterService.class)
public abstract class AbstractRouterService {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractRouterService.class);
	private RouteLoader routeLoader;
	private ObjectTranslator translator;
	private CookieTranslator cookieTranslator;
	private WebInjector webInjector;
	
	public AbstractRouterService(
			WebInjector webInjector,
			RouteLoader routeLoader,
			CookieTranslator cookieTranslator,
			ObjectTranslator translator
	) {
		this.webInjector = webInjector;
		this.routeLoader = routeLoader;
		this.cookieTranslator = cookieTranslator;
		this.translator = translator;
	}

	public RouterStreamRef incomingRequest(RouterRequest routerRequest, ProxyStreamHandle handler) {
		try {
			Session session = (Session) cookieTranslator.translateCookieToScope(routerRequest, new SessionImpl(translator));
			FlashSub flash = (FlashSub) cookieTranslator.translateCookieToScope(routerRequest, new FlashImpl(translator));
			Validation validation = (Validation) cookieTranslator.translateCookieToScope(routerRequest, new ValidationImpl(translator));
			ApplicationContext ctx = webInjector.getAppContext();
			RequestContext requestCtx = new RequestContext(validation, flash, session, routerRequest, ctx);

			String user = session.get("userId");
			MDC.put("userId", user);

			//TODO(dhiller): This is request heaaders choke point but need ot also perhaps setup streaming choke point
			//here as well
			Current.setContext(requestCtx);
			try {
				return incomingRequestImpl(requestCtx, handler);
			} finally {
				Current.removeContext();
				MDC.remove("userId");
			}

		} catch(BadCookieException e) {
			//CHEAT: we know this is syncrhonous exception from the translateCookieToScope
			log.warn("This occurs if secret key changed, or you booted another webapp with different key on same port or someone modified the cookie", e);
			XFuture<StreamWriter> writer = handler.sendRedirectAndClearCookie(routerRequest, e.getCookieName());
			return new RouterStreamRef("cookieFailed", writer, null);
		}
	}
	
	protected abstract RouterStreamRef incomingRequestImpl(RequestContext req, ProxyStreamHandle handler);
	
	public String convertToUrl(String routeId, Map<String, Object> args, boolean isValidating) {
		return routeLoader.convertToUrl(routeId, args, isValidating);
	}
	
	public FileMeta relativeUrlToHash(String urlPath) {
		if(!urlPath.startsWith("/"))
			urlPath = "/"+urlPath;
		
		return routeLoader.relativeUrlToHash(urlPath);
	}
	
	protected void runStartupHooks(Injector injector) {
		log.info("Running startup hooks for server");
		
		Key<Set<Startable>> key = Key.get(new TypeLiteral<Set<Startable>>(){});
		Set<Startable> startupHooks = injector.getInstance(key);
		for(Startable s : startupHooks) {
			runStartupHook(s);
		}
		log.info("Ran all startup hooks");
	}

	private void runStartupHook(Startable s) {
		try {
			log.info("starting startup hook="+s.getClass().getSimpleName());
			s.start();
			log.info("Successfully ran startup hook="+s.getClass().getSimpleName());
		} catch(Throwable e) {
			throw new RuntimeException("Startup hook="+s.getClass().getSimpleName()+" failed", e);
		}
	}
	
	public <T> ObjectStringConverter<T> getConverterFor(T bean) {
		return translator.getConverterFor(bean);
	}

	protected abstract void configure(Arguments arguments);

	protected abstract Injector start();

}
