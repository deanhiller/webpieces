package org.webpieces.gradle.compiler;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.classpath.ModuleRegistry;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.internal.tasks.DefaultSourceSetOutput;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.internal.SourceSetUtil;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.Cast;

/**
 * Based off GroovyBasePlugin.java 
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/GroovyBasePlugin.java
 * 
 * @author dhiller
 */
public class TemplateCompilerPlugin implements Plugin<Project> {
	
    private final ObjectFactory objectFactory;
    private final ModuleRegistry moduleRegistry;
    
    private Project project;
    
    @Inject
    public TemplateCompilerPlugin(ObjectFactory objectFactory, ModuleRegistry moduleRegistry) {
		this.objectFactory = objectFactory;
		this.moduleRegistry = moduleRegistry;
    }
    
    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPluginManager().apply(JavaBasePlugin.class);

        configureGroovyRuntimeExtension();
        //configureCompileDefaults();
        configureSourceSetDefaults();

        //configureGroovydoc();
    }

    private void configureGroovyRuntimeExtension() {
        //not needed but could do webpieces version but we just install the correct plugin version instead as each
        //webepices plugin is in step with webpieces release
//        groovyRuntime = project.getExtensions().create(GROOVY_RUNTIME_EXTENSION_NAME, GroovyRuntime.class, project);
    	project.getExtensions().create("compileTemplateSetting", TemplateCompileOptions.class, project);
    }

      //not needed but could do webpieces compiler like the goovy one but again we keep plugins releasing along with
      // webpieces releases so we don't see a need yet
//    private void configureCompileDefaults() {
//        project.getTasks().withType(TemplateCompilerTask.class).configureEach(new Action<TemplateCompilerTask>() {
//            public void execute(final TemplateCompilerTask compile) {
//                compile.getConventionMapping().map("templatesClasspath", new Callable<Object>() {
//                    public Object call() throws Exception {
//                        return groovyRuntime.inferGroovyClasspath(compile.getClasspath());
//                    }
//                });
//            }
//        });
//    }

    private void configureSourceSetDefaults() {
        System.out.println("setup configure source set defaults");  

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new ConfigureAction(project, objectFactory));
    }

    private static class ConfigureAction implements Action<SourceSet> {
        private Project project;
		private ObjectFactory objectFactory;

		public ConfigureAction(Project project, ObjectFactory objectFactory) {
        	this.project = project;
        	this.objectFactory = objectFactory;
		}

		public void execute(final SourceSet sourceSet) {
            System.out.println("executing source set default setup");  

            final DefaultTemplateSourceSet groovySourceSet = new DefaultTemplateSourceSet("templates", ((DefaultSourceSet) sourceSet).getDisplayName(), objectFactory);
            new DslObject(sourceSet).getConvention().getPlugins().put("templates", groovySourceSet);
            
            groovySourceSet.getTemplateDirSet().srcDir("src/" + sourceSet.getName() + "/java");
            sourceSet.getResources().getFilter().exclude(new Spec<FileTreeElement>() {
                public boolean isSatisfiedBy(FileTreeElement element) {
                    return groovySourceSet.getTemplateDirSet().contains(element.getFile());
                }
            });
            
            sourceSet.getAllJava().source(groovySourceSet.getTemplateDirSet());
            sourceSet.getAllSource().source(groovySourceSet.getTemplateDirSet());

            //copy over since it is deprecated...
            configureOutputDirectoryForSourceSet(sourceSet, groovySourceSet.getTemplateDirSet(), project);
            
            final Provider<TemplateCompilerTask> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("templates"), TemplateCompilerTask.class, new Action<TemplateCompilerTask>() {
                @Override
                public void execute(TemplateCompilerTask compile) {
                    SourceSetUtil.configureForSourceSet(sourceSet, groovySourceSet.getTemplateDirSet(), compile, compile.getOptions(), project);
                    compile.dependsOn(sourceSet.getCompileJavaTaskName());
                    compile.setDescription("Compiles the " + sourceSet.getName() + " Groovy source.");
                    compile.setSource(groovySourceSet.getTemplateDirSet());
                }
            });

            // TODO: `classes` should be a little more tied to the classesDirs for a SourceSet so every plugin
            // doesn't need to do this.
            project.getTasks().named(sourceSet.getClassesTaskName()).configure(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    task.dependsOn(compileTask);
                }
            });
        }
    }
    
    public static void configureOutputDirectoryForSourceSet(final SourceSet sourceSet, final SourceDirectorySet sourceDirectorySet, final Project target) {
        final String sourceSetChildPath = "classes/" + sourceDirectorySet.getName() + "/" + sourceSet.getName();
        sourceDirectorySet.setOutputDir(target.provider(new Callable<File>() {
            @Override
            public File call() {
                return new File(target.getBuildDir(), sourceSetChildPath);
            }
        }));

        DefaultSourceSetOutput sourceSetOutput = Cast.cast(DefaultSourceSetOutput.class, sourceSet.getOutput());
        sourceSetOutput.addClassesDir(new Callable<File>() {
            @Override
            public File call() {
                return sourceDirectorySet.getOutputDir();
            }
        });
    }
}
