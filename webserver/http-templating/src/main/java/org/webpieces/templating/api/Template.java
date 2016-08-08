package org.webpieces.templating.api;

import java.util.Map;

public interface Template {

	TemplateResult run(Map<String, Object> pageArgs, Map<?, ?> templateProps, ReverseUrlLookup urlLookup);

}
