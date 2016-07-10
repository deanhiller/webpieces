package org.webpieces.gradle.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.GroovyClass;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.impl.HtmlToJavaClassCompiler;

import com.google.inject.Guice;
import com.google.inject.Injector;

import groovy.lang.Closure;

public class TemplateCompilerTask extends AbstractCompile {
	private static final Logger log = LoggerFactory.getLogger(TemplateCompilerTask.class);

	private static HtmlToJavaClassCompiler compiler;

    private TemplateCompileOptions options = new TemplateCompileOptions();

    static {
    	Injector injector = Guice.createInjector(new DevTemplateModule());
    	compiler = injector.getInstance(HtmlToJavaClassCompiler.class);
    }
    
    @Nested
    public TemplateCompileOptions getOptions() {
        return options;
    }

    public void options(Action<TemplateCompileOptions> action) {
        action.execute(getOptions());
    }

    public void options(Closure<?> closure) {
        getProject().configure(getOptions(), closure);
    }
    
	@TaskAction
	public void compile() {
        LogLevel logLevel = getProject().getGradle().getStartParameter().getLogLevel();
        
        File destinationDir = getDestinationDir();
        System.out.println("destDir="+destinationDir);
        
        FileCollection srcCollection = getSource();
        Set<File> files = srcCollection.getFiles();
        
        File firstFile = files.iterator().next();
        File baseDir = findBase(firstFile);
        
        for(File f : files) {
        	System.out.println("file="+f);
        	
        	String fullName = findFullName(baseDir, f);
        	System.out.println("name="+fullName);
        	
        	String source = readSource(f);
        	
        	compiler.compile(fullName, source, clazz -> compiledGroovyClass(destinationDir, clazz));
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

	private Object compiledGroovyClass(File destinationDir, GroovyClass clazz) {
		String name = clazz.getName();
		File f = new File(destinationDir, name);
		System.out.println("file write to="+f);
		
		try {
			try (FileOutputStream str = new FileOutputStream(f)) {
				IOUtils.write(clazz.getBytes(), str);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return null;
		
	}
}
