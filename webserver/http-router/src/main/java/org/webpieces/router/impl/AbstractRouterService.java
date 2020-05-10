package org.webpieces.router.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.FlashSub;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Session;
import org.webpieces.ctx.api.Validation;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.Startable;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.router.impl.ctx.FlashImpl;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.ctx.ValidationImpl;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.proxyout.ResponseOverrideSender;
import org.webpieces.router.impl.routeinvoker.WebSettings;
import org.webpieces.router.impl.routers.ExceptionWrap;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.futures.FutureHelper;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.webpieces.http2engine.api.StreamWriter;

public abstract class AbstractRouterService {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractRouterService.class);
	private RouteLoader routeLoader;
	private ObjectTranslator translator;
	private Provider<ResponseStreamer> proxyProvider;
	private WebSettings webSettings;
	private CookieTranslator cookieTranslator;
	private WebInjector webInjector;
	private FailureResponder failureResponder;
	private FutureHelper futureUtil;
	
	public AbstractRouterService(
			FailureResponder failureResponder,
			FutureHelper futureUtil,
			WebInjector webInjector,
			RouteLoader routeLoader,
			CookieTranslator cookieTranslator,
			ObjectTranslator translator,
			Provider<ResponseStreamer> proxyProvider,
			WebSettings webSettings
	) {
		this.failureResponder = failureResponder;
		this.futureUtil = futureUtil;
		this.webInjector = webInjector;
		this.routeLoader = routeLoader;
		this.cookieTranslator = cookieTranslator;
		this.translator = translator;
		this.proxyProvider = proxyProvider;
		this.webSettings = webSettings;
	}

	public CompletableFuture<StreamWriter> incomingRequest(RouterRequest routerRequest, ProxyStreamHandle handler) {
		try {
			Session session = (Session) cookieTranslator.translateCookieToScope(routerRequest, new SessionImpl(translator));
			FlashSub flash = (FlashSub) cookieTranslator.translateCookieToScope(routerRequest, new FlashImpl(translator));
			Validation validation = (Validation) cookieTranslator.translateCookieToScope(routerRequest, new ValidationImpl(translator));
			ApplicationContext ctx = webInjector.getAppContext(); 
			RequestContext requestCtx = new RequestContext(validation, flash, session, routerRequest, ctx);
			
			String user = session.get("userId");
			MDC.put("userId", user);
			return processRequest(requestCtx, handler);

		} catch(BadCookieException e) {
			log.warn("This occurs if secret key changed, or you booted another webapp with different key on same port or someone modified the cookie", e);
			return failureResponder.sendRedirectAndClearCookie(new ResponseOverrideSender(handler), routerRequest, e.getCookieName());
		}
	}

	private CompletableFuture<StreamWriter> processRequest(RequestContext requestCtx, ProxyStreamHandle handler) {
		ResponseStreamer proxy = proxyProvider.get();
		proxy.init(requestCtx.getRequest(), handler, webSettings.getMaxBodySizeToSend());
		return futureUtil.catchBlockWrap(
				() -> incomingRequestImpl(requestCtx, handler),
				(t) -> finalFailure(handler, t, requestCtx, proxy)
		);
	}

	public Throwable finalFailure(ProxyStreamHandle handler, Throwable e, RequestContext requestCtx, ResponseStreamer proxy) {
		if(ExceptionWrap.isChannelClosed(e))
			return e;

		log.error("This is a final(secondary failure) trying to render the Internal Server Error Route", e);

		CompletableFuture<Void> future = futureUtil.syncToAsyncException(
				() -> failureResponder.failureRenderingInternalServerErrorPage(requestCtx, e, proxy)
		);
		
		future.exceptionally((t) -> {
			log.error("Webpieces failed at rendering it's internal error page since webapps internal erorr app page failed", t);
			return null;
		});
		return e;
	}
	
	protected abstract CompletableFuture<StreamWriter> incomingRequestImpl(RequestContext req, ProxyStreamHandle handler);
	
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
