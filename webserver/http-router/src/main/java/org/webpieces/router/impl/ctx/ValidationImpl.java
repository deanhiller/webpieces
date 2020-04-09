package org.webpieces.router.impl.ctx;

import org.webpieces.ctx.api.Validation;
import org.webpieces.router.impl.params.ObjectTranslator;

public class ValidationImpl extends FlashScopeImpl implements Validation {
	public static final String COOKIE_NAME = CookieScopeImpl.COOKIE_NAME_PREFIX+"Errors";
	
	public ValidationImpl(ObjectTranslator objectTranslator) {
		super(objectTranslator);
	}

	public void addError(String name, String error) {
		put(name, error);
	}	
	
	public boolean hasErrors() {
		if(cookie.size() > 0)
			return true;
		return false;
	}

	@Override
	public String getName() {
		return COOKIE_NAME;
	}

	public String getError(String fieldName) {
		return get(fieldName);
	}

	public void setGlobalError(String error) { put("_global", error); }

	public boolean hasGlobalError() { return containsKey("_global"); }

	public String globalError() { return get("_global"); }
}
