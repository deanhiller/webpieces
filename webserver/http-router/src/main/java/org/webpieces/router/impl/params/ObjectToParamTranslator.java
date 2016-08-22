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
	
	public Map<String, String> formMap(Method method, List<String> pathParamNames, List<Object> args) {
		if(pathParamNames.size() != args.size())
			throw new IllegalReturnValueException("The Redirect object returned from method='"+method+"' has the wrong number of arguments. args.size="+args.size()+" should be size="+pathParamNames.size());

		Map<String, String> nameToValue = new HashMap<>();
		for(int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			Object obj = args.get(i);
			if(obj != null) {
				Function<Object, String> function = translator.getMarshaller(obj.getClass());
				if(function != null) {
					nameToValue.put(key, function.apply(obj));
				} else {
					nameToValue.put(key,  obj.toString());
				}
			}
		}
		return nameToValue;
	}
}
