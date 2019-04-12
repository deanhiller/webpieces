package org.webpieces.gradle.compiler;

import javax.annotation.Nullable;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;

import groovy.lang.Closure;

public interface TemplateSourceSet {

    /**
     * Returns the source to be compiled by the Webpieces *.html/*.tag/*.json compiler for this source set. 
     *
     * @return The Html/*.tag/*.json source. Never returns null.
     */
	SourceDirectorySet getTemplateDirSet();
	
    /**
     * Configures the Groovy source for this set.
     *
     * <p>The given closure is used to configure the {@link SourceDirectorySet} which contains the Groovy source.
     *
     * @param configureClosure The closure to use to configure the Groovy source.
     * @return this
     */
	@SuppressWarnings("rawtypes")
	TemplateSourceSet template(@Nullable Closure configureClosure);

    /**
     * Configures the Groovy source for this set.
     *
     * <p>The given action is used to configure the {@link SourceDirectorySet} which contains the Groovy source.
     *
     * @param configureAction The action to use to configure the Groovy source.
     * @return this
     */
	TemplateSourceSet template(Action<? super SourceDirectorySet> configureAction);
	
}
