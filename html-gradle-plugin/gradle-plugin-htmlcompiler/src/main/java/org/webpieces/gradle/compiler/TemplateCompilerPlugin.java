package org.webpieces.gradle.compiler;

import java.io.File;

import javax.inject.Inject;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.classpath.ModuleRegistry;
import org.gradle.api.internal.lambdas.SerializableLambdas;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.internal.tasks.DefaultSourceSetOutput;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.*;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.internal.Cast;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.jvm.tasks.ProcessResources;

/**
 * Based off GroovyBasePlugin.java 
 * https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/GroovyBasePlugin.java
 *
 * NOTE: Most of the variables are still named 'groovyXXX' to keep 1 to 1 so that as that plugin changes, we can adapt easier
 * by comparing as gradle is a complex beast (amazing, but under the hood, complicated to achieve it's goal)
 *
 * @author dhiller
 */
public class TemplateCompilerPlugin implements Plugin<Project> {

    private static final Logger log = Logging.getLogger(TemplateCompilerPlugin.class);
	
    private final ObjectFactory objectFactory;
    //private final ModuleRegistry moduleRegistry;
    private final JvmPluginServices jvmPluginServices;

    private Project project;
    
    @Inject
    public TemplateCompilerPlugin(ObjectFactory objectFactory, ModuleRegistry moduleRegistry, JvmEcosystemUtilities jvmPluginServices) {
		this.objectFactory = objectFactory;
		//this.moduleRegistry = moduleRegistry;
        this.jvmPluginServices = (JvmPluginServices)jvmPluginServices;
    }
    
    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPluginManager().apply(JavaBasePlugin.class);

        configureGroovyRuntimeExtension();
        //configureCompileDefaults();
        configureSourceSetDefaults(project);

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

    private JavaPluginExtension javaPluginExtension() {
        return projectExtension(JavaPluginExtension.class);
    }
    private <T> T projectExtension(Class<T> type) {
        return extensionOf(project, type);
    }

    private <T> T extensionOf(ExtensionAware extensionAware, Class<T> type) {
        return extensionAware.getExtensions().getByType(type);
    }

    private void configureSourceSetDefaults(Project project) {
        log.debug("setup configure source set defaults");

        JavaPluginExtension javaPluginExtension = javaPluginExtension();
//        SourceSetContainer sourceSets = javaPluginExtension.getSourceSets();
//        SourceSet main = sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME);
//        SourceSet test = sourceSets.findByName(SourceSet.TEST_SOURCE_SET_NAME);
//
//        test.setRuntimeClasspath(project.getObjects().fileCollection().from(test.getOutput(), main.getOutput(), project.getConfigurations().getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)));

//        // Register the project's source set output directories
//        sourceSets.all(sourceSet ->
//                buildOutputCleanupRegistry.registerOutputs(sourceSet.getOutput())
//        );

        javaPluginExtension.getSourceSets().all(sourceSet -> {
            String name = "templates";
            final DefaultTemplateSourceSet groovySourceSet = new DefaultTemplateSourceSet(name, ((DefaultSourceSet) sourceSet).getDisplayName(), objectFactory);
            (new DslObject(sourceSet)).getConvention().getPlugins().put("templates", groovySourceSet);

            //copied from JavaBasePlugin
            processCopyTemplateResources(project, sourceSet);

            //sourceSet.getExtensions().add(GroovySourceDirectorySet.class, "groovy", groovySourceSet.getGroovy()); //We do not need this one
            SourceDirectorySet groovySource = groovySourceSet.getTemplateDirSet();
            groovySource.srcDir("src/" + sourceSet.getName() + "/java");
            sourceSet.getResources().getFilter().exclude(SerializableLambdas.spec((element) -> {
                return groovySource.contains(element.getFile());
            }));
            sourceSet.getAllJava().source(groovySource);
            sourceSet.getAllSource().source(groovySource);

            //copy over but comment as I think no longer needed anymore
            //NEEDED?: configureOutputDirectoryForSourceSet(sourceSet, groovySourceSet.getTemplateDirSet(), project);

            final TaskProvider<TemplateCompile> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("templates"), TemplateCompile.class, compile -> {
                JvmPluginsHelper.configureForSourceSet(sourceSet, groovySource, compile, compile.getOptions(), project);
                //copy over but comment as I think no longer needed anymore
                //NEEDED?: compile.dependsOn(sourceSet.getCompileJavaTaskName());
                compile.setDescription("Compiles the " + sourceSet.getName() + " Webpieces Templates.");
                compile.setSource(groovySource);
                //copy over but comment as I think no longer needed anymore
                //NEEDED?: compile.setDestinationDir(new File(project.getBuildDir(), "classes/" + groovySourceSet.getTemplateDirSet().getName() + "/" + sourceSet.getName()));

                //we do not support different versions of java from gradle right now...
                //compile.getJavaLauncher().convention(getToolchainTool(project, JavaToolchainService::launcherFor));
            });
            JvmPluginsHelper.configureOutputDirectoryForSourceSet(sourceSet, groovySource, this.project, compileTask, compileTask.map(TemplateCompile::getOptions));
            this.jvmPluginServices.useDefaultTargetPlatformInference(this.project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName()), compileTask);
            this.jvmPluginServices.useDefaultTargetPlatformInference(this.project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName()), compileTask);
            this.project.getTasks().named(sourceSet.getClassesTaskName(), (task) -> {
                task.dependsOn(new Object[]{compileTask});
            });

            //Ties resources to be done first or something but we don't need this
//            this.project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName()).attributes((attrs) -> {
//                attrs.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, (LibraryElements)this.project.getObjects().named(LibraryElements.class, "classes+resources"));
//            });
        });

        //this.project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new ConfigureAction(this.project, objectFactory));
    }

    private void processCopyTemplateResources(Project project, SourceSet sourceSet) {
//        ConventionMapping outputConventionMapping = ((IConventionAware) sourceSet.getOutput()).getConventionMapping();
//        ConfigurationContainer configurations = project.getConfigurations();
//        this.defineConfigurationsForSourceSet(sourceSet, configurations);
//        this.definePathsForSourceSet(sourceSet, outputConventionMapping, project);
        this.createProcessResourcesTask(sourceSet, sourceSet.getJava(), project);
    }

    private void defineConfigurationsForSourceSet(SourceSet sourceSet, ConfigurationContainer configurations) {
        String implementationConfigurationName = sourceSet.getImplementationConfigurationName();
        String runtimeOnlyConfigurationName = sourceSet.getRuntimeOnlyConfigurationName();
        String compileOnlyConfigurationName = sourceSet.getCompileOnlyConfigurationName();
        String compileClasspathConfigurationName = sourceSet.getCompileClasspathConfigurationName();
        String annotationProcessorConfigurationName = sourceSet.getAnnotationProcessorConfigurationName();
        String runtimeClasspathConfigurationName = sourceSet.getRuntimeClasspathConfigurationName();
        String sourceSetName = sourceSet.toString();
        Configuration implementationConfiguration = (Configuration)configurations.maybeCreate(implementationConfigurationName);
        implementationConfiguration.setVisible(false);
        implementationConfiguration.setDescription("Implementation only dependencies for " + sourceSetName + ".");
        implementationConfiguration.setCanBeConsumed(false);
        implementationConfiguration.setCanBeResolved(false);
        DeprecatableConfiguration compileOnlyConfiguration = (DeprecatableConfiguration)configurations.maybeCreate(compileOnlyConfigurationName);
        compileOnlyConfiguration.setVisible(false);
        compileOnlyConfiguration.setCanBeConsumed(false);
        compileOnlyConfiguration.setCanBeResolved(false);
        compileOnlyConfiguration.setDescription("Compile only dependencies for " + sourceSetName + ".");
        ConfigurationInternal compileClasspathConfiguration = (ConfigurationInternal)configurations.maybeCreate(compileClasspathConfigurationName);
        compileClasspathConfiguration.setVisible(false);
        compileClasspathConfiguration.extendsFrom(new Configuration[]{compileOnlyConfiguration, implementationConfiguration});
        compileClasspathConfiguration.setDescription("Compile classpath for " + sourceSetName + ".");
        compileClasspathConfiguration.setCanBeConsumed(false);
        this.jvmPluginServices.configureAsCompileClasspath(compileClasspathConfiguration);
        ConfigurationInternal annotationProcessorConfiguration = (ConfigurationInternal)configurations.maybeCreate(annotationProcessorConfigurationName);
        annotationProcessorConfiguration.setVisible(false);
        annotationProcessorConfiguration.setDescription("Annotation processors and their dependencies for " + sourceSetName + ".");
        annotationProcessorConfiguration.setCanBeConsumed(false);
        annotationProcessorConfiguration.setCanBeResolved(true);
        this.jvmPluginServices.configureAsRuntimeClasspath(annotationProcessorConfiguration);
        Configuration runtimeOnlyConfiguration = (Configuration)configurations.maybeCreate(runtimeOnlyConfigurationName);
        runtimeOnlyConfiguration.setVisible(false);
        runtimeOnlyConfiguration.setCanBeConsumed(false);
        runtimeOnlyConfiguration.setCanBeResolved(false);
        runtimeOnlyConfiguration.setDescription("Runtime only dependencies for " + sourceSetName + ".");
        ConfigurationInternal runtimeClasspathConfiguration = (ConfigurationInternal)configurations.maybeCreate(runtimeClasspathConfigurationName);
        runtimeClasspathConfiguration.setVisible(false);
        runtimeClasspathConfiguration.setCanBeConsumed(false);
        runtimeClasspathConfiguration.setCanBeResolved(true);
        runtimeClasspathConfiguration.setDescription("Runtime classpath of " + sourceSetName + ".");
        runtimeClasspathConfiguration.extendsFrom(new Configuration[]{runtimeOnlyConfiguration, implementationConfiguration});
        this.jvmPluginServices.configureAsRuntimeClasspath(runtimeClasspathConfiguration);
        sourceSet.setCompileClasspath(compileClasspathConfiguration);
        sourceSet.setRuntimeClasspath(sourceSet.getOutput().plus(runtimeClasspathConfiguration));
        sourceSet.setAnnotationProcessorPath(annotationProcessorConfiguration);
        compileClasspathConfiguration.deprecateForDeclaration(new String[]{implementationConfigurationName, compileOnlyConfigurationName});
        runtimeClasspathConfiguration.deprecateForDeclaration(new String[]{implementationConfigurationName, compileOnlyConfigurationName, runtimeOnlyConfigurationName});
    }

    private void definePathsForSourceSet(SourceSet sourceSet, ConventionMapping outputConventionMapping, Project project) {
        outputConventionMapping.map("resourcesDir", () -> {
            String classesDirName = "resources/" + sourceSet.getName()+"Templates";
            return new File(project.getBuildDir(), classesDirName);
        });
        sourceSet.getJava().srcDir("src/" + sourceSet.getName() + "/java");
        sourceSet.getResources().srcDir("src/" + sourceSet.getName() + "/java").include("**/*.html", "**/*.tag", "**/*.json");
    }

    private void createProcessResourcesTask(SourceSet sourceSet, SourceDirectorySet resourceSet, Project project) {
        TaskProvider<Copy> copyTemplatesTask = project.getTasks().register(sourceSet.getProcessResourcesTaskName() + "Templates", Copy.class, (resourcesTask) -> {
            File fromDir = new File("src/"+sourceSet.getName()+"/java");
            File toDir = new File(project.getBuildDir(), "resources/" + sourceSet.getName());
            resourcesTask.from(fromDir).include("**/*.html", "**/*.tag", "**/*.json");
            resourcesTask.into(toDir);
            resourcesTask.eachFile(new Action<FileCopyDetails>() {
                @Override
                public void execute(FileCopyDetails fileCopyDetails) {
                    log.log(LogLevel.LIFECYCLE, "copying file="+fileCopyDetails.getPath() +" from="+fileCopyDetails.getSourcePath());
                }
            });
            resourcesTask.setDescription("Processes " + resourceSet + ".");
        });

        this.project.getTasks().named(sourceSet.getProcessResourcesTaskName(), (task) -> {
            task.dependsOn(new Object[]{copyTemplatesTask});
        });
    }

    public static void configureOutputDirectoryForSourceSet(final SourceSet sourceSet, final SourceDirectorySet sourceDirectorySet, final Project target) {
        final String sourceSetChildPath = "classes/" + sourceDirectorySet.getName() + "/" + sourceSet.getName();
        sourceDirectorySet.setOutputDir(target.provider(() -> new File(target.getBuildDir(), sourceSetChildPath)));
        DefaultSourceSetOutput sourceSetOutput = Cast.cast(DefaultSourceSetOutput.class, sourceSet.getOutput());
        sourceSetOutput.addClassesDir(sourceDirectorySet.getClassesDirectory());
    }
}
