package org.webpieces.ctx.api;

import java.util.Map;

public interface CookieScope {

	boolean isNeedCreateCookie();

	String getName();

	Map<String, String> getMapData();

	Integer getMaxAge();

	void setExisted(boolean b);

	void setMapData(Map<String, String> dataMap);

}
