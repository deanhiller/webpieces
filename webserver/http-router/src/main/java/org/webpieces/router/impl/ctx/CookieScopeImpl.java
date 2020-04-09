package org.webpieces.router.impl.ctx;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.ctx.api.CookieScope;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.impl.params.ObjectTranslator;


public abstract class CookieScopeImpl implements CookieScope {

	public final static String COOKIE_NAME_PREFIX = "web";
	
	protected boolean previouslyExisted = false;
	protected boolean hasModifiedData = false;
	
	/**
	 * "" represents user set to null vs user did not set to null
	 */
	protected Map<String, String> cookie = new HashMap<>();

	private ObjectTranslator objectTranslator;
	
	public CookieScopeImpl(ObjectTranslator objectTranslator) {
		this.objectTranslator = objectTranslator;
	}
	
	public void setExisted(boolean existed) {
		this.previouslyExisted = existed;
	}

	//All cookie states(Keep in mind isKeep for Session is hasDataInMap)
	//previouslyExisted   isKeep   dataModified     result
	//false                false    false            no create cookie
	//false                false    true             no create cookie
	//false                true     false            no create cookie because Map empty
	//false                true     true             ***create new cookie***
	//true                 false    false            create delete cookie
	//true                 false    true             create delete cookie
	//true                 true     false            no-op, let browser keep cookie(no need to reset it)
	//true                 true     true             ***create update cookie***
	public boolean isNeedCreateSetCookie() {
		if(isKeep() && hasModifiedData)
			return true;
		return false;
	}
	
	public boolean isNeedCreateDeleteCookie() {
		if(previouslyExisted && !isKeep())
			return true;
		return false;
	}
	
	protected abstract boolean isKeep();
	
	public Map<String, String> getMapData() {
		return cookie;
	}

	public void setMapData(Map<String, String> dataMap) {
		this.cookie = dataMap;
	}
	
	@Override
	public boolean containsKey(String key) {
		return cookie.containsKey(key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void put(String key, Object value) {		
		hasModifiedData = true;
		ObjectStringConverter marshaller = objectTranslator.getConverterFor(value);
		String strValue = marshaller.objectToString(value);
		cookie.put(key, strValue);
	}
	
	@Override
	public <T> T remove(String key, Class<T> type) {
		hasModifiedData = true;
		String valueStr = remove(key);
		return translate(type, valueStr);
	}

	private <T> T translate(Class<T> type, String valueStr) {
		ObjectStringConverter<T> unmarshaller = objectTranslator.getConverter(type);
		T result = unmarshaller.stringToObject(valueStr);
		return result;
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		String valueStr = get(key);
		return translate(type, valueStr);
	}

	@Override
	public String get(String key) {
		return cookie.get(key);
	}

	@Override
	public String remove(String key) {
		hasModifiedData = true;
		return cookie.remove(key);
	}
}
