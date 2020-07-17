package org.webpieces.plugin.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJsonConverter {
	
	private ObjectMapper mapper;
	private Set<Class> primitiveTypes = new HashSet<>();
	private boolean convertNullToEmptyStr;

	@Inject
	public JacksonJsonConverter(ObjectMapper mapper, ConverterConfig config) {
		this.mapper = mapper;
		convertNullToEmptyStr = config.isConvertNullToEmptyStr();
		primitiveTypes.add(Boolean.class);
		primitiveTypes.add(Byte.class);
		primitiveTypes.add(Short.class);
		primitiveTypes.add(Integer.class);
		primitiveTypes.add(Long.class);
		primitiveTypes.add(Float.class);
		primitiveTypes.add(Double.class);
		primitiveTypes.add(Boolean.TYPE);
		primitiveTypes.add(Byte.TYPE);
		primitiveTypes.add(Short.TYPE);
		primitiveTypes.add(Integer.TYPE);
		primitiveTypes.add(Long.TYPE);
		primitiveTypes.add(Float.TYPE);
		primitiveTypes.add(Double.TYPE);
	}

	public <T> T readValue(byte[] json, Class<T> clazz) {
		try {
			T obj = mapper.readValue(json, clazz);
			if(convertNullToEmptyStr)
				return convertStrings(obj, true);
			return obj;
		} catch (JsonProcessingException e) {
			throw new JsonReadException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T readValue(String json, Class<T> clazz) {
		try {
			T obj = mapper.readValue(json, clazz);
			if(convertNullToEmptyStr)
				return convertStrings(obj, true);
			return obj;
		} catch (JsonProcessingException e) {
			throw new JsonReadException(e);
		}
	}
	
	private <T> T convertStrings(T obj, boolean toEmptyStr) {
		Class c = obj.getClass();
		Method[] methods = c.getMethods();
		for(Method m : methods) {
			
			Parameter[] parameters = m.getParameters();
			if(parameters.length == 1 && m.getName().startsWith("set"))
			{
				Class<?> type = parameters[0].getType();
				if(primitiveTypes.contains(type)) {
					continue;
				} else if(type.equals(String.class)) {
					convertNullToEmptyString(obj, m, toEmptyStr);
				} else if(Map.class.isAssignableFrom(type)) {
					Map value = (Map)callGetMethod(obj, m);
					if(value != null)
						convert(value, toEmptyStr);
				} else if(List.class.isAssignableFrom(type)) {
					List value = (List)callGetMethod(obj, m);
					if(value != null)
						convert(value, toEmptyStr);
				} else if(Object.class.isAssignableFrom(type)) {
					Object value = callGetMethod(obj, m);
					if(value != null)
						convertStrings(value, toEmptyStr);
				}
			}
			
		}
		
		return obj;
	}


	private void convert(List values, boolean toEmptyStr) {
		for(Object obj : values) {
			convertStrings(obj, toEmptyStr);
		}
	}

	private void convert(Map values, boolean toEmptyStr) {
		for(Object value : values.values()) {
			convertStrings(value, toEmptyStr);
		}
	}

	private void convertNullToEmptyString(Object obj, Method setMethod, boolean toEmptyStr) {
		String value = (String) callGetMethod(obj, setMethod);
		
		if(toEmptyStr) {
			if(value == null) {
				try {
					setMethod.invoke(obj, "");
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			if(value != null && value.trim().equals("")) {
				try {
					setMethod.invoke(obj, (String)null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private Object callGetMethod(Object obj, Method setMethod) {
		Method method = fetchGetMethod(obj, setMethod);
		
		Object value;
		try {
			value = method.invoke(obj);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return value;
	}

	private Method fetchGetMethod(Object obj, Method setMethod) {
		String getMethod = "get"+setMethod.getName().substring(3);
		Method method;
		try {
			method = obj.getClass().getMethod(getMethod);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Method="+getMethod+" does not exist on class="+obj.getClass().getName(), e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		return method;
	}

	public String writeValueAsString(Object obj) {
		try {
			if(convertNullToEmptyStr) {
				convertStrings(obj, false);
			}
			
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public byte[] writeValueAsBytes(Object obj) {
		try {
			if(convertNullToEmptyStr) {
				convertStrings(obj, false);
			}
			
			return mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public JsonNode readTree(byte[] data) {
		try {
			return mapper.readTree(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
