package org.webpieces.ctx.api;

import java.util.Map;
import java.util.Set;

public interface FlashSub extends Flash {

	void saveFormParams(Map<String, String> fields, Set<String> secureFieldNames);

}
