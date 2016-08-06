package org.webpieces.templating.api;

import java.util.Map;

public interface TemplateInfo {

	String getSuperTemplateClassName();

	String getTemplateClassName();

	Map<?, ?> getTemplateProperties();

	String getResult();

}
