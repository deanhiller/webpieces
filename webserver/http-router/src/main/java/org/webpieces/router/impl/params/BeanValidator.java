package org.webpieces.router.impl.params;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

import org.webpieces.http.exception.Violation;

public class BeanValidator {
	
	private Validator validator;
	private ExecutableValidator execValidator;

	@Inject
	public BeanValidator(
			Validator validator,
			ExecutableValidator execValidator
	) {
		this.validator = validator;
		this.execValidator = execValidator;
	}

	public List<Violation> validate(Object controller, Method m, List<Object> args) {
		Object[] params = args.toArray();
		List<Violation> all = new ArrayList<>();
		Set<ConstraintViolation<Object>> violations = execValidator.validateParameters(controller, m, params);
		addAll(null, all, violations);
		
		Parameter[] parameters = m.getParameters();
		for(int i = 0; i < parameters.length; i++) {
			Object arg = args.get(i);
			if(arg != null) {
				Parameter param = parameters[i];
				String paramName = param.getName();
				Set<ConstraintViolation<Object>> violations2 = validator.validate(arg);
				addAll(paramName+".", all, violations2);
			}
		}
		
		return all;
	}

	private void addAll(String prefix, List<Violation> all, Set<ConstraintViolation<Object>> violations) {
		for(ConstraintViolation<Object> violation : violations) {
			String path = getPathFun(prefix, violation);
			all.add(new Violation(path, violation.getMessage()));
		}
	}

	private String getPathFun(String prefix, ConstraintViolation<Object> violation) {
		Path path = violation.getPropertyPath();
		Iterator<Node> iterator = path.iterator();
		String pathStr = prefix;
		while(iterator.hasNext()) {
			if(prefix == null) {
				//This is a method, so get last piece of path!!
				pathStr = iterator.next().getName();
			} else {
				//This is a bean.prop.prop so put it all together
				pathStr += iterator.next().getName();				
			}
			if(iterator.hasNext()) {
				pathStr += "."; //only if not the end
			}
		}
		return pathStr;
	}

	
}
