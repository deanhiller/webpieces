package org.webpieces.gradle.compiler;

import static org.gradle.api.reflect.TypeOf.typeOf;
import static org.gradle.util.ConfigureUtil.configure;

import javax.annotation.Nullable;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import groovy.lang.Closure;

/**
 * Mimic DefaultGroovySourceSet 
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/internal/tasks/DefaultGroovySourceSet.java
 */ 
public class DefaultTemplateSourceSet implements TemplateSourceSet, HasPublicType {
    private final SourceDirectorySet templates;

    public DefaultTemplateSourceSet(String name, String displayName, ObjectFactory objectFactory) {
        templates = objectFactory.sourceDirectorySet(name, displayName +  " Template(html/tag/json) source");
        templates.getFilter().include("**/*.html", "**/*.tag", "**/*.json");
    }

    public SourceDirectorySet getTemplateDirSet() {
        return templates;
    }


    @SuppressWarnings("rawtypes")
	public TemplateSourceSet template(@Nullable Closure configureClosure) {
        configure(configureClosure, getTemplateDirSet());
        return this;
    }

    @Override
    public TemplateSourceSet template(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getTemplateDirSet());
        return this;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return typeOf(TemplateSourceSet.class);
    }
}
