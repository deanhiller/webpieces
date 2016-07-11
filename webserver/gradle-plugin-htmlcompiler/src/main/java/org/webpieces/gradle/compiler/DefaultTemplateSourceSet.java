package org.webpieces.gradle.compiler;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

public class DefaultTemplateSourceSet implements TemplateSourceSet {
    private final SourceDirectorySet groovy;

    public DefaultTemplateSourceSet(String displayName, SourceDirectorySetFactory sourceDirectorySetFactory) {
        groovy = sourceDirectorySetFactory.create(displayName +  " Template(html/json) source");
        groovy.getFilter().include("**/*.html", "**/*.json");
    }

    public SourceDirectorySet getTemplatesSrc() {
        return groovy;
    }

    @SuppressWarnings("rawtypes")
	public TemplateSourceSet htmlSourceSet(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getTemplatesSrc());
        return this;
    }

}
