package org.webpieces.compiler.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.ClassFileNotFoundException;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.file.VirtualFile;

import org.webpieces.util.compiling.GroovyCompiling;

/*
 * Compile classes that need compiling to load them
 */
public class CompilingClassloader extends ClassLoader implements ClassDefinitionLoader {

	private static final Logger log = LoggerFactory.getLogger(CompilingClassloader.class);

    /**
     * This protection domain applies to all loaded classes.
     */
    private final ProtectionDomain protectionDomain;

	private final CompileConfig config;

	private final BytecodeCache byteCodeCache;

	private final CompilerWrapper compiler;

	private final CompileMetaMgr appClassMgr;
    
    private final FileStateHashCreator classStateHashCreator;
    /**
     * Used to track change of the application sources path
     */
    private final int pathHash;

	private FileLookup fileLookup;

	private final int instance;
    
	private static int lastUsedInstanceNumber = 0;
	
    public CompilingClassloader(CompileConfig config, CompilerWrapper compiler, FileLookup fileLookup) {
        super(CompilingClassloader.class.getClassLoader());
    	this.config = config;
    	this.byteCodeCache = new BytecodeCache(config);
    	this.compiler = compiler;
    	this.appClassMgr = compiler.getAppClassMgr();
    	this.fileLookup = fileLookup;
    	this.classStateHashCreator = new FileStateHashCreator(config);
    	
    	VirtualFile pathForCodeSrc = config.getJavaPath().get(0);
        // Clean the existing classes
        for (CompileClassMeta applicationClass : appClassMgr.all()) {
            applicationClass.uncompile();
        }
        this.pathHash = classStateHashCreator.computePathHash(config.getJavaPath());
        
        try {
            CodeSource codeSource = new CodeSource(new URL("file:" + pathForCodeSrc.getAbsolutePath()), (Certificate[]) null);
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            protectionDomain = new ProtectionDomain(codeSource, permissions);
        } catch (MalformedURLException e) {
            throw SneakyThrow.sneak(e);
        }
        
        synchronized(CompilingClassloader.class) {
        	this.instance = lastUsedInstanceNumber;
        	lastUsedInstanceNumber++;
        }
    }

    /**
     * You know ...
     */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // First check if it's an application Class
        Class<?> applicationClass = loadApplicationClass(name);
        if (applicationClass != null) {
            if (resolve) {
                resolveClass(applicationClass);
            }
            return applicationClass;
        }

        // Delegate to the classic classloader
        return super.loadClass(name, resolve);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~
    public Class<?> loadApplicationClass(String name) {
    	//We must intern the name so it becomes the SAME EXACT lock in case the
    	//same name is passed in.  ie. name1.intern() == name2.intern() so while
    	//you can use name.equals(name2), you could also do name1.intern() == name2.intern() as
    	//they intern returns the same object
    	synchronized (name.intern()) {
			return loadApplicationClassImpl(name);
		}
    }
    
    public Class<?> loadApplicationClassImpl(String name) {
        Class<?> maybeAlreadyLoaded = super.findLoadedClass(name);
        if(maybeAlreadyLoaded != null) {
            return maybeAlreadyLoaded;
        }

        long start = System.currentTimeMillis();
        CompileClassMeta applicationClass = appClassMgr.getApplicationClass(name);
        
        //For anonymous classes...
        if(applicationClass == null) {
        	VirtualFile file = fileLookup.getJava(name);
        	//Hibernate has 
        	if(!name.endsWith("_") && file == null && config.getFailIfNotInSourcePaths() != null) {
        		if(name.endsWith("_") || name.contains("$HibernateProxy$")) {
        			//Avoid bytebuddy classes that hibernate creates
        			//AND MetaModel JPA classes ending in _
        		} else if(name.startsWith(config.getFailIfNotInSourcePaths())) {
        	        String fileName = name.replace(".", "/") + ".java";
        	        
        	        //Groovy asks for weird paths sometimes so we have to avoid this during groovy compiles
        	        if(!GroovyCompiling.isCompilingGroovy())
        	        	throw new ClassFileNotFoundException("Could not find java file="+fileName+" in paths="+config.getJavaPath());
        		}
        		
        	}
        		
        	applicationClass = appClassMgr.getOrCreateApplicationClass(name, file);
        }
        
        //if still null...
        if(applicationClass == null) {
        	//the parent classloader is responsible for this class as it is not on our compile path
        	return null;
        }
        
        if (applicationClass.isDefinable()) {
            return applicationClass.javaClass;
        }
        byte[] bc = byteCodeCache.getBytecode(name, applicationClass.javaSource);

        if(log.isTraceEnabled())
			log.trace("Loading class for "+name);

        if (bc != null) {
        	applicationClass.javaByteCode = bc;
            applicationClass.javaClass = defineClass(applicationClass.name, applicationClass.javaByteCode, 0, applicationClass.javaByteCode.length, protectionDomain);
            resolveClass(applicationClass.javaClass);
            
            if(log.isDebugEnabled()) {
            	long time = System.currentTimeMillis() - start;
            	if(log.isTraceEnabled())
					log.trace(time+"ms to load class "+name+" from cache");
            }

            return applicationClass.javaClass;
        }
        
        
        byte[] byteCode = applicationClass.javaByteCode;
        if(byteCode == null) {
            compiler.compile(new String[]{applicationClass.name}, this);
            byteCode = applicationClass.javaByteCode;
        }
        
        if(byteCode == null) {
        	throw new IllegalStateException("Bug, should not get here.  we could not compile and no exception thrown(should have had upstream fail fast exception");
        	//previously removed class and returned
//            	appClassMgr.classes.remove(name);
//            	return;
        }
        
        applicationClass.javaClass = defineClass(applicationClass.name, applicationClass.javaByteCode, 0, applicationClass.javaByteCode.length, protectionDomain);
        byteCodeCache.cacheBytecode(applicationClass.javaByteCode, name, applicationClass.javaSource);
        resolveClass(applicationClass.javaClass);

        if(log.isTraceEnabled()) {
        	long time = System.currentTimeMillis() - start;
        	if(log.isTraceEnabled())
				log.trace(time+"ms to load class "+name);
        }

        return applicationClass.javaClass;
    }

    //hack to catch an issue with 
    //Caused by: java.util.zip.ZipException: ZipFile invalid LOC header (bad signature)
    //so we can capture more info
    
    private ThreadLocal<String> foundInDir = new ThreadLocal<String>();
    
    /**
     * Search for the byte code of the given class.
     */
    @Override
    public byte[] getClassDefinition(String name) {
        name = name.replace(".", "/") + ".class";
    
        foundInDir.set("never.seen");
        
        try (InputStream is = getResourceAsStream(name)) {
	        if (is == null) {
	            return null;
	        }
        
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (Exception e) {
        	throw new RuntimeException("Failure trying to get class definition="+name+" in the path="+foundInDir.get(), e);
        } finally {
        	foundInDir.set(null);
        }
    }

    /**
     * You know ...
     */
    @Override
    public InputStream getResourceAsStream(String name) {
		for (VirtualFile vf : config.getJavaPath()) {
            VirtualFile res = vf.child(name);
            if (res != null && res.exists()) {
            	foundInDir.set(res.getCanonicalPath());
                return res.openInputStream();
            }
        }
		foundInDir.set("notFoundInOurPaths");
        return super.getResourceAsStream(name);
	}
    
    

    /**
     * You know ...
     */
    @Override
    public URL getResource(String name) {
        for (VirtualFile vf : config.getJavaPath()) {
            VirtualFile res = vf.child(name);
            if (res != null && res.exists()) {
            	return res.toURL();
            }
        }
        return super.getResource(name);
    }

    /**
     * You know ...
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<URL>();
        for (VirtualFile vf : config.getJavaPath()) {
            VirtualFile res = vf.child(name);
            if (res != null && res.exists()) {
            	urls.add(res.toURL());
            }
        }
        Enumeration<URL> parent = super.getResources(name);
        while (parent.hasMoreElements()) {
            URL next = parent.nextElement();
            if (!urls.contains(next)) {
                urls.add(next);
            }
        }
        final Iterator<URL> it = urls.iterator();
        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    /**
     * Detect Java changes
     */
    public boolean isNeedToReloadJavaFiles() {
        // Now check for file modification
        List<CompileClassMeta> modifieds = new ArrayList<CompileClassMeta>();
        for (CompileClassMeta applicationClass : appClassMgr.all()) {
            if (applicationClass.javaFile.lastModified() > applicationClass.timestamp) {
                applicationClass.refresh();
                modifieds.add(applicationClass);
            }
        }
        Set<CompileClassMeta> modifiedWithDependencies = new HashSet<CompileClassMeta>();
        modifiedWithDependencies.addAll(modifieds);
        List<ClassDefinition> newDefinitions = new ArrayList<ClassDefinition>();
        List<String> classNames = modifiedWithDependencies
                .stream()
                .map(compileClassMeta -> compileClassMeta.name)
                .collect(Collectors.toList());

        classNames.addAll(getNewFiles());

        if (classNames.size() != 0) {
            compiler.compile(classNames.toArray(new String[0]), this);
        }
        for (CompileClassMeta applicationClass : modifiedWithDependencies) {
            if (applicationClass.javaByteCode == null) {
                appClassMgr.classes.remove(applicationClass.name);
                //We are looking for a reproducible scenario here so we can write a test!!!
                //how is the compile call return null...
                //I saw this once waiting overnight with the server running all night??  how did that cause this?
           
                throw new IllegalStateException("In what case can this ever happen in?(we need a reproducible case). In the meantime, restarting your Dev server fixes this."); 
            } else {
                byteCodeCache.cacheBytecode(applicationClass.javaByteCode, applicationClass.name, applicationClass.javaSource);
                //in rare case where outerclass is outside scope of compiling, but inner static class can be recompiled
                if(applicationClass.javaClass == null) {
                	loadApplicationClass(applicationClass.name);
                }
                
                newDefinitions.add(new ClassDefinition(applicationClass.javaClass, applicationClass.javaByteCode));
            }
        }
        if (newDefinitions.size() > 0) {
            //Cache.clear();
            if (HotswapAgent.enabled) {
                try {
                    HotswapAgent.reload(newDefinitions.toArray(new ClassDefinition[newDefinitions.size()]));
                } catch (Throwable e) {
                	return true;
                }
            } else {
            	return true;
            }
        }

        // Now check if there is new classes or removed classes
        int hash = classStateHashCreator.computePathHash(config.getJavaPath());
        if (hash != this.pathHash) {
            // Remove class for deleted files !!
            for (CompileClassMeta applicationClass : appClassMgr.all()) {
                if (!applicationClass.javaFile.exists()) {
                	appClassMgr.classes.remove(applicationClass.name);
                }
                if (applicationClass.name.contains("$")) {
                	appClassMgr.classes.remove(applicationClass.name);
                    // Ok we have to remove all classes from the same file ...
                    VirtualFile vf = applicationClass.javaFile;
                    for (CompileClassMeta ac : appClassMgr.all()) {
                        if (ac.javaFile.equals(vf)) {
                        	appClassMgr.classes.remove(ac.name);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private List<String> getNewFiles() {
        List<String> classNames = new ArrayList<>();
        for (VirtualFile virtualFile : config.getJavaPath()) {
            List<VirtualFile> files = scan(virtualFile);
            for (VirtualFile file : files) {
                String classPath = file.getAbsolutePath().replaceFirst(virtualFile.getAbsolutePath(), "")
                        .replaceFirst("/", "")
                        .replace(".java", "")
                        .replace("/", ".");
                if (appClassMgr.getApplicationClass(classPath) == null) {
                    classNames.add(classPath);
                }
            }
        }
        return classNames;
    }

    private List<VirtualFile> scan(VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".java")) {
                return Collections.singletonList(current);
            }
        } else if (!current.getName().startsWith(".")) {
            return current.list()
                    .stream()
                    .flatMap(virtualFile -> scan(virtualFile).stream())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "[CompilingClassLoader " + appClassMgr+", instance="+instance+"]";
    }

}
