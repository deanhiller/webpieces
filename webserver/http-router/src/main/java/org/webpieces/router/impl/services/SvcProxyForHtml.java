package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.executable.ExecutableValidator;

import org.webpieces.ctx.api.Flash;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Validation;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.BadClientRequestException;
import org.webpieces.router.api.exceptions.Violation;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.model.SvcProxyLogic;
import org.webpieces.router.impl.params.BeanValidator;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

public class SvcProxyForHtml implements Service<MethodMeta, Action> {

	private final ParamToObjectTranslatorImpl translator;
	private final RouterConfig config;
	private final ControllerInvoker invoker;
	private FutureHelper futureUtil;
	private BeanValidator validator;
	
	public SvcProxyForHtml(SvcProxyLogic svcProxyLogic, FutureHelper futureUtil) {
		this.futureUtil = futureUtil;
		this.translator = svcProxyLogic.getTranslator();
		this.config = svcProxyLogic.getConfig();
		this.invoker = svcProxyLogic.getServiceInvoker();
		this.validator = svcProxyLogic.getValidator();
	}

	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		return futureUtil.syncToAsyncException(() -> invokeMethod(meta));
	}

	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		RouteInfoForHtml info = (RouteInfoForHtml) meta.getRoute();
		
		tokenCheck(info, meta.getCtx());
		
		Method m = meta.getLoadedController().getControllerMethod();
		Object obj = meta.getLoadedController().getControllerInstance();
		Validation validation = meta.getCtx().getValidation();
		
		//We chose to do this here so any filters ESPECIALLY API filters 
		//can catch and translate api errors and send customers a logical response
		//On top of that ORM plugins can have a transaction filter and then in this
		//createArgs can look up the bean before applying values since it is in
		//the transaction filter
		CompletableFuture<List<Object>> future = translator.createArgs(m, meta.getCtx(), null)
													.thenApply ( args -> validate(obj, m, meta.getCtx(), args));
		
		return future.thenCompose(argsResult -> doTheInvoke(meta, argsResult));
	}

	/**
	 * This validation covers THREE cases I know of
	 *  controller method public Action postHibernateEntity(CustomerDbo customer) //hibernate plugin validation AFTER looking up in DB
	 *  controller method public Action postDto(@Dto SomeDto dto) //dto plugin validation AFTER looking up from remote service
	 *  controller method public Action postSomething(@NotBlank String username) //simple validation
	 *  
	 *  JSON or other content validation is done in SvcProxyForContent.java and throws BadClientRequestException so that translators
	 *  can translate to generic error message sent to clients
	 */
	private List<Object> validate(Object controller, Method m, RequestContext requestContext, List<Object> args) {
		if(requestContext.getRequest().method != HttpMethod.POST)
			return args; //ONLY validate on post requests
		
		Validation validation = requestContext.getValidation();
		
		List<Violation> violations = validator.validate(controller, m, args);
		
		//Since this is web, just add errors to Validation object
		for(Violation violation : violations) {
			//add error to list of errors for form to display
			validation.addError(violation.getPath(), violation.getMessage());
		}
		
		return args;
	}
	
	private CompletableFuture<Action> doTheInvoke(MethodMeta meta, List<Object> argsResult) {
		try {
			return invoker.invokeAndCoerce(meta.getLoadedController(), argsResult.toArray()).thenApply( action -> {
				if(config.isValidateFlash()) {
					validateKeepFlagSet(action, meta.getCtx(), meta.getLoadedController());
				}
				return action;
			});
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof WebpiecesException)
				throw (WebpiecesException)e.getCause();
			throw SneakyThrow.sneak(e);
		} catch (IllegalAccessException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	/**
	 * This has to be above LoginFilter so LoginFilter can flash the multiPartParams so edits exist through
	 * a login!!
	 * 
	 */
	private void tokenCheck(RouteInfoForHtml info, RequestContext ctx) {
		RouterRequest req = ctx.getRequest();

		if(req.multiPartFields.size() == 0)
			return;

		if(config.isTokenCheckOn() && info.isCheckSecureToken()) {
			String token = ctx.getSession().get(SessionImpl.SECURE_TOKEN_KEY);
			List<String> formToken = req.multiPartFields.get(RequestContext.SECURE_TOKEN_FORM_NAME);
			if(formToken == null)
				throw new BadClientRequestException("missing form token(or route added without setting checkToken variable to false)"
						+ "...someone posting form without getting it first(hacker or otherwise) OR "
						+ "you are not using the #{form}# tag or the #{secureToken}# tag to secure your forms");
			else if(formToken.size() == 0) {
				throw new BadClientRequestException("missing form token(or route added without setting checkToken variable to false)"
						+ "...someone posting form without getting it first(hacker or otherwise) OR "
						+ "you are not using the #{form}# tag or the #{secureToken}# tag to secure your forms");				
			} else if(formToken.size() > 1) {
				throw new BadClientRequestException("Somehow, there are two values for key="+RequestContext.SECURE_TOKEN_FORM_NAME+". This name is reserved.  perhaps your app is using it?");
			}
			
			String formPostedToken = formToken.get(0);
			if(token == null) {
				throw new BadClientRequestException("Somehow, the cookie is missing the secure token.  key="+SessionImpl.SECURE_TOKEN_KEY+"."
						+ " This token is set in the session when rendering form tags in FormTag.java when calling Current.session().getOrCreateSecureToken();. form token="+formPostedToken);
			} else if(!token.equals(formPostedToken))
				throw new BadClientRequestException("bad form token...someone posting form with invalid token(hacker or otherwise)");
		}
	}
	
	private void validateKeepFlagSet(Action action, RequestContext requestContext, LoadedController loadedController) {
		if(requestContext.getRequest().method != HttpMethod.POST)
			return; //no validation needed
		
		Flash flash = requestContext.getFlash();
		Validation validation = requestContext.getValidation();
		
		//This is for developers as it forces them to call flash.keep() or flash.noKeep() so that they don't forget on
		//forms.  This is only for incoming POST.  On redirect, call flash.keep() and on render call flash.noKeep() in general.
		if(!flash.isKeepFlagSet()) {
			throw new IllegalStateException("In your controller, you did not call flash.keep() or flash.noKeep().  "
					+ "You must do one or the other(usually call keep() on redirects to flash user inpput so user doesn't lose his input)."
					+ "\nOffending controller="+loadedController.getControllerMethod()
					+ "\nResponse Action(your controller returned)="+action);
		} else if(!validation.isKeepFlagSet()) {
			throw new IllegalStateException("In your controller, you did not call validation.keep() or validation.noKeep().  "
					+ "You must do one or the other(usually call keep() on redirects to flash user inpput so user doesn't lose his input)"
					+ "\nOffending controller="+loadedController.getControllerMethod()
					+ "\nResponse Action(your controller returned)="+action);
		}
	}	
}
