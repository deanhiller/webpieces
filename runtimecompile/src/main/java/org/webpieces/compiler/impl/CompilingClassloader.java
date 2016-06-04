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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.VirtualFile;

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
    
    private final FileStateHashCreator classStateHashCreator = new FileStateHashCreator();
    /**
     * Used to track change of the application sources path
     */
    private final int pathHash;
    
    public CompilingClassloader(CompileConfig config, CompilerWrapper compiler) {
        super(CompilingClassloader.class.getClassLoader());
    	this.config = config;
    	this.byteCodeCache = new BytecodeCache(config);
    	this.compiler = compiler;
    	this.appClassMgr = compiler.getAppClassMgr();
    	
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
        	throw new RuntimeException(e);
            //throw new UnexpectedException(e);
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

        Class<?> maybeAlreadyLoaded = super.findLoadedClass(name);
        if(maybeAlreadyLoaded != null) {
            return maybeAlreadyLoaded;
        }

        long start = System.currentTimeMillis();
        CompileClassMeta applicationClass = appClassMgr.getApplicationClass(name);
        
        if(applicationClass == null) {
        	//the parent classloader is responsible for this class
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
            
            if(log.isTraceEnabled()) {
            	long time = System.currentTimeMillis() - start;
            	log.trace(time+"ms to load class "+name+" from cache");
            }

            return applicationClass.javaClass;
        }
        
        
        byte[] byteCode = applicationClass.javaByteCode;
        if(byteCode == null)
        	byteCode = applicationClass.compile(compiler, this);
        
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
        	log.trace(time+"ms to load class "+name);
        }

        return applicationClass.javaClass;
    }

    /**
     * Search for the byte code of the given class.
     */
    public byte[] getClassDefinition(String name) {
        name = name.replace(".", "/") + ".class";
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
        	throw new IllegalArgumentException(e);
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
                return res.inputstream();
            }
        }
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

            public boolean hasMoreElements() {
                return it.hasNext();
            }

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
        for (CompileClassMeta applicationClass : modifiedWithDependencies) {
            if (applicationClass.compile(compiler, this) == null) {
                appClassMgr.classes.remove(applicationClass.name);
                throw new IllegalStateException("In what case can this ever happen in?"); 
            } else {
                byteCodeCache.cacheBytecode(applicationClass.javaByteCode, applicationClass.name, applicationClass.javaSource);
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

    @Override
    public String toString() {
        return "[CompilingClassLoader " + appClassMgr+"]";
    }

}
