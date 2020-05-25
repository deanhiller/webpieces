package org.webpieces.router.impl.ctx;

import org.webpieces.ctx.api.FlashScope;
import org.webpieces.router.impl.params.ObjectTranslator;

public abstract class FlashScopeImpl extends CookieScopeImpl implements FlashScope {

	protected Boolean isKeep = null;

	public FlashScopeImpl(ObjectTranslator objectTranslator) {
		super(objectTranslator);
	}

	@Deprecated
	public void keep() {
		if(isKeep != null)
			throw new IllegalStateException("isKeep is already set.  you cannot set/unset");
		isKeep = true;
	}
	
	@Override
	public void keep(boolean isKeep) {
		if(this.isKeep != null)
			throw new IllegalStateException("isKeep is already set.  you cannot set this twice");
		this.isKeep = isKeep;
	}

	@Override
	public boolean isKeepFlagSet() {
		return isKeep != null;
	}
	
	@Override
	public boolean isKeep() {
		if(isKeep == null) {
			return false;
		} else if(isKeep && cookie.size() > 0) {
			return true;
		}
		return false;
	}
}
