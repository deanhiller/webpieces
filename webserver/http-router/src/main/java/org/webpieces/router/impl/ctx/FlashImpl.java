package org.webpieces.router.impl.ctx;

import java.util.List;
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

	public void saveFormParams(Map<String, List<String>> fields, Set<String> secureFieldNames) {
		for(Entry<String, List<String>> entry : fields.entrySet()) {
			String key = entry.getKey();
			if(!secureFieldNames.contains(key)) {
				List<String> value = entry.getValue();
				if(value.size() == 1)
					put(key, value.get(0));
				else
					put(key, form(value));
			}
		}
	}

	private String form(List<String> values) {
		String result = "";
		String seperator = "";
		for(String val : values) {
			result += seperator + val;
			if(seperator.equals(""))
				seperator = ",";
		}
		return result;
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

	@Override
	public String getError() {
		return get("_error");
	}

	@Override
	public void setError(String msg) {
		put("_error", msg);
	}
	
	@Override
	public void setShowEditPopup(boolean b) {
		put("_showEditPopup", b+"");
	}

	@Override
	public boolean isShowEditPopup() {
		String str = get("_showEditPopup");
		if(str == null)
			return false;
		return Boolean.parseBoolean(str);
	}
}
