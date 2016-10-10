package org.webpieces.router.impl.params;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.webpieces.router.api.exceptions.IllegalReturnValueException;

public class ObjectToParamTranslator {

	private ObjectTranslator translator;

	@Inject
	public ObjectToParamTranslator(ObjectTranslator translator) {
		this.translator = translator;
	}
	
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
			
			Function<Object, String> function = translator.getMarshaller(value.getClass());
			if(function != null) {
				nameToValue.put(key, function.apply(value));
			} else {
				nameToValue.put(key,  value.toString());
			}
		}
		return nameToValue;
	}
}
