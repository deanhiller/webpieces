package org.webpieces.router.api.simplesvr;

import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.router.api.TemplateApi;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

public class NullTemplateApi implements TemplateApi {
    @Override
    public void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs) {

    }

    @Override
    public String convertTemplateClassToPath(String fullClass) {
        return fullClass;
    }

	@Override
	public void installCustomTags(Set<HtmlTagCreator> tagCreators) {
	}
}
