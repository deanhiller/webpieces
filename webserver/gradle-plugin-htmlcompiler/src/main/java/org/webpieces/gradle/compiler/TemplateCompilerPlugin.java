package org.webpieces.gradle.compiler;

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

public class TemplateCompilerPlugin implements Plugin<ProjectInternal> {
	
    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public TemplateCompilerPlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }
    
    @Override
    public void apply(ProjectInternal project) {
    	project.getExtensions().create("compileTemplateSetting", TemplateCompileOptions.class);
        project.getPluginManager().apply(JavaBasePlugin.class);
        JavaBasePlugin javaBasePlugin = project.getPlugins().getPlugin(JavaBasePlugin.class);
        configureSourceSetDefaults(project, javaBasePlugin);
    }
    
    private void configureSourceSetDefaults(Project project, final JavaBasePlugin javaBasePlugin) {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new CompileAction(project, javaBasePlugin));
    }

    private class CompileAction implements Action<SourceSet> {
        private Project project;
		private JavaBasePlugin javaBasePlugin;

		public CompileAction(Project project, JavaBasePlugin javaBasePlugin) {
			this.project = project;
			this.javaBasePlugin = javaBasePlugin;
		}

		public void execute(SourceSet sourceSet) {
            final DefaultTemplateSourceSet templateSourceSet = new DefaultTemplateSourceSet(((DefaultSourceSet) sourceSet).getDisplayName(), sourceDirectorySetFactory);
            new DslObject(sourceSet).getConvention().getPlugins().put("templates", templateSourceSet);

            templateSourceSet.getTemplatesSrc().srcDir("src/"+sourceSet.getName()+"/java");
            sourceSet.getResources().getFilter().exclude(new Spec<FileTreeElement>() {
                public boolean isSatisfiedBy(FileTreeElement element) {
                    return templateSourceSet.getTemplatesSrc().contains(element.getFile());
                }
            });
            sourceSet.getAllJava().source(templateSourceSet.getTemplatesSrc());
            sourceSet.getAllSource().source(templateSourceSet.getTemplatesSrc());

            String compileTaskName = sourceSet.getCompileTaskName("templates");
            TemplateCompilerTask compile = project.getTasks().create(compileTaskName, TemplateCompilerTask.class);
            javaBasePlugin.configureForSourceSet(sourceSet, compile);
            compile.setGroup("Build");
            compile.setDescription("Compiles the " + sourceSet.getName() + " Html or other template files source.");
            compile.dependsOn(sourceSet.getCompileJavaTaskName());

            compile.setSource(templateSourceSet.getTemplatesSrc());

            project.getTasks().getByName(sourceSet.getClassesTaskName()).dependsOn(compileTaskName);
        }
    }

}
