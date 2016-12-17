package org.webpieces.router.impl.ctx;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.webpieces.ctx.api.FlashSub;
import org.webpieces.router.impl.params.ObjectTranslator;

public class FlashImpl extends FlashScopeImpl implements FlashSub {
	public static String COOKIE_NAME = CookieScopeImpl.COOKIE_NAME_PREFIX+"Flash";

	public FlashImpl(ObjectTranslator objectTranslator) {
		super(objectTranslator);
	}

	public void saveFormParams(Map<String, String> fields, Set<String> secureFieldNames) {
		for(Entry<String, String> entry : fields.entrySet()) {
			String key = entry.getKey();
			if(!secureFieldNames.contains(key))
				put(key, entry.getValue());
		}
	}

	@Override
	public String getName() {
		return COOKIE_NAME;
	}

	public boolean hasMessage() {
		return containsKey("_message");
	}

	public void setMessage(String msg) {
		put("_message", msg);
	}

	public String getMessage() {
		return get("_message");
	}
}
