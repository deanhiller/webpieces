package org.webpieces.plugin.dto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.extensions.EntityLookup;
import org.webpieces.router.api.extensions.Meta;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.ParamMeta;
import org.webpieces.router.impl.WebInjector;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.params.ParamNode;
import org.webpieces.router.impl.params.ParamTreeNode;
import org.webpieces.router.impl.params.ValueNode;

public class DtoLookup implements EntityLookup {

	private static final Logger log = LoggerFactory.getLogger(DtoLookup.class);
	private ObjectTranslator translator;
	private WebInjector webInjector;
	
	@Inject
	public DtoLookup(ObjectTranslator translator, WebInjector webInjector) {
		this.translator = translator;
		this.webInjector = webInjector;
	}
	
	@Override
	public <T> boolean isManaged(Class<T> paramTypeToCreate) {
		Dto annotation = paramTypeToCreate.getAnnotation(Dto.class);
		return annotation != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> XFuture<T> find(Meta paramMeta, ParamTreeNode tree, Function<Class<T>, T> beanCreate) {
		if(!(paramMeta instanceof ParamMeta))
			throw new UnsupportedOperationException("this plugin does not support type="+paramMeta.getClass());
		
		ParamMeta m = (ParamMeta) paramMeta;
		Class<T> paramTypeToCreate = (Class<T>) m.getFieldClass();
		Dto annotation = paramTypeToCreate.getAnnotation(Dto.class);
		
//		EntityManager entityManager = Em.get();
//		Metamodel metamodel = entityManager.getMetamodel();
//		ManagedType<T> managedType = metamodel.managedType(paramTypeToCreate);
//		IdentifiableType<T> entityType = (IdentifiableType<T>) managedType;
		Method method;
		try {
			method = paramTypeToCreate.getMethod("getId");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Class="+paramTypeToCreate.getName()+" has an annotation @Dto and this requires that dto to have a method getId, but we could not find that method", e);
		}
		Class<?> idClazz = method.getReturnType();
//		SingularAttribute<? super T, ?> idAttribute = entityType.getId(idClazz);
//		String name = idAttribute.getName();
		
		String name = "id";
		ParamNode paramNode = tree.get(name);
		
		String value = null;
		if(paramNode != null) {
			if(!(paramNode instanceof ValueNode))
				throw new IllegalStateException("The id field in the DTO should have matched to a "
						+ "ValueNode on incoming data and did not. node="+paramNode+".  bad multipart form?  (Please "
						+ "let us know so we can pair with you on this and I can add better error messaging)");
			ValueNode node = (ValueNode) paramNode;
			value = node.getValue();
		}
		
		if(value == null) {
			T theBean = beanCreate.apply(paramTypeToCreate);
			return XFuture.completedFuture(theBean);
		}
		
		@SuppressWarnings("rawtypes")
		ObjectStringConverter unmarshaller = translator.getConverter(idClazz);
		Object id = unmarshaller.stringToObject(value);

		Class lookupClass = annotation.lookupClass();
		String function = annotation.function();
		
		Object instance = webInjector.getCurrentInjector().getInstance(lookupClass);
		
		Method lookupMethod;
		try {
			lookupMethod = lookupClass.getMethod(function, idClazz);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Your function in @Dto='XFuture<"+paramTypeToCreate+"> "+function+"("+idClazz.getName()+")' on class="
						+lookupClass.getName()+" cannot be found.  We also did not find method='"+paramTypeToCreate+" "+function+"("+idClazz.getName()+")'");
		}
		
		Object result;
		try {
			result = lookupMethod.invoke(instance, id);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Exception invoking lookup method", e);
		}
		
		if(result instanceof XFuture) {
			return (XFuture<T>)result;
		}
		
		return (XFuture<T>) XFuture.completedFuture(result);
	}


}
