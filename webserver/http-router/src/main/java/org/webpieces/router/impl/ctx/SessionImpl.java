package org.webpieces.router.impl.ctx;

import org.webpieces.ctx.api.Session;
import org.webpieces.router.impl.params.ObjectTranslator;

public class SessionImpl extends CookieScopeImpl implements Session {

	public static String COOKIE_NAME = CookieScopeImpl.COOKIE_NAME_PREFIX+"Session";
	
	public SessionImpl(ObjectTranslator objectTranslator) {
		super(objectTranslator);
	}

	protected boolean isKeep() {
		return cookie.size() > 0;
	}
	
	@Override
	public String getName() {
		return COOKIE_NAME;
	}

}
