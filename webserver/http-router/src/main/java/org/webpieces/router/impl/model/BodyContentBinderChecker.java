package org.webpieces.router.impl.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.hooks.ControllerInfo;

import com.google.inject.Injector;

@Singleton
public class BodyContentBinderChecker {

	private Set<BodyContentBinder> bodyBinderPlugins;
	private List<String> allBinderAnnotations;
	
	public BodyContentBinder contentPreconditionCheck(Injector injector, Object meta, Method controllerMethod, Parameter[] parameters) {
		List<String> binderMatches = new ArrayList<>();
		AtomicReference<BodyContentBinder> lastMatch = new AtomicReference<BodyContentBinder>(null);
		for(BodyContentBinder binder : bodyBinderPlugins) {
			for(Parameter p : parameters) {
				Annotation[] annotations = p.getAnnotations();
				Class<?> entityClass = p.getType();
				recordParameterMatches(lastMatch, binderMatches, binder, annotations, entityClass);
			}
			
			Annotation[] annotations = controllerMethod.getAnnotations();
			recordParameterMatches(lastMatch, binderMatches, binder, annotations, null);
		}

		Class<?> returnType = controllerMethod.getReturnType();
		if(Action.class.isAssignableFrom(returnType))
			throw new IllegalArgumentException("The method for content route="+meta+" is returning Action and this is not allowed.  method="+controllerMethod);
		if(binderMatches.size() == 0)
			throw new IllegalArgumentException("there was not a single method parameter annotated with a Plugin"
					+ " annotation on method="+controllerMethod+".  looking at your\n"
					+ "plugins, these are the annotations available="+allBinderAnnotations+"\n"
					+ "You either need one parameter with one of the annotations OR\n"
					+ "you need to annotata the method(if it is read only and no request is supplied)");
		else if(binderMatches.size() > 1)
			throw new IllegalArgumentException("there is more than one parameter with a Plugin"
					+ " annotation on method="+controllerMethod+".  These\n"
					+ "are the ones we found(please delete one)="+binderMatches+"\n"		
					+ "Also make sure one parameter OR the method has the annotation, not both");

		
		
		return lastMatch.get();
	}
	
	private void recordParameterMatches(AtomicReference<BodyContentBinder> lastMatch, List<String> binderMatches, BodyContentBinder binder, Annotation[] annotations, Class<?> entityClass) {
		for(Annotation a : annotations) {
			if(binder.isManaged(entityClass, a.annotationType())) {
				binderMatches.add("@"+binder.getAnnotation().getSimpleName());
				lastMatch.set(binder);
			}
		}
	}
	
	public void install(Set<BodyContentBinder> bodyBinders) {
		this.bodyBinderPlugins = bodyBinders;
		this.allBinderAnnotations = new ArrayList<>();
		for(BodyContentBinder binder : bodyBinders) {
			allBinderAnnotations.add("@"+binder.getAnnotation().getSimpleName());
		}
	}
}
