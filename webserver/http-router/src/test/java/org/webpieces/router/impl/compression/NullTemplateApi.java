package org.webpieces.router.impl.compression;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.router.api.TemplateApi;

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
