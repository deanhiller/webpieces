package org.webpieces.gradle.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.templatingdev.api.CompileCallback;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.StubModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.templatingdev.impl.HtmlToJavaClassCompiler;
import org.webpieces.util.net.URLEncoder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import groovy.lang.GroovyClassLoader;

public class TemplateCompilerTask extends AbstractCompile {

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
			throw new RuntimeException(e);
		}
	}

	public void compileImpl(TemplateCompileOptions options) throws IOException {
		
        File buildDir = getProject().getBuildDir();
        //need to make customizable...
        File groovySrcGen = new File(buildDir, "groovysrc"); 
        System.out.println("groovy src directory="+groovySrcGen);

		Charset encoding = Charset.forName(options.getEncoding());
		TemplateCompileConfig config = new TemplateCompileConfig(false);
		config.setFileEncoding(encoding);
		config.setPluginClient(true);
		config.setGroovySrcWriteDirectory(groovySrcGen);
		System.out.println("custom tags="+options.getCustomTags());
		config.setCustomTagsFromPlugin(options.getCustomTags());
    	
        LogLevel logLevel = getProject().getGradle().getStartParameter().getLogLevel();
        
        File destinationDir = getDestinationDir();
        System.out.println("destDir="+destinationDir);
        File routeIdFile = new File(destinationDir, ProdTemplateModule.ROUTE_META_FILE);
        if(routeIdFile.exists())
        	routeIdFile.delete();
        routeIdFile.createNewFile();
        
        System.out.println("routeId.txt file="+routeIdFile.getAbsolutePath());
        
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
	        	System.out.println("file="+f);
	        	
	        	String fullName = findFullName(baseDir, f);
	        	System.out.println("name="+fullName);
	        	
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
			throw new RuntimeException(e);
		}
	}
	
	private String findFullName(File baseDir, File f) {
		if(f.getName().contains("_"))
			throw new IllegalArgumentException("File name is invalid.  It cannot contain _ in the name="+f.getAbsolutePath());
		
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
			throw new IllegalStateException("baseDir of src/main/java could not be found.  We currently dont' work outside src/main/java yet");
		
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
			File f = new File(destinationDir, fullPathName);
			//File f = createFile(destinationDir, name);
			System.out.println("file write to="+f);
			
			try {
				try (FileOutputStream str = new FileOutputStream(f)) {
					IOUtils.write(clazz.getBytes(), str);
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
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
				routeOut.write(ProdTemplateModule.ROUTE_TYPE+"/"+encodedSourceLocation+"/"+encodedRouteId+":"+encodedArgs+":dummy\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void recordPath(String relativeUrlPath, String sourceLocation) {
			if(relativeUrlPath.contains(":"))
				throw new RuntimeException("bug, route should not contain : character");
			
			String encodedPath = URLEncoder.encode(relativeUrlPath, StandardCharsets.UTF_8);
			String encodedSourceLocation = URLEncoder.encode(sourceLocation, StandardCharsets.UTF_8);
			
			try {
				routeOut.write(ProdTemplateModule.PATH_TYPE+"/"+encodedSourceLocation+"/"+encodedPath+"\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
