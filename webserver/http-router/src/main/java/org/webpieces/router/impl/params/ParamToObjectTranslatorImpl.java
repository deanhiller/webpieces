package org.webpieces.router.impl.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Validation;
import org.webpieces.http.exception.BadRequestException;
import org.webpieces.router.api.exceptions.DataMismatchException;
import org.webpieces.router.api.exceptions.IllegalArgException;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.extensions.EntityLookup;
import org.webpieces.router.api.extensions.Meta;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.ParamMeta;
import org.webpieces.util.exceptions.SneakyThrow;

@Singleton
public class ParamToObjectTranslatorImpl {

	//private static final Logger log = LoggerFactory.getLogger(ArgumentTranslator.class);
	private ParamValueTreeCreator treeCreator;
	private ObjectTranslator objectTranslator;
	private Set<EntityLookup> lookupHooks;

	@Inject
	public ParamToObjectTranslatorImpl(ParamValueTreeCreator treeCreator, ObjectTranslator primitiveConverter) {
		this.treeCreator = treeCreator;
		this.objectTranslator = primitiveConverter;
	}
	
	//ok, here are a few different scenarios to consider
	//   1. /user/{var1}/{var2}/{var3}             Controller.method() and controller accesses RequestLocal.getRequest().getParams().get("var1");
	//   2. /user/{var1}/{var2}/{var3}             Controller.method(var1, var2, var3)
	//   3. /user/{var1}?var2=xx&var3=yyy&cat=dog  Controller.method(var1) and controller accesses RequestLocal.getRequest().getParams().get("var2");
	//   4. /user/{var1}?var2=xx                   Controller.method(var2) and controller accesses RequestLocal.getRequest().getParams().get("var1");
    //   5. /user?var1=xxx&var1=yyy                Controller.method({xxx, yyy}) as an array
	//   6. /user/{var1}/{var1}/{var1}             We don't allow this and last one wins if they are different since outgoing they all have to be the same
	//
	//ON TOP of this, do you maintain a separate structure for params IN THE PATH /user/{var1} vs in the query params /user/{var1}?var1=xxx
	//
	//AND ON TOP of that, we have multi-part fields as well with keys and values
	public CompletableFuture<List<Object>> createArgs(Method m, RequestContext ctx, BodyContentBinder binder) {
		RouterRequest req = ctx.getRequest();
		try {
			return createArgsImpl(m, ctx, binder);
		} catch(DataMismatchException e) {
			if(req.method == HttpMethod.GET) {
				//For GET with query params or path urls, if we can't convert, it should be a 404...
				//This is because a human user typed in the wrong url so they should get back not found
				throw new NotFoundException(e);
			} else {
				//For POST with multipart, this should be a 500 because a human user does NOT type in post
				//urls and instead the developer typed in the wrong url and an issue needs to be fixed(or 
				//some hacker is doing something so internal error there is fine as well)
				if(req.multiPartFields.size() > 0)
					throw new IllegalArgException(e);
				else //for apis that POST, this is a client error(or developer error when testing)
					throw new BadRequestException(e);
			}
		}
	}

	protected CompletableFuture<List<Object>> createArgsImpl(Method method, RequestContext ctx, BodyContentBinder binder) {
		RouterRequest req = ctx.getRequest();
		Parameter[] paramMetas = method.getParameters();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		
		ParamTreeNode paramTree = new ParamTreeNode();
		
		//For multipart AND for query params such as ?var=xxx&var=yyy&var2=xxx AND for url path params /mypath/{var1}/account/{id}
		
		//query params first
		Map<String, String> queryParams = translate(req.queryParams);
		treeCreator.createTree(paramTree, queryParams, FromEnum.QUERY_PARAM);
		//next multi-part params
		Map<String, String> multiPartParams = translate(req.multiPartFields);		
		treeCreator.createTree(paramTree, multiPartParams, FromEnum.FORM_MULTIPART);
		//lastly path params
		treeCreator.createTree(paramTree, ctx.getPathParams(), FromEnum.URL_PATH);

		List<Object> results = new ArrayList<>();
		CompletableFuture<List<Object>> future = CompletableFuture.completedFuture(results);
		for(int i = 0; i < paramMetas.length; i++) {
			Parameter paramMeta = paramMetas[i];
			Annotation[] annotations = paramAnnotations[i];
			ParamMeta fieldMeta = new ParamMeta(method, paramMeta, annotations);
			String name = fieldMeta.getName();
			ParamNode paramNode = paramTree.get(name);
			CompletableFuture<Object> beanFuture;
			if(binder != null && isManagedBy(binder, fieldMeta)) {
				Object bean = binder.unmarshal(ctx, fieldMeta, req.body.createByteArray());
				beanFuture = CompletableFuture.completedFuture(bean);
			} else {
				beanFuture = translate(req, method, paramNode, fieldMeta, ctx.getValidation());
			}
			
			future = future.thenCompose( list -> {
				return beanFuture.thenApply( bean -> {
					list.add(bean);
					return list;
				});
			});
			
		}
		return future;
	}

	private boolean isManagedBy(BodyContentBinder binder, ParamMeta fieldMeta) {
		Class<?> fieldClass = fieldMeta.getFieldClass();
		Annotation[] annotations = fieldMeta.getAnnotations();
		for(Annotation anno : annotations) {
			if(binder.isManaged(fieldClass, anno.annotationType()))
				return true;
		}
		return false;
	}

	private Map<String, String> translate(Map<String, List<String>> queryParams) {
		Map<String, String> newForm = new HashMap<>();
		for(Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			if(value.size() == 1) {
				newForm.put(key, value.get(0));
			} else {
				for(int i = 0; i < value.size(); i++) {
					//put in proper form such that invoking PropertyUtils works...
					String newKey = key+"["+i+"]";
					newForm.put(newKey, value.get(i));
				}
			}
		}
		return newForm;
	}

	private CompletableFuture<Object> translate(RouterRequest req, Method method, ParamNode valuesToUse, Meta fieldMeta, Validation validator) {

		Class<?> fieldClass = fieldMeta.getFieldClass();
		ObjectStringConverter<?> converter = objectTranslator.getConverter(fieldClass);
		if(converter != null) {
			Object convert = convert(req, method, valuesToUse, fieldMeta, converter, validator);
			return CompletableFuture.completedFuture(convert);
		} else if(fieldClass.isArray()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it="+fieldMeta);
		} else if(fieldClass.isEnum()) {
			throw new UnsupportedOperationException("You need to install a "+ObjectStringConverter.class.getSimpleName()+" for this enum "+fieldClass+" meta class="+fieldMeta.getFieldClass()+". This\n" +
					"can be done like so in one of your guice modules....\n" +
					"\t\tMultibinder<ObjectStringConverter> conversionBinder = Multibinder.newSetBinder(binder, ObjectStringConverter.class);\n" +
					"\t\tconversionBinder.addBinding().to({YOUR_ENUM}.WebConverter.class);");
		} else if(List.class.isAssignableFrom(fieldClass)) {
			if(valuesToUse == null)
				return CompletableFuture.completedFuture(new ArrayList<>());
			else if(valuesToUse instanceof ArrayNode) {
				List<ParamNode> paramNodes = ((ArrayNode) valuesToUse).getList();
				return createList(req, method, fieldMeta, validator, paramNodes);
			} else if(valuesToUse instanceof ValueNode) {
				List<ParamNode> paramNodes = new ArrayList<>();
				paramNodes.add(valuesToUse);
				return createList(req, method, fieldMeta, validator, paramNodes);
			}	
			throw new IllegalArgumentException("Found List on field or param="+fieldMeta+" but did not find ArrayNode type");
		} else if(valuesToUse instanceof ArrayNode) {
			throw new IllegalArgumentException("Incoming array need a type List but instead found type="+fieldClass+" on field="+fieldMeta);
		} else if(valuesToUse instanceof ValueNode) {
			ValueNode v = (ValueNode) valuesToUse;
			String fullName = v.getFullName();
			throw new IllegalArgumentException("Could not convert incoming value="+v.getValue()+" of key name="+fullName+" field="+fieldMeta);
		} else if(valuesToUse == null) {
			fieldMeta.validateNullValue(); //validate if null is ok or not
			return CompletableFuture.completedFuture(null);
		} else if(!(valuesToUse instanceof ParamTreeNode)) {
			throw new IllegalStateException("Bug, must be missing a case. v="+valuesToUse+" type to field="+fieldMeta);
		}

		ParamTreeNode tree = (ParamTreeNode) valuesToUse;
		EntityLookup pluginLookup = fetchPluginLoader(fieldClass);
		
		CompletableFuture<Object> future = null;
		if(pluginLookup != null) {
			future = pluginLookup.find(fieldMeta, tree, c -> createBean(c));
			if(future == null)
				throw new IllegalStateException("plugin="+pluginLookup.getClass()+" failed to create bean.  This is a plugin bug");
		} else {
			Object newBean = createBean(fieldClass);
			future = CompletableFuture.completedFuture(newBean);
		}
		
		return future.thenCompose( bean -> {
			return fillBeanIn(bean, tree, req, method, validator);
		});
	}

	private CompletableFuture<Object> fillBeanIn(Object bean, ParamTreeNode tree, RouterRequest req, Method method, Validation validator) {
		CompletableFuture<Object> future = CompletableFuture.completedFuture(bean);
		for(Map.Entry<String, ParamNode> entry: tree.entrySet()) {
			String key = entry.getKey();
			ParamNode value = entry.getValue();
			Field field = findBeanFieldType(bean.getClass(), key, new ArrayList<>());
			
			FieldMeta nextFieldMeta = new FieldMeta(field);
			
			//Here, if there are any lookups(doubtful!!), they would all be done in parallel....
			CompletableFuture<Object> translated = translate(req, method, value, nextFieldMeta, validator);
			future = future.thenCompose( b -> translated)
							.thenApply( translatedValue -> {
								nextFieldMeta.setValueOnBean(bean, translatedValue);
								return bean;
							});
		}
		
		return future;
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Object> createList(RouterRequest req, Method method, Meta fieldMeta, Validation validator, List<ParamNode> paramNodes) {
		List<Object> list = new ArrayList<>();
		ParameterizedType type = (ParameterizedType) fieldMeta.getParameterizedType();
		Type[] actualTypeArguments = type.getActualTypeArguments();
		Type type2 = actualTypeArguments[0];
		@SuppressWarnings("rawtypes")
		GenericMeta genMeta = new GenericMeta((Class) type2);
		
		CompletableFuture<List<Object>> future = CompletableFuture.completedFuture(list);
		for(ParamNode node : paramNodes) {
			CompletableFuture<Object> fut;
			if(node != null) { 
				fut = translate(req, method, node, genMeta, validator);
			} else {
				fut = CompletableFuture.completedFuture(null);
			}
			
			future = future.thenCompose( myList -> {
				return fut.thenApply( b -> { 
					myList.add(b);
					return myList;
				});
			});
		}
		
		return future.thenApply( l -> (Object)l );
	}

	private Field findBeanFieldType(Class<?> beanType, String key, List<String> classList) {
		classList.add(beanType.getName());
		
		Field[] fields = beanType.getDeclaredFields();
		for(Field f : fields) {
			if(key.equals(f.getName())) {
				return f;
			}
		}
		
		Class<?> superclass = beanType.getSuperclass();
		if(superclass == null)
			throw new IllegalArgumentException("Private Field(not getter or setter!!) with name="+key+" not found in any of the classes="+classList);
		
		return findBeanFieldType(superclass, key, classList);
	}

	private <T> Object createBean(Class<T> paramTypeToCreate) {
		try {
			return paramTypeToCreate.getDeclaredConstructor().newInstance();
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private Object convert(RouterRequest req, Method method, ParamNode valuesToUse, Meta fieldMeta, ObjectStringConverter converter, Validation validator) {
		Class<?> paramTypeToCreate = fieldMeta.getFieldClass();
		if(fieldMeta instanceof ParamMeta) {
			//for params only not fields as with fields, we just don't set the field and skip it...before we call a method,
			//we MUST have a value to set
			if(Boolean.TYPE == paramTypeToCreate && valuesToUse == null) {
				//very special case
				//This is a cheat for checkboxes since null represents false on a boolean primitive
				//This does cause others that have a boolean param to be false if nothing is set
				return false;
			}
			checkForBadNullToPrimitiveConversion(req, valuesToUse, fieldMeta, method);
		}
		
		if(valuesToUse == null)
			return null;
		if(!(valuesToUse instanceof ValueNode))
			throw new IllegalArgumentException("method takes param type="+paramTypeToCreate+" but complex structure found");
		ValueNode node = (ValueNode) valuesToUse;
		String value = node.getValue();
		try {
			return converter.stringToObject(value);
		} catch(Exception e) {
			if(node.getFrom() == FromEnum.FORM_MULTIPART) {
				validator.addError(node.getFullKeyName(), "Could not convert value");
				return null;
			} else
				//This should be a 404 in production if the url is bad...
				throw new NotFoundException("The method='"+method+"' requires that the parameter or field '"+fieldMeta+"' be of type="
						+paramTypeToCreate+" but the request contained a value that could not be converted="+value);
		}
	}

	private void checkForBadNullToPrimitiveConversion(RouterRequest req, ParamNode valuesToUse, Meta fieldMeta,
			Method method) {
		Class<?> paramTypeToCreate2 = fieldMeta.getFieldClass();
		if(paramTypeToCreate2.isPrimitive()) {
			if(valuesToUse == null) {
				String s = "The method='"+method+"' requires that "+fieldMeta+" be of type="
						+paramTypeToCreate2+" but the request did not contain any value in query params, path "
						+ "params nor multi-part form fields with a value and we can't convert null to a primitive";
				throw new DataMismatchException(s);
			}
		}
	}

	private EntityLookup fetchPluginLoader(Class<?> paramTypeToCreate) {
		for(EntityLookup lookup : lookupHooks) {
			if(lookup.isManaged(paramTypeToCreate))
				return lookup;
		}
		return null;
	}

	public void install(Set<EntityLookup> lookupHooks) {
		this.lookupHooks = lookupHooks;
	}

}
