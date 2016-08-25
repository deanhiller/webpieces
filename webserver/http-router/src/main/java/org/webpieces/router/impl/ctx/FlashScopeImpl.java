package org.webpieces.router.impl.ctx;

import org.webpieces.ctx.api.FlashScope;
import org.webpieces.router.impl.params.ObjectTranslator;

public abstract class FlashScopeImpl extends CookieScopeImpl implements FlashScope {

	protected boolean isKeep = false;

	public FlashScopeImpl(ObjectTranslator objectTranslator) {
		super(objectTranslator);
	}

	public void keep() {
		isKeep = true;
	}
	
	@Override
	public boolean isKeep() {
		if(isKeep && cookie.size() > 0) {
			return true;
		}
		return false;
	}
}
