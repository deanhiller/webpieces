package org.webpieces.webserver.impl;

import org.webpieces.router.api.TemplateApi;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.util.Map;

@Singleton
public class WebServerTemplateProxy implements TemplateApi {

    private TemplateService templateService;

    @Inject
    public WebServerTemplateProxy(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs) {
        templateService.loadAndRunTemplate(templatePath, out, pageArgs);
    }

    @Override
    public String convertTemplateClassToPath(String fullClass) {
        //TODO(dhiller): Fix to not be static, we can't bug fix static code live..
        return TemplateUtil.convertTemplateClassToPath(fullClass);
    }
}
