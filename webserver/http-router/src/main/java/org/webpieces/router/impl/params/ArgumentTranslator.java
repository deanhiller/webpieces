package org.webpieces.router.impl.params;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Validation;
import org.webpieces.router.api.exceptions.NotFoundException;
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
			ParamMeta fieldMeta = new ParamMeta(paramMeta);
			String name = fieldMeta.getName();
			ParamNode paramNode = paramTree.get(name);
			Object arg = translate(req, meta, paramNode, fieldMeta, validator);
			args.add(arg);
		}
		return args.toArray();
	}

	private Object translate(RouterRequest req, RouteMeta meta, ParamNode valuesToUse, Meta fieldMeta, Validation validator) {

		Class<?> fieldClass = fieldMeta.getFieldClass();
		Function<String, Object> converter = primitiveConverter.getConverter(fieldClass);
		if(converter != null) {
			return convert(req, meta, valuesToUse, fieldMeta, converter, validator);
		} else if(fieldClass.isArray()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it="+fieldMeta);
		} else if(fieldClass.isEnum()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it="+fieldMeta);
		} else if(isPluginManaged(fieldClass)) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it="+fieldMeta);
		} else if(List.class.isAssignableFrom(fieldClass)) {
			if(valuesToUse == null)
				return null;
			else if(!(valuesToUse instanceof ArrayNode)) {
				throw new IllegalArgumentException("Found List on field or param="+fieldMeta+" but did not find ArrayNode type");
			}

			List<Object> list = new ArrayList<>();
			ArrayNode n = (ArrayNode) valuesToUse;
			List<ParamNode> paramNodes = n.getList();
			ParameterizedType type = (ParameterizedType) fieldMeta.getParameterizedType();
			Type[] actualTypeArguments = type.getActualTypeArguments();
			Type type2 = actualTypeArguments[0];
			GenericMeta genMeta = new GenericMeta((Class) type2);
			for(ParamNode node : paramNodes) {
				Object bean = null;
				if(node != null)
					bean = translate(req, meta, node, genMeta, validator);
					
				list.add(bean);
			}
			return list;
			
		} else if(valuesToUse instanceof ArrayNode) {
			throw new IllegalArgumentException("Incoming array need a type List but instead found type="+fieldClass+" on field="+fieldMeta);
		} else if(valuesToUse instanceof ValueNode) {
			ValueNode v = (ValueNode) valuesToUse;
			String fullName = v.getFullName();
			throw new IllegalArgumentException("Could not convert incoming value="+v.getValue()+" of key name="+fullName+" field="+fieldMeta);
		} else if(!(valuesToUse instanceof ParamTreeNode)) {
			throw new IllegalStateException("Bug, must be missing a case. v="+valuesToUse+" type to field="+fieldMeta);
		}
		
		Object bean = createBean(fieldClass);
		ParamTreeNode tree = (ParamTreeNode) valuesToUse;
		for(Map.Entry<String, ParamNode> entry: tree.entrySet()) {
			String key = entry.getKey();
			ParamNode value = entry.getValue();
			Field field = findBeanFieldType(bean.getClass(), key, new ArrayList<>());
			
			FieldMeta nextFieldMeta = new FieldMeta(field);
			Object translatedValue = translate(req, meta, value, nextFieldMeta, validator);
			
			nextFieldMeta.setValueOnBean(bean, translatedValue);
		}
		return bean;
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

	private Object convert(RouterRequest req, RouteMeta routeMeta, ParamNode valuesToUse, Meta fieldMeta, Function<String, Object> converter, Validation validator) {
		Class<?> paramTypeToCreate = fieldMeta.getFieldClass();
		Method method = routeMeta.getMethod();
		if(fieldMeta instanceof ParamMeta) {
			//for params only not fields as with fields, we just don't set the field and skip it...before we call a method,
			//we MUST have a value to set
			checkForBadNullToPrimitiveConversion(req, valuesToUse, fieldMeta, method);
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
				if(req.method == HttpMethod.GET) {
					//For GET with query params or path urls, if we can't convert, it should be a 404...
					throw new NotFoundException(s);
				} else {
					//For POST with multipart, this should be a 500
					throw new IllegalArgumentException(s);
				}
			}
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