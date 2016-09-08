package org.webpieces.gradle.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.templating.impl.HtmlToJavaClassCompiler;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
		Charset encoding = Charset.forName(options.getEncoding());
		TemplateCompileConfig config = new TemplateCompileConfig();
		config.setFileEncoding(encoding);
		config.setPluginClient(true);
		System.out.println("custom tags="+options.getCustomTags());
		config.setCustomTagsFromPlugin(options.getCustomTags());
    	Injector injector = Guice.createInjector(new DevTemplateModule(config));
    	HtmlToJavaClassCompiler compiler = injector.getInstance(HtmlToJavaClassCompiler.class);
    	
        LogLevel logLevel = getProject().getGradle().getStartParameter().getLogLevel();
        
        File destinationDir = getDestinationDir();
        System.out.println("destDir="+destinationDir);
        File routeIdFile = new File(destinationDir, "org.webpieces.routeId.txt");
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
	        for(File f : files) {
	        	System.out.println("file="+f);
	        	
	        	String fullName = findFullName(baseDir, f);
	        	System.out.println("name="+fullName);
	        	
	        	String source = readSource(f);
	        	
	        	compiler.compile(fullName, source, new PluginCompileCallback(destinationDir, bufWrite)); 
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
		
		public void compiledGroovyClass(GroovyClass clazz) {
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
		public void routeIdFound(String routeId, List<String> argNames, String sourceLocation) {
			String argStr = "";
			for(String s: argNames) {
				argStr += ","+s;
			}
			
			try {
				routeOut.write(routeId+":"+argStr+":"+sourceLocation+"\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
