package org.webpieces.gradle.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.compile.CompilerForkUtils;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.webpieces.templating.api.ProdConstants;
import org.webpieces.templatingdev.api.CompileCallback;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.StubModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.templatingdev.impl.HtmlToJavaClassCompiler;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.util.net.URLEncoder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import groovy.lang.GroovyClassLoader;

@CacheableTask
public class TemplateCompile extends AbstractCompile implements HasCompileOptions {

	private static final Logger log = Logging.getLogger(TemplateCompile.class);

    private final CompileOptions compileOptions;
    //private final GroovyCompileOptions groovyCompileOptions = new GroovyCompileOptions();

    public TemplateCompile() {
        CompileOptions compileOptions = getServices().get(ObjectFactory.class).newInstance(CompileOptions.class);
        this.compileOptions = compileOptions;
        CompilerForkUtils.doNotCacheIfForkingViaExecutable(compileOptions, getOutputs());
    }
    
    /**
     * Returns the options for Java compilation.
     *
     * @return The Java compile options. Never returns null.
     */
    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }
//    private TemplateCompileOptions options = new TemplateCompileOptions();
//
//    @Nested
//    public TemplateCompileOptions getOptions() {
//        return options;
//    }
//
//    public void options(Action<TemplateCompileOptions> action) {
//        action.execute(getOptions());
//    }
//
//    public void options(Closure<?> closure) {
//        getProject().configure(getOptions(), closure);
//    }
    
	@TaskAction
	public void compile() {
		try {
			TemplateCompileOptions options = getProject().getExtensions().findByType(TemplateCompileOptions.class);
			compileImpl(options);
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public void compileImpl(TemplateCompileOptions options) throws IOException {
		
        File buildDir = getProject().getBuildDir();
        //need to make customizable...
        File groovySrcGen = new File(buildDir, "groovysrc");

        log.log(LogLevel.LIFECYCLE, "putting groovy scripts in: " + groovySrcGen);

		Charset encoding = Charset.forName(options.getEncoding());
		TemplateCompileConfig config = new TemplateCompileConfig(false);
		config.setFileEncoding(encoding);
		config.setPluginClient(true);
		config.setGroovySrcWriteDirectory(groovySrcGen);
		log.log(LogLevel.LIFECYCLE, "Custom tags: " + options.getCustomTags());
		config.setCustomTagsFromPlugin(options.getCustomTags());
    	
        LogLevel logLevel = getProject().getGradle().getStartParameter().getLogLevel();

        File destinationDir = getDestinationDirectory().getAsFile().get();
        log.log(LogLevel.LIFECYCLE, "Writing class files to destDir: " + destinationDir);
        File routeIdFile = new File(destinationDir, ProdConstants.ROUTE_META_FILE);
        if(routeIdFile.exists())
        	routeIdFile.delete();
        routeIdFile.createNewFile();
        
        log.log(LogLevel.LIFECYCLE, "routeId file: " + routeIdFile);
        
        FileCollection srcCollection = getSource();
        Set<File> files = srcCollection.getFiles();
        
        File firstFile = files.iterator().next();
        File baseDir = findBase(firstFile);
        try (FileOutputStream routeOut = new FileOutputStream(routeIdFile);
        		OutputStreamWriter write = new OutputStreamWriter(routeOut, encoding.name());
        		BufferedWriter bufWrite = new BufferedWriter(write)
        		) {
        	
        	Injector injector = Guice.createInjector(
        			new StubModule(), 
        			new DevTemplateModule(config, new PluginCompileCallback(destinationDir, bufWrite))
        			);
        	HtmlToJavaClassCompiler compiler = injector.getInstance(HtmlToJavaClassCompiler.class);
        	GroovyClassLoader cl = new GroovyClassLoader();
        	
	        for(File f : files) {
				String fullName = findFullName(baseDir, f);

	        	log.log(LogLevel.INFO, "file compile name={}, file={}", fullName, f);

	        	String source = readSource(f);
	        	
	        	compiler.compile(cl, fullName, source);
	        }
		}
        
		setDidWork(true);
	}
	
	private String readSource(File f) {
		try {
			try (FileInputStream in = new FileInputStream(f)) {
				return IOUtils.toString(in);
			}
		} catch(IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	private String findFullName(File baseDir, File f) {
		if(f.getName().contains("_"))
			throw new IllegalArgumentException("File name is invalid.  It cannot contain _ in the name="+f);
		
		String name = f.getName().replace(".", "_");
		File current = f.getParentFile();
		while(current != null && !baseDir.equals(current)) {
			name = current.getName()+"."+name;
			current = current.getParentFile();
		}
		
		if(!current.equals(baseDir))
			throw new IllegalStateException("Could not find basedir="+baseDir+" on file="+f);
		
		return name;
	}

	private File findBase(File firstFile) {
		File baseDir = recurse(firstFile.getParentFile());
		if(baseDir == null)
			throw new IllegalStateException("A baseDir was not found in src/main/java nor src/test/java for file="+firstFile.getAbsolutePath()+".  templatecompiler doesn't currently work work outside those base directories");
		
		return baseDir;
	}

	private File recurse(File firstFile) {
		if(firstFile.getParentFile() == null || firstFile.getParentFile().getParentFile() == null)
			return null;
		String src = firstFile.getParentFile().getParentFile().getName();
		String main = firstFile.getParentFile().getName();
		String java = firstFile.getName();
		
		if("src".equals(src) && "main".equals(main) && "java".equals(java))
			return firstFile;
		else if("src".equals(src) && "test".equals(main) && "java".equals(java))
			return firstFile;

		return recurse(firstFile.getParentFile());
	}

	private static class PluginCompileCallback implements CompileCallback {
		
		private File destinationDir;
		private BufferedWriter routeOut;

		public PluginCompileCallback(File destinationDir, BufferedWriter bufWrite) {
			this.destinationDir = destinationDir;
			this.routeOut = bufWrite;
		}

		public void compiledGroovyClass(GroovyClassLoader groovyCl, GroovyClass clazz) {
			String name = clazz.getName();
			String path = name.replace('.', '/');
			String fullPathName = path+".class";
			
			File target = new File(destinationDir, fullPathName);

			//Seems sometimes need to create and sometimes don't(it failed when I upgraded gradle when
			//I just used 'new File' so converted this back to the original createFile
			//I think we have been flip flopping but not sure what caused each issue yet.
			//I think this if exists, skip this piece should fix the flipflopping
			if(target.exists()) {
				//If you run ./gradle compileTemplates twice, the files will pre-exist already so we need to delete
				//the file before we create and write to it.
				log.log(LogLevel.INFO, "Deleting {}", target);
				if(!target.delete())
					throw new IllegalStateException("Could not delete file="+target+"  Cannot continue");
			}

			createFile(target);
			log.log(LogLevel.INFO, "Writing {}", target);
			
			try {
				try (FileOutputStream str = new FileOutputStream(target)) {
					IOUtils.write(clazz.getBytes(), str);
				}
			} catch(IOException e) {
				throw SneakyThrow.sneak(e);
			}
		}

		public File createFile(File target) {
			target.getParentFile().mkdirs();
			try {
				Files.createFile(target.toPath());
			} catch (IOException e) {
				throw SneakyThrow.sneak(e);
			}
			return target;
		}		
		
		@Override
		public void recordRouteId(String routeId, List<String> argNames, String sourceLocation) {
			String argStr = "";
			for(String s: argNames) {
				if(s.contains(":"))
					throw new RuntimeException("bug, argument should not contain : character.  argNames="+argNames+" arg="+s);
				
				argStr += ","+s;
			}

			if(routeId.contains(":"))
				throw new RuntimeException("bug, route should not contain : character");

			String encodedRouteId = URLEncoder.encode(routeId, StandardCharsets.UTF_8);
			String encodedArgs = URLEncoder.encode(argStr, StandardCharsets.UTF_8);
			String encodedSourceLocation = URLEncoder.encode(sourceLocation, StandardCharsets.UTF_8);

			try {
				routeOut.write(ProdConstants.ROUTE_TYPE+"/"+encodedSourceLocation+"/"+encodedRouteId+":"+encodedArgs+":dummy\n");
			} catch (IOException e) {
				throw SneakyThrow.sneak(e);
			}
		}

		@Override
		public void recordPath(String relativeUrlPath, String sourceLocation) {
			if(relativeUrlPath.contains(":"))
				throw new RuntimeException("bug, route should not contain : character");
			
			String encodedPath = URLEncoder.encode(relativeUrlPath, StandardCharsets.UTF_8);
			String encodedSourceLocation = URLEncoder.encode(sourceLocation, StandardCharsets.UTF_8);
			
			try {
				routeOut.write(ProdConstants.PATH_TYPE+"/"+encodedSourceLocation+"/"+encodedPath+"\n");
			} catch (IOException e) {
				throw SneakyThrow.sneak(e);
			}
		}
	}
	
}
