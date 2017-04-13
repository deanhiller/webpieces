package org.webpieces.router.impl.params;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.ctx.api.WebConverter;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;

public class ObjectToParamTranslator {

	private ObjectTranslator translator;

	@Inject
	public ObjectToParamTranslator(ObjectTranslator translator) {
		this.translator = translator;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> formMap(Method method, List<String> pathParamNames, Map<String, Object> redirectArgs) {
		if(pathParamNames.size() != redirectArgs.size())
			throw new IllegalReturnValueException("The Redirect object returned from method='"+method+"' has the wrong number of arguments. args.size="+redirectArgs.size()+" should be size="+pathParamNames.size());

		Map<String, String> nameToValue = new HashMap<>();
		for(int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			Object value = redirectArgs.get(key);
			
			//value can't be null on redirect
			if(value == null)
				throw new IllegalArgumentException("Controller did not set key='"+key+"' or passed in null"
						+ " for '"+key+"' and this is not allowed as you end up with the word 'null' in your url");
			
			WebConverter function = translator.getConverter(value.getClass());
			if(function != null) {
				nameToValue.put(key, function.objectToString(value));
			} else {
				nameToValue.put(key,  value.toString());
			}
		}
		return nameToValue;
	}
}
