package org.webpieces.router.impl.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.router.api.BodyContentBinder;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.RenderContent;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.exceptions.BadRequestException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.body.BodyParser;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class ServiceProxy implements Service<MethodMeta, Action> {

	private final static Logger log = LoggerFactory.getLogger(ServiceProxy.class);
	private ParamToObjectTranslatorImpl translator;
	private BodyParsers requestBodyParsers;
	private RouterConfig config;
	
	public ServiceProxy(ParamToObjectTranslatorImpl translator, BodyParsers requestBodyParsers, RouterConfig config) {
		this.translator = translator;
		this.requestBodyParsers = requestBodyParsers;
		this.config = config;
	}
	
	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		try {
			return invokeMethod(meta);
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof NotFoundException) {
				return createNotFound((NotFoundException) cause);
			}
			return createRuntimeFuture(cause);
		} catch(NotFoundException e) {
			return createNotFound(e);
		} catch(Throwable e) {
			return createRuntimeFuture(e);
		}			
	}

	private CompletableFuture<Action> createRuntimeFuture(Throwable e) {
		CompletableFuture<Action> future = new CompletableFuture<Action>();
		future.completeExceptionally(e);
		return future;
	}

	private CompletableFuture<Action> createNotFound(NotFoundException e) {
		CompletableFuture<Action> future = new CompletableFuture<Action>();
		future.completeExceptionally(e);
		return future;
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		RouterRequest req = meta.getCtx().getRequest();
		
		log.info("Incoming content length was specified, but no contentType was(We will not parse the body).  req="+req);

		parseBodyFromContentType(meta.getRoute(), meta.getCtx(), meta.getBodyContentBinder());

		Method m = meta.getMethod();
		Object obj = meta.getControllerInstance();
		
		//We chose to do this here so any filters ESPECIALLY API filters 
		//can catch and translate api errors and send customers a logical response
		//On top of that ORM plugins can have a transaction filter and then in this
		//createArgs can look up the bean before applying values since it is in
		//the transaction filter
		List<Object> argsResult = translator.createArgs(m, meta.getCtx(), meta.getBodyContentBinder());
		
		Object retVal = m.invoke(obj, argsResult.toArray());
		
		if(meta.getBodyContentBinder() != null)
			return unwrapResult(m, retVal, meta.getBodyContentBinder());

		if(retVal == null)
			throw new IllegalStateException("Your controller method returned null which is not allowed.  offending method="+m);
		
		if(retVal instanceof CompletableFuture) {
			return (CompletableFuture<Action>) retVal;
		} else {
			Action action = (Action) retVal;
			return CompletableFuture.completedFuture(action);
		}
	}

	private void parseBodyFromContentType(Route route, RequestContext ctx, BodyContentBinder bodyContentBinder) {
		RouterRequest req = ctx.getRequest();
		if(bodyContentBinder != null)
			return; //A route that defines the content gets to override the content type header so just return
		if(req.contentLengthHeaderValue == null)
			return;
		
		if(req.contentTypeHeaderValue == null) {
			log.info("Incoming content length was specified, but no contentType was(We will not parse the body).  req="+req);
			return;
		}
		
		BodyParser parser = requestBodyParsers.lookup(req.contentTypeHeaderValue);
		if(parser == null) {
			log.error("Incoming content length was specified but content type was not 'application/x-www-form-urlencoded'(We will not parse body).  req="+req);
			return;
		}

		DataWrapper body = req.body;
		Charset encoding = config.getDefaultFormAcceptEncoding();
		parser.parse(body, req, encoding);
		
		if(config.isTokenCheckOn() && route.isCheckSecureToken()) {
			String token = ctx.getSession().get(SessionImpl.SECURE_TOKEN_KEY);
			List<String> formToken = req.multiPartFields.get(RequestContext.SECURE_TOKEN_FORM_NAME);
			if(formToken == null)
				throw new BadRequestException("missing form token(or route added without setting checkToken variable to false)"
						+ "...someone posting form without getting it first(hacker or otherwise) OR "
						+ "you are not using the #{form}# tag or the #{secureToken}# tag to secure your forms");
			else if(!token.equals(formToken.get(0)))
				throw new BadRequestException("bad form token...someone posting form with invalid token(hacker or otherwise)");
		}
	}

	@SuppressWarnings("unchecked")
	private CompletableFuture<Action> unwrapResult(Method method, Object retVal, BodyContentBinder binder) {
		Class<?> returnType = method.getReturnType();
		
		
		if(CompletableFuture.class.isAssignableFrom(returnType)) {
			if(retVal == null)
				throw new IllegalStateException("Your method returned a null CompletableFuture which it not allowed.  method="+method);
			CompletableFuture<Object> future = (CompletableFuture<Object>) retVal;
			return future.thenApply((bean) -> binder.marshal(bean));
		} else {
			RenderContent content = binder.marshal(retVal);
			return CompletableFuture.completedFuture(content);
		}
	}
}
