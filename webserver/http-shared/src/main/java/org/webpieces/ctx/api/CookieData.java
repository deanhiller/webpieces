package org.webpieces.ctx.api;

import java.util.List;
import java.util.Map;

public interface CookieData {

	boolean isNeedCreateCookie();

	String getName();

	Map<String, List<String>> getMapData();

	Integer getMaxAge();

	void setExisted(boolean b);

	void setMapData(Map<String, List<String>> dataMap);

}
