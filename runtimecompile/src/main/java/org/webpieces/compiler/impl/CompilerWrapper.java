package org.webpieces.compiler.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.VirtualFile;
import org.eclipse.jdt.internal.compiler.Compiler;

public class CompilerWrapper {

	private static final Logger log = LoggerFactory.getLogger(CompilerWrapper.class);
	
	//Contains true or false as to whether this is a package(true) or a class(false)
    CompileMetaMgr appClassMgr;
    Map<String, String> settings;

	private FileLookup fileLookup;

    /**
     * Try to guess the magic configuration options
     */
    public CompilerWrapper(CompileMetaMgr appClassMgr, FileLookup lookup) {
        this.appClassMgr = appClassMgr;
        this.fileLookup = lookup;
        this.settings = new HashMap<String, String>();
        this.settings.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
        this.settings.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
        this.settings.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);
        this.settings.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
        this.settings.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
        this.settings.put(CompilerOptions.OPTION_Encoding, "UTF-8");
        this.settings.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
        String javaVersion = CompilerOptions.VERSION_1_8;
        
        this.settings.put(CompilerOptions.OPTION_Source, javaVersion);
        this.settings.put(CompilerOptions.OPTION_TargetPlatform, javaVersion);
        this.settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
        this.settings.put(CompilerOptions.OPTION_Compliance, javaVersion);
    }

    /**
     * Something to compile
     */
    final class CompilationUnit implements ICompilationUnit {

        final private String clazzName;
        final private String fileName;
        final private char[] typeName;
        final private char[][] packageName;

        CompilationUnit(String pClazzName) {
            clazzName = pClazzName;
            if (pClazzName.contains("$")) {
                pClazzName = pClazzName.substring(0, pClazzName.indexOf("$"));
            }
            fileName = pClazzName.replace('.', '/') + ".java";
            int dot = pClazzName.lastIndexOf('.');
            if (dot > 0) {
                typeName = pClazzName.substring(dot + 1).toCharArray();
            } else {
                typeName = pClazzName.toCharArray();
            }
            StringTokenizer izer = new StringTokenizer(pClazzName, ".");
            packageName = new char[izer.countTokens() - 1][];
            for (int i = 0; i < packageName.length; i++) {
                packageName[i] = izer.nextToken().toCharArray();
            }
        }

        public char[] getFileName() {
            return fileName.toCharArray();
        }

        public char[] getContents() {
            return appClassMgr.getApplicationClass(clazzName).javaSource.toCharArray();
        }

        public char[] getMainTypeName() {
            return typeName;
        }

        public char[][] getPackageName() {
            return packageName;
        }

		@Override
		public boolean ignoreOptionalProblems() {
			return true;
		}
    }

    /**
     * Please compile this className
     */
    @SuppressWarnings("deprecation")
    public void compile(String[] classNames, ClassDefinitionLoader loader) {

        ICompilationUnit[] compilationUnits = new CompilationUnit[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            compilationUnits[i] = new CompilationUnit(classNames[i]);
        }
        IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.exitOnFirstError();
        IProblemFactory problemFactory = new DefaultProblemFactory(Locale.ENGLISH);

        /**
         * To find types ...
         */
        INameEnvironment nameEnvironment = new INameEnvironment() {

            public NameEnvironmentAnswer findType(final char[][] compoundTypeName) {
                final StringBuffer result = new StringBuffer();
                for (int i = 0; i < compoundTypeName.length; i++) {
                    if (i != 0) {
                        result.append('.');
                    }
                    result.append(compoundTypeName[i]);
                }
                return findType(result.toString());
            }

            public NameEnvironmentAnswer findType(final char[] typeName, final char[][] packageName) {
                final StringBuffer result = new StringBuffer();
                for (int i = 0; i < packageName.length; i++) {
                    result.append(packageName[i]);
                    result.append('.');
                }
                result.append(typeName);
                return findType(result.toString());
            }

            private NameEnvironmentAnswer findType(final String name) {
                try {

                    char[] fileName = name.toCharArray();
                    VirtualFile file = fileLookup.getJava(name);
                    CompileClassMeta applicationClass = appClassMgr.getOrCreateApplicationClass(name, file);

                    // ApplicationClass exists
                    if (applicationClass != null) {

                        if (applicationClass.javaByteCode != null) {
                        	//if class byte code exist because we are already compiled, return the compiled byte code
                            ClassFileReader classFileReader = new ClassFileReader(applicationClass.javaByteCode, fileName, true);
                            return new NameEnvironmentAnswer(classFileReader, null);
                        }
                        // Cascade compilation
                        ICompilationUnit compilationUnit = new CompilationUnit(name);
                        return new NameEnvironmentAnswer(compilationUnit, null);
                    }

                    // So it's a standard class
                    byte[] bytes = loader.getClassDefinition(name);
                    if (bytes != null) {
                        ClassFileReader classFileReader = new ClassFileReader(bytes, fileName, true);
                        return new NameEnvironmentAnswer(classFileReader, null);
                    }

                    // So it does not exist
                    return null;
                } catch (ClassFormatException e) {
                    // Something very very bad
                	throw new IllegalArgumentException(e);
                }
            }

            public boolean isPackage(char[][] parentPackageName, char[] packageName) {
                // Rebuild something usable
                StringBuilder sb = new StringBuilder();
                if (parentPackageName != null) {
                    for (char[] p : parentPackageName) {
                        sb.append(new String(p));
                        sb.append(".");
                    }
                }
                sb.append(new String(packageName));
                String name = sb.toString();
                if (appClassMgr.getApplicationClass(name) != null) {
                    return false;
                } else if (loader.getClassDefinition(name) != null) {
                	// Check if thera a .java or .class for this ressource
                	//We need to avoid this call since it reads in byte code
                	//If something causes this we throw an exception so we are told and can quickly fix this
                	//We could create a packageCache<String, Boolean> to avoid calling loader.getClassDefinition!!!
                	throw new IllegalStateException("this is reading in bytecode which I would like to avoid...do we hit this situation?");
//                    packagesCache.put(name, false);
//                    return false;
                }
                return true;
            }

            public void cleanup() {
            }
        };

        /**
         * Compilation result
         */
        ICompilerRequestor compilerRequestor = new ICompilerRequestor() {

            public void acceptResult(CompilationResult result) {
                // If error
                if (result.hasErrors()) {
                    for (IProblem problem: result.getErrors()) {
                        String className = new String(problem.getOriginatingFileName()).replace("/", ".");
                        className = className.substring(0, className.length() - 5);
                        String message = problem.getMessage();
                        if (problem.getID() == IProblem.CannotImportPackage) {
                            // Non sense !
                            message = problem.getArguments()[0] + " cannot be resolved";
                        }
                        throw new CompilationException(appClassMgr.getApplicationClass(className).javaFile, message, problem.getSourceLineNumber(), problem.getSourceStart(), problem.getSourceEnd());
                    }
                }
                // Something has been compiled
                ClassFile[] clazzFiles = result.getClassFiles();
                for (int i = 0; i < clazzFiles.length; i++) {
                    final ClassFile clazzFile = clazzFiles[i];
                    final char[][] compoundName = clazzFile.getCompoundName();
                    final StringBuffer clazzName = new StringBuffer();
                    for (int j = 0; j < compoundName.length; j++) {
                        if (j != 0) {
                            clazzName.append('.');
                        }
                        clazzName.append(compoundName[j]);
                    }

                    if (log.isTraceEnabled()) {
                        log.trace("Received Success eclipse Compiled result for=" + clazzName);
                    }

                    appClassMgr.getApplicationClass(clazzName.toString()).compiled(clazzFile.getBytes());
                }
            }
        };

        /**
         * The JDT compiler
         */
        Compiler jdtCompiler = new Compiler(nameEnvironment, policy, settings, compilerRequestor, problemFactory) {

            @Override
            protected void handleInternalException(Throwable e, CompilationUnitDeclaration ud, CompilationResult result) {
            }
        };

        // Go !
        jdtCompiler.compile(compilationUnits);

    }

	public CompileMetaMgr getAppClassMgr() {
		return appClassMgr;
	}
}
