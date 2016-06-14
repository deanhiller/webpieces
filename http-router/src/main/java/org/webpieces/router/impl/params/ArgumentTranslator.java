package org.webpieces.router.impl.params;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.Param;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.RouteMeta;

public class ArgumentTranslator {

	private ParamValueTreeCreator treeCreator;
	private PrimitiveTranslator primitiveConverter;

	@Inject
	public ArgumentTranslator(ParamValueTreeCreator treeCreator, PrimitiveTranslator primitiveConverter) {
		this.treeCreator = treeCreator;
		this.primitiveConverter = primitiveConverter;
	}
	
	public Object[] createArgs(MatchResult result, Request req) {
		RouteMeta meta = result.getMeta();
		Parameter[] paramMetas = meta.getMethod().getParameters();
		
		ParamTreeNode paramTree = new ParamTreeNode();
		//For multipart AND for query params such as ?var=xxx&var=yyy&var2=xxx
		
		//query params first
		treeCreator.createTree(paramTree, req.queryParams);
		//next multi-part overwrites query params (if duplicates which there should not be)
		treeCreator.createTree(paramTree, req.multiPartFields);
		//lastly path params overwrites multipart and query params (if duplicates which there should not be)
		treeCreator.createTree(paramTree, createStruct(result.getPathParams()));
		
		List<Object> args = new ArrayList<>();
		for(Parameter paramMeta : paramMetas) {
			Object arg = translate(meta, paramTree, paramMeta);
			args.add(arg);
		}
		return args.toArray();
	}

	private Object translate(RouteMeta meta, ParamTreeNode paramTree, Parameter paramMeta) {
		Param annotation = paramMeta.getAnnotation(Param.class);
		String name = annotation.value();
		ParamNode valuesToUse = paramTree.get(name);
		
		Class<?> paramTypeToCreate = paramMeta.getType();
		
		Function<String, Object> converter = primitiveConverter.getConverter(paramTypeToCreate);
		if(converter != null) {
			return convert(meta, name, valuesToUse, paramTypeToCreate, converter);
		} else if(paramTypeToCreate.isArray()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		} else if(paramTypeToCreate.isEnum()) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		} else if(isPluginManaged(paramTypeToCreate)) {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		} else {
			throw new UnsupportedOperationException("not done yet...let me know and I will do it");
		}
	}

	private Object convert(RouteMeta meta, String name, ParamNode valuesToUse, Class<?> paramTypeToCreate, Function<String, Object> converter) {
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
		String[] values = node.getValue();
		if(values.length > 1)
			throw new NotFoundException("There is an array of values when the method only takes one value of type="+paramTypeToCreate+" and not an array");
		String value = null;
		if(values.length == 1)
			value = values[0];
		if(paramTypeToCreate == String.class)
			return value;
		try {
			return converter.apply(value);
		} catch(Exception e) {
			//This should be a 404 in production
			throw new NotFoundException("The method='"+method+"' requires that @Param("+name+") be of type="
					+paramTypeToCreate+" but the request contained a value that could not be converted="+value);
		}
	}

	private boolean isPluginManaged(Class<?> paramTypeToCreate) {
		//TODO: implement so we can either
		// load existing from db from hidden id field in the form
		// create a new db object that is filled in that can easily be saved
		return false;
	}

	private Map<String, String[]> createStruct(Map<String, String> params) {
		//why is this not easy to do in jdk8 as this is pretty ugly...
		return params.entrySet().stream()
	            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> new String[] { entry.getValue() } ));
	}

}