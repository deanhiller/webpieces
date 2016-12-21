package org.webpieces.router.impl.params;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Singleton;

/**
 * This is THE class to translate objects to strings and strings to objects.  Overriding this, you can also add types 
 * to translate as well like jodatime dates or java.util.Dates etc.  This translator applies to EVERYTHING in the
 * platform OR if it doesn't, that is a bug(I don't think we missed any locations).  This includes rendering in 
 * templates, path parames, etc. etc.
 * 
 * @author dhiller
 *
 */
@Singleton
public class ObjectTranslator {

	private Map<Class<?>, Function<String, Object>> classToUnmarshaller = new HashMap<>();
	private Map<Class<?>, Function<Object, String>> classToMarshaller = new HashMap<>();
	
	public ObjectTranslator() {
		classToUnmarshaller.put(Boolean.class, s -> s == null ? null : Boolean.parseBoolean(s));
		classToUnmarshaller.put(Boolean.TYPE, s -> Boolean.parseBoolean(s));
		classToUnmarshaller.put(Byte.class, s -> s == null ? null : Byte.parseByte(s));
		classToUnmarshaller.put(Byte.TYPE, s -> Byte.parseByte(s));
		classToUnmarshaller.put(Short.class, s -> s == null ? null : Short.parseShort(s));
		classToUnmarshaller.put(Short.TYPE, s -> Short.parseShort(s));
		classToUnmarshaller.put(Integer.class, s -> s == null ? null : Integer.parseInt(s));
		classToUnmarshaller.put(Integer.TYPE, s -> Integer.parseInt(s));
		classToUnmarshaller.put(Long.class, s -> s == null ? null : Long.parseLong(s));
		classToUnmarshaller.put(Long.TYPE, s -> Long.parseLong(s));
		classToUnmarshaller.put(Float.class, s -> s == null ? null : Float.parseFloat(s));
		classToUnmarshaller.put(Float.TYPE, s -> Float.parseFloat(s));
		classToUnmarshaller.put(Double.class, s -> s == null ? null : Double.parseDouble(s));
		classToUnmarshaller.put(Double.TYPE, s -> Double.parseDouble(s));
		classToUnmarshaller.put(String.class, s -> s);
		
		classToMarshaller.put(Boolean.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Boolean.TYPE, s -> s.toString());
		classToMarshaller.put(Byte.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Byte.TYPE, s -> s.toString());
		classToMarshaller.put(Short.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Short.TYPE, s -> s.toString());
		classToMarshaller.put(Integer.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Integer.TYPE, s -> s.toString());
		classToMarshaller.put(Long.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Long.TYPE, s -> s.toString());
		classToMarshaller.put(Float.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Float.TYPE, s -> s.toString());
		classToMarshaller.put(Double.class, s -> s == null ? null : s.toString());
		classToMarshaller.put(Double.TYPE, s -> s.toString());
		classToMarshaller.put(String.class, s -> s == null ? null : s.toString());
	}

	public Function<String, Object> getUnmarshaller(Class<?> paramTypeToCreate) {
		return classToUnmarshaller.get(paramTypeToCreate);
	}

	public Function<Object, String> getMarshaller(Class<?> type) {
		return classToMarshaller.get(type);
	}
}
