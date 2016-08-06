package org.webpieces.templating.api;

import java.util.Map;

public interface TemplateResult {

	String getSuperTemplateClassName();

	String getTemplateClassName();

	Map<?, ?> getTemplateProperties();

	String getResult();

}
