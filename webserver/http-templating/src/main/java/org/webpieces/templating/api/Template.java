package org.webpieces.templating.api;

import java.util.Map;

public interface Template {

	TemplateInfo run(Map<String, Object> pageArgs, Map<?, ?> templateProps);

}
