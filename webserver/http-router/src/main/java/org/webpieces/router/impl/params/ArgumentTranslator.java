package org.webpieces.router.impl.params;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.Param;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.RouteMeta;

public class ArgumentTranslator {

	private static final Logger log = LoggerFactory.getLogger(ArgumentTranslator.class);
	private ParamValueTreeCreator treeCreator;
	private PrimitiveTranslator primitiveConverter;

	@Inject
	public ArgumentTranslator(ParamValueTreeCreator treeCreator, PrimitiveTranslator primitiveConverter) {
		this.treeCreator = treeCreator;
		this.primitiveConverter = primitiveConverter;
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
	
	public Object[] createArgs(MatchResult result, RouterRequest req, Validation validator) {
		RouteMeta meta = result.getMeta();
		Parameter[] paramMetas = meta.getMethod().getParameters();
		
		ParamTreeNode paramTree = new ParamTreeNode();
		
		//For multipart AND for query params such as ?var=xxx&var=yyy&var2=xxx AND for url path params /mypath/{var1}/account/{id}
		
		//query params first
		treeCreator.createTree(paramTree, req.queryParams, FromEnum.QUERY_PARAM);
		//next multi-part params
		treeCreator.createTree(paramTree, req.multiPartFields, FromEnum.FORM_MULTIPART);
		//lastly path params
		treeCreator.createTree(paramTree, createStruct(result.getPathParams()), FromEnum.URL_PATH);
		
		List<Object> args = new ArrayList<>();
		for(Parameter paramMeta : paramMetas) {
			Class<?> paramTypeToCreate = paramMeta.getType();
			Param annotation = paramMeta.getAnnotation(Param.class);
			String name = paramMeta.getName();
			if(annotation != null) {
				name = annotation.value();
			}
			
			ParamNode paramNode = paramTree.get(name);
			Object arg = translate(meta, paramNode, paramTypeToCreate, validator, name);
			args.add(arg);
		}
		return args.toArray();
	}

	private Object translate(RouteMeta meta, ParamNode valuesToUse, Class<?> paramTypeToCreate, Validation validator, String name) {

		Function<String, Object> converter = primitiveConverter.getConverter(paramTypeToCreate);
		if(converter != null) {
			return convert(meta, name, valuesToUse, paramTypeToCreate, converter, validator);
		} else if(paramTypeToCreate.isArray()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		} else if(paramTypeToCreate.isEnum()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		} else if(isPluginManaged(paramTypeToCreate)) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		} else if(valuesToUse instanceof ValueNode) {
			ValueNode v = (ValueNode) valuesToUse;
			String fullName = v.getFullName();
			throw new IllegalArgumentException("Could not convert incoming value="+v.getValue()+" of key name="+fullName);
		} else if(!(valuesToUse instanceof ParamTreeNode)) {
			throw new IllegalStateException("Bug, must be missing a case. v="+valuesToUse);
		}
		
		Object bean = createBean(paramTypeToCreate);
		ParamTreeNode tree = (ParamTreeNode) valuesToUse;
		for(Map.Entry<String, ParamNode> entry: tree.entrySet()) {
			String key = entry.getKey();
			ParamNode value = entry.getValue();
			Field field = findBeanFieldType(bean.getClass(), key, new ArrayList<>());
			BiFunction<Object, Object, Void> applyBeanValueFunction = createFunction(bean.getClass(), field);
			Class<?> fieldType = field.getType();
			Object translatedValue = translate(meta, value, fieldType, validator, name);
			//skip setting to null if it is a primitive(allow setting null on a String 
			if(translatedValue != null)
				applyBeanValueFunction.apply(bean, translatedValue);
			else if(!fieldType.isPrimitive())
				applyBeanValueFunction.apply(bean, translatedValue);
		}
		return bean;
	}

	private BiFunction<Object, Object, Void> createFunction(Class<? extends Object> beanClass, Field field) {
		String key = field.getName();
		String cap = key.substring(0, 1).toUpperCase() + key.substring(1);
		String methodName = "set"+cap;

		//What is slower....throwing exceptions or looping over methods to not through exception?....
		try {
			Method method = beanClass.getMethod(methodName, field.getType());
			return (bean, val) -> invokeMethod(method, bean, val);
		} catch (NoSuchMethodException e) {
			log.warn("performance penalty since method="+methodName+" does not exist on class="+beanClass.getName()+" using field instead to set data");
			return (bean, val) -> setField(field, bean, val);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private Void setField(Field field, Object bean, Object val) {
		field.setAccessible(true);
		try {
			field.set(bean, val);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private Void invokeMethod(Method method, Object bean, Object val) {
		try {
			method.invoke(bean, val);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return null;
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
			throw new IllegalArgumentException("Field with name="+key+" not found in any of the classes="+classList);
		
		return findBeanFieldType(superclass, key, classList);
	}

	private Object createBean(Class<?> paramTypeToCreate) {
		try {
			return paramTypeToCreate.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Object convert(RouteMeta meta, String name, ParamNode valuesToUse, Class<?> paramTypeToCreate, Function<String, Object> converter, Validation validator) {
		Method method = meta.getMethod();
		if(paramTypeToCreate.isPrimitive()) {
			if(valuesToUse == null)
				//This should be a 404 NotFound in production (in cases where user types a non-int value in the query param)
				throw new NotFoundException("The method='"+method+"' requires that @Param("+name+") be of type="
						+paramTypeToCreate+" but the request did not contain any value in query params, path "
								+ "params nor multi-part form fields with a value and we can't convert null to a primitive");
		}
		
		if(valuesToUse == null)
			return null;
		if(!(valuesToUse instanceof ValueNode))
			throw new IllegalArgumentException("method takes param type="+paramTypeToCreate+" but complex structure found");
		ValueNode node = (ValueNode) valuesToUse;
		List<String> values = node.getValue();
		if(values.size() > 1)
			throw new NotFoundException("There is an array of values when the method only takes one value of type="+paramTypeToCreate+" and not an array");
		String value = null;
		if(values.size() == 1)
			value = values.get(0);
		if(paramTypeToCreate == String.class)
			return value;
		try {
			return converter.apply(value);
		} catch(Exception e) {
			if(node.getFrom() == FromEnum.FORM_MULTIPART) {
				validator.addErrorInst(node.getFullKeyName(), "Could not convert value");
				return null;
			} else
				//This should be a 404 in production if the url is bad...
				throw new NotFoundException("The method='"+method+"' requires that the parameter named '"+name+"' or annotated with @Param("+name+") be of type="
						+paramTypeToCreate+" but the request contained a value that could not be converted="+value);
		}
	}

	private boolean isPluginManaged(Class<?> paramTypeToCreate) {
		//TODO: implement so we can either
		// load existing from db from hidden id field in the form
		// create a new db object that is filled in that can easily be saved
		return false;
	}

	private Map<String, List<String>> createStruct(Map<String, String> params) {
		//why is this not easy to do in jdk8 as this is pretty ugly...
		return params.entrySet().stream()
	            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> Arrays.asList(entry.getValue()) ));
	}
	
}