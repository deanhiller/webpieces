package org.webpieces.router.impl.params;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.exceptions.IllegalReturnValueException;

public class ObjectToStringTranslator {

	public Map<String, String> formMap(Method method, List<String> pathParamNames, List<Object> args) {
		if(pathParamNames.size() != args.size())
			throw new IllegalReturnValueException("The Redirect object returned from method='"+method+"' has the wrong number of arguments. args.size="+args.size()+" should be size="+pathParamNames.size());

		Map<String, String> nameToValue = new HashMap<>();
		for(int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			Object obj = args.get(i);
			if(obj != null) {
				//TODO: need reverse binding here!!!!
				//Anotherwords, apps register Converters String -> Object and Object to String and we should really be
				//using that instead of toString to convert which could be different
				nameToValue.put(key, obj.toString());
			}
		}
		return nameToValue;
	}
}
