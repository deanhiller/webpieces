package org.webpieces.router.impl.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Singleton;

import org.webpieces.ctx.api.WebConverter;

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

	protected Map<Class<?>, WebConverter<?>> classToConverter = new HashMap<>();
	protected Map<Class<?>, WebConverter<?>> appConverters;
	
	public ObjectTranslator() {
		add(Boolean.class, s -> s == null ? null : Boolean.parseBoolean(s), s -> s == null ? null : s.toString());
		add(Byte.class, s -> s == null ? null : Byte.parseByte(s), s -> s == null ? null : s.toString());
		add(Short.class, s -> s == null ? null : Short.parseShort(s), s -> s == null ? null : s.toString());
		add(Integer.class, s -> s == null ? null : Integer.parseInt(s), s -> s == null ? null : s.toString());
		add(Long.class, s -> s == null ? null : Long.parseLong(s), s -> s == null ? null : s.toString());
		add(Float.class, s -> s == null ? null : Float.parseFloat(s), s -> s == null ? null : s.toString());
		add(Double.class, s -> s == null ? null : Double.parseDouble(s), s -> s == null ? null : s.toString());
		add(String.class, s -> s, s -> s);

		add(Boolean.TYPE, s -> Boolean.parseBoolean(s), s -> s.toString());
		add(Byte.TYPE, s -> Byte.parseByte(s), s -> s.toString());
		add(Short.TYPE, s -> Short.parseShort(s), s -> s.toString());
		add(Integer.TYPE, s -> Integer.parseInt(s), s -> s.toString());
		add(Long.TYPE, s -> Long.parseLong(s), s -> s.toString());
		add(Float.TYPE, s -> Float.parseFloat(s), s -> s.toString());
		add(Double.TYPE, s -> Double.parseDouble(s), s -> s.toString());
	}
	
	private <T> void add(Class<T> clazz, Function<String, T> toObj, Function<T, String> toStr) {
		classToConverter.put(clazz, new PrimitiveConverter<T>(clazz, toObj, toStr));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> WebConverter<T> getConverter(Class<T> fieldClass) {
		WebConverter webConverter = classToConverter.get(fieldClass);
		if(webConverter != null)
			return webConverter;
		return (WebConverter<T>) appConverters.get(fieldClass);
	}

	@SuppressWarnings("rawtypes")
	public void install(Set<WebConverter> converters) {
		//have to recreate for dev mode.  prod only calls this once
		appConverters = new HashMap<>();
		for(WebConverter converter : converters) {
			appConverters.put(converter.getConverterType(), converter);
		}
	}
}
