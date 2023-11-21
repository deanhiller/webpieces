package org.webpieces.plugin.json;

import java.io.IOException;
import java.lang.reflect.Constructor;
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

import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.SneakyThrow;

public class JacksonJsonConverter {
	
	private SingletonSupplier<ObjectMapper> mapper;
	private Set<Class> primitiveTypes = new HashSet<>();
	private boolean convertNullToEmptyStr;

	/**
	 * NOTE: It is better to inject ObjectMapperProvider in case FeatureTest on creating clients
	 * via Guice forgets to bind to ObjectMapperProvider(or better yet, doesn't need to)
	 */
	@Inject
	public JacksonJsonConverter(ObjectMapperProvider mapperProvider, ConverterConfig config) {
		this.mapper = new SingletonSupplier<>(() -> mapperProvider.get());
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
			if(json.length == 0) {
				return clazz.getDeclaredConstructor().newInstance();
			}
			T obj = mapper.get().readValue(json, clazz);
			if(convertNullToEmptyStr)
				return convertStrings(obj, true);
			return obj;
		} catch(JsonProcessingException e) {
			throw new JsonReadException(e.getMessage(), e);
		} catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
			throw SneakyThrow.sneak(e);
		}
    }

	public <T> T readValue(String json, Class<T> clazz) {
		try {
			if(json.isBlank()) {
				return clazz.getDeclaredConstructor().newInstance();
			}

			T obj = mapper.get().readValue(json, clazz);
			if(convertNullToEmptyStr)
				return convertStrings(obj, true);
			return obj;
		} catch (JsonProcessingException e) {
			throw new JsonReadException(e);
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw SneakyThrow.sneak(e);
        }
    }
	
	private <T> T convertStrings(T obj, boolean toEmptyStr) {
		if (obj == null) {
			return null;
		}

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

		try {
			if(toEmptyStr) {
				if(value == null) {
					setMethod.invoke(obj, "");
				} else {
					setMethod.invoke(obj, value.trim());
				}
			} else {
				if(value != null && value.trim().equals("")) {
					setMethod.invoke(obj, (String)null);
				} else if (value != null) {
					setMethod.invoke(obj, value.trim());
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private Object callGetMethod(Object obj, Method setMethod) {
		Method method = fetchGetMethod(obj, setMethod);
		
		Object value;
		try {
			value = method.invoke(obj);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw SneakyThrow.sneak(e);
		}
		return value;
	}

	private Method fetchGetMethod(Object obj, Method setMethod) {
		String getMethod = "get"+setMethod.getName().substring(3);
		Method method;
		try {
			method = obj.getClass().getMethod(getMethod);
		} catch (NoSuchMethodException | SecurityException e) {
			throw SneakyThrow.sneak(e);
		}
		return method;
	}

	public String writeValueAsString(Object obj) {
		try {
			if(convertNullToEmptyStr) {
				convertStrings(obj, false);
			}
			
			return mapper.get().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public byte[] writeValueAsBytes(Object obj) {
		try {
			if(convertNullToEmptyStr) {
				convertStrings(obj, false);
			}
			
			return mapper.get().writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public JsonNode readTree(byte[] data) {
		try {
			return mapper.get().readTree(data);
		} catch(JsonProcessingException e) {
			throw new JsonReadException(e.getMessage(), e);
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
}
