package org.webpieces.gradle.htmlcompiler;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

public class DefaultHtmlSourceSet implements HtmlSourceSet {
    private final SourceDirectorySet groovy;

    public DefaultHtmlSourceSet(String displayName, SourceDirectorySetFactory sourceDirectorySetFactory) {
        groovy = sourceDirectorySetFactory.create(displayName +  " Template(html/json) source");
        groovy.getFilter().include("**/*.html", "**/*.json");
    }

    public SourceDirectorySet getTemplatesSrc() {
        return groovy;
    }

    public HtmlSourceSet htmlSourceSet(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getTemplatesSrc());
        return this;
    }

}
