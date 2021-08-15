package org.webpieces.ctx.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FlashSub extends Flash {

	void saveFormParams(Map<String, List<String>> fields, Set<String> secureFieldNames);

}
