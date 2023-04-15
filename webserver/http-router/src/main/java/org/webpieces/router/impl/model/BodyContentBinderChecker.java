package org.webpieces.router.impl.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.extensions.BodyContentBinder;

import com.google.inject.Injector;

@Singleton
public class BodyContentBinderChecker {

	private Map<Class, BodyContentBinder> bodyBinderPlugins;
	private List<String> allBinderAnnotations;

	public BodyContentBinder lookup(Class<? extends BodyContentBinder> binder) {
		return bodyBinderPlugins.get(binder);
	}

	public BodyContentBinder contentPreconditionCheck(Injector injector, Object meta, Method controllerMethod, Parameter[] parameters) {
		Set<String> binderMatches = new HashSet<>();
		Set<BodyContentBinder> matches = new HashSet<>();
		for(BodyContentBinder binder : bodyBinderPlugins.values()) {
			for(Parameter p : parameters) {
				Annotation[] annotations = p.getAnnotations();
				Class<?> entityClass = p.getType();
				recordParameterMatches(matches, binderMatches, binder, annotations, entityClass);
			}
			
			Annotation[] annotations = controllerMethod.getAnnotations();
			recordParameterMatches(matches, binderMatches, binder, annotations, null);
		}

		Class<?> returnType = controllerMethod.getReturnType();
		if(Action.class.isAssignableFrom(returnType))
			throw new IllegalArgumentException("The method for content route="+meta+" is returning Action and this is not allowed.  method="+controllerMethod);
		if(matches.size() == 0)
			throw new IllegalArgumentException("there was not a single method parameter annotated with a Plugin"
					+ " annotation on method="+controllerMethod+".  looking at your\n"
					+ "plugins, these are the annotations available="+allBinderAnnotations+"\n"
					+ "You either need one parameter with one of the annotations OR\n"
					+ "you need to annotata the method(if it is read only and no request is supplied) OR\n"
					+ "you can call addRoute(.........., YourBodyContentBinder.class");
		else if(matches.size() > 1)
			throw new IllegalArgumentException("there is more than one parameter with a Plugin"
					+ " annotation on method="+controllerMethod+".  These\n"
					+ "are the ones we found(please delete one)="+binderMatches+"\n"
					+ "Also make sure one parameter OR the method has the annotation, not both");

		return matches.toArray(new BodyContentBinder[0])[0];
	}
	
	private void recordParameterMatches(Set<BodyContentBinder> matches, Set<String> binderMatches, BodyContentBinder binder, Annotation[] annotations, Class<?> entityClass) {
		if(binder.canTransform(entityClass)) {
			binderMatches.add("@"+binder.getAnnotation().getSimpleName());
			matches.add(binder);
		}
	}

	public void install(Set<BodyContentBinder> bodyBinders) {
		this.bodyBinderPlugins = new HashMap<>();
		this.allBinderAnnotations = new ArrayList<>();
		for(BodyContentBinder binder : bodyBinders) {
			allBinderAnnotations.add("@"+binder.getAnnotation().getSimpleName());
			if(this.bodyBinderPlugins.get(binder.getClass()) != null) {
				throw new IllegalStateException("Binder class being installed twice not allowed="+binder.getClass());
			}
			this.bodyBinderPlugins.put(binder.getClass(), binder);
		}
	}
}
