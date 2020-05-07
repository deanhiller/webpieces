package org.webpieces.router.impl.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Singleton;

import org.webpieces.router.api.extensions.ObjectStringConverter;

//TODO(dhiller): Make this class part of utils?  It's very useful for argument conversion as well.
//We hae to worry about startup and whether converters are installed early enough to also parse arguments though!!

/**
 * This is THE class to translate objects to strings and strings to objects.  Overriding this, you can also add types 
 * to translate as well like jodatime dates or java.util.Dates etc.  This translator applies to EVERYTHING in the
 * platform OR if it doesn't, that is a bug(I don't think we missed any locations).  This includes rendering in 
 * templates, path parames, etc. etc.
 * 
 * Some examples are
 * 1. If you put into any cookie an object, it translates that to a string via converters and you get it back with get(key, XXXXX.class) and it converts it back to object
 * 2. On a GET, you may have url path params OR url query params.  ie. /url/{id} (path param) or /url?id=value and those are converted using these converters from string to object
 * 3. On a POST, you may have url path params OR url query params OR form post fields(multi-part) and all those are converted string to object via these converters if possible
 * 4. When created a redirect url from ROUTE_ID, key=Object1, key2=Object2, we convert all those objects to strings which are converted back on step 2/3 above
 * 5. When creating a url in a webpage, the groovy may be @[ROUTE_ID, key:object1, key2:object2] and object to string is used so on 2 and 3, we convert them back to objects
 * 5. The hibernate plugin puts an id in a <input type="hidden" in the webpage.  To do this, it converts object to string.  Then on post, it converst that string back to object through these converters
 * 6. The property bean plugin if you expose set/getSomething(Bean b), it exposes Strings to input fields in the webpage and converts between getters/setters
 *
 * @author dhiller
 *
 */
@Singleton
public class ObjectTranslator {

	protected Map<Class<?>, ObjectStringConverter<?>> classToConverter = new HashMap<>();
	protected Map<Class<?>, ObjectStringConverter<?>> appConverters;
	protected static final ObjectStringConverter<Object> SIMPLE_CONVERTER = new SimpleConverter();
	
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

	@SuppressWarnings("unchecked")
	public <T> ObjectStringConverter<T> getConverterFor(T bean) {
		if(bean == null) {
			return (ObjectStringConverter<T>) SIMPLE_CONVERTER;
		}
		
		ObjectStringConverter<T> converter = (ObjectStringConverter<T>) getConverter(bean.getClass());
		if(converter != null)
			return converter;
		return (ObjectStringConverter<T>) SIMPLE_CONVERTER;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> ObjectStringConverter<T> getConverter(Class<T> fieldClass) {
		ObjectStringConverter webConverter = classToConverter.get(fieldClass);
		if(webConverter != null)
			return webConverter;
		return (ObjectStringConverter<T>) appConverters.get(fieldClass);
	}

	@SuppressWarnings("rawtypes")
	public void install(Set<ObjectStringConverter> converters) {
		//have to recreate for dev mode.  prod only calls this once
		appConverters = new HashMap<>();
		for(ObjectStringConverter converter : converters) {
			appConverters.put(converter.getConverterType(), converter);
		}
	}
}
