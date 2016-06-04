package org.webpieces.compiler.impl;

import java.util.List;

import org.webpieces.util.file.VirtualFile;

public class FileLookup {

    private List<VirtualFile> javaPath;
	private CompileMetaMgr appClassMgr;

	public FileLookup(CompileMetaMgr appClassMgr, List<VirtualFile> javaPath) {
		this.appClassMgr = appClassMgr;
    	this.javaPath = javaPath;
	}

	// ~~ Utils
    /**
     * Retrieve the corresponding source file for a given class name.
     * It handles innerClass too !
     * @param name The fully qualified class name 
     * @return The virtualFile if found
     */
    public VirtualFile getJava(String name) {
        String fileName = name;
        if (fileName.contains("$")) {
            fileName = fileName.substring(0, fileName.indexOf("$"));
        }
        fileName = fileName.replace(".", "/") + ".java";
        for (VirtualFile path : javaPath) {
            VirtualFile javaFile = path.child(fileName);
            if (javaFile.exists()) {
                return javaFile;
            }
        }
        return null;
    }

	   // ~~~ Intern
    void scanFilesWithFilter(String filterToPackage) {
        for (VirtualFile virtualFile : javaPath) {
            scanFilesWithFilter(virtualFile, filterToPackage);
        }
    }

    void scanFilesWithFilter(VirtualFile path, String filterToPackage) {
        for (VirtualFile virtualFile : path.list()) {
            scanForJavaFiles("", virtualFile, filterToPackage);
        }
    }

    void scanForJavaFiles(String packageName, VirtualFile current, String filterToPackage) {
        if (!current.isDirectory()) {
        	if(filterToPackage != null && !packageName.startsWith(filterToPackage))
        		return;
        	else if (current.getName().endsWith(".java") && !current.getName().startsWith(".")) {
                String classname = packageName + current.getName().substring(0, current.getName().length() - 5);
                appClassMgr.getOrCreateApplicationClass(classname, current);
            }
        } else {
            for (VirtualFile virtualFile : current.list()) {
                scanForJavaFiles(packageName + current.getName() + ".", virtualFile, filterToPackage);
            }
        }
    }
}
