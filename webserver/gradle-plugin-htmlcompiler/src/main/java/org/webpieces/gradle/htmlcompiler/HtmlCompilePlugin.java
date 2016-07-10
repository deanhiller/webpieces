package org.webpieces.gradle.htmlcompiler;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;

public class HtmlCompilePlugin implements Plugin<ProjectInternal> {
	
    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public HtmlCompilePlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }
    
    @Override
    public void apply(ProjectInternal project) {
        project.getPluginManager().apply(JavaBasePlugin.class);
        JavaBasePlugin javaBasePlugin = project.getPlugins().getPlugin(JavaBasePlugin.class);
        configureSourceSetDefaults(project, javaBasePlugin);
    }
    
    private void configureSourceSetDefaults(Project project, final JavaBasePlugin javaBasePlugin) {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new Action<SourceSet>() {
            public void execute(SourceSet sourceSet) {
            	System.out.println("hi there....awesome");
            	
                final DefaultHtmlSourceSet templateSourceSet = new DefaultHtmlSourceSet(((DefaultSourceSet) sourceSet).getDisplayName(), sourceDirectorySetFactory);
                new DslObject(sourceSet).getConvention().getPlugins().put("templates", templateSourceSet);

                templateSourceSet.getTemplatesSrc().srcDir("src/main/java");
                sourceSet.getResources().getFilter().exclude(new Spec<FileTreeElement>() {
                    public boolean isSatisfiedBy(FileTreeElement element) {
                        return templateSourceSet.getTemplatesSrc().contains(element.getFile());
                    }
                });
                sourceSet.getAllJava().source(templateSourceSet.getTemplatesSrc());
                sourceSet.getAllSource().source(templateSourceSet.getTemplatesSrc());

                String compileTaskName = sourceSet.getCompileTaskName("templates");
                HtmlCompileTask compile = project.getTasks().create(compileTaskName, HtmlCompileTask.class);
                javaBasePlugin.configureForSourceSet(sourceSet, compile);
                compile.setGroup("Build");
                compile.setDescription("Compiles the " + sourceSet.getName() + " Html or other template files source.");
                compile.dependsOn(sourceSet.getCompileJavaTaskName());

                compile.setSource(templateSourceSet.getTemplatesSrc());

                project.getTasks().getByName(sourceSet.getClassesTaskName()).dependsOn(compileTaskName);
            }
        });
    }
    

}
