package org.webpieces.compiler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompilationsException;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileError;
import org.webpieces.util.file.VirtualFile;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompilerWrapper {

    private static final Logger log = LoggerFactory.getLogger(CompilerWrapper.class);

    //Contains true or false as to whether this is a package(true) or a class(false)
    CompileMetaMgr appClassMgr;

    private final FileLookup fileLookup;
    private final CompileConfig config;
    private final String tempDirPath;

    /**
     * Try to guess the magic configuration options
     */
    public CompilerWrapper(CompileMetaMgr appClassMgr, FileLookup lookup, CompileConfig config) {
        this.appClassMgr = appClassMgr;
        this.fileLookup = lookup;
        this.config = config;

        File tempDir = new File("./build/tmp");
        deleteDir(tempDir);
        tempDir.mkdirs();
        this.tempDirPath = tempDir.getAbsolutePath();
        log.warn("CLASS OUT DIR " + this.tempDirPath);
    }

    void deleteDir(File folder) {
        File[] contents = folder.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        folder.delete();
    }

    /**
     * Please compile this className
     */
    public void compile(String[] classNames, ClassDefinitionLoader loader) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        WebpiecesFileManager manager = new WebpiecesFileManager(compiler.getStandardFileManager(null, null, null));

        List<String> options = Stream.of("-parameters",
                "-d", tempDirPath,
                "-classpath", tempDirPath + ":" + System.getProperty("java.class.path"))
                .collect(Collectors.toList());

        List<JavaFileObject> files = new ArrayList<>();

        for (String name : classNames) {

            VirtualFile file = fileLookup.getJava(name);
            String className = name;
            if (name.contains("$")) {
                className = className.substring(0, name.indexOf("$"));
            }
            files.add(new MemoryJavaFileObject(className, file.contentAsString(config.getFileEncoding())));
        }
        List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
//        compiler.getTask(null, null, errors::add, options, null, files).call();
        boolean result = compiler.getTask(null, manager, errors::add, options, null, files).call();

        if (log.isTraceEnabled())
            log.trace("Received Success eclipse Compiled result for=" + classNames[0]);

        if (result) {
            for (int i = 0; i < classNames.length; i++) {
                VirtualFile file = fileLookup.getJava(classNames[i]);
                CompileClassMeta appClass = appClassMgr.getOrCreateApplicationClass(classNames[i], file);

                appClass.compiled(manager.getBytes(classNames[i]));
            }
        } else {
            StringBuilder fullMessage = new StringBuilder("Could not compile files!!!  Each reason is below\n");
            List<CompileError> compileErrors = new ArrayList<>();
            for (Diagnostic<? extends JavaFileObject> problem : errors) {
                String className = problem.getSource().getName().replaceFirst("/", "").replace("/", ".");
                className = className.substring(0, className.length() - 5);

                CompileClassMeta applicationClass = appClassMgr.getApplicationClass(className);
                VirtualFile javaFile = applicationClass.javaFile;

                fullMessage.append("\n\nClass could not compile:").append(className).append("\nFile=").append(javaFile.getCanonicalPath()).append("\n");

                String message = "Problem with class:" + className + "!  Issue: " + problem.getMessage(Locale.ENGLISH) + "\n";
                fullMessage.append("\nCompiler Issue: ").append(problem.getMessage(Locale.ENGLISH)).append("\n\n");

                CompileError compileError = new CompileError(javaFile, className, config.getFileEncoding(), message, problem);
                for (String line : compileError.getBadSourceLine()) {
                    fullMessage.append("    ").append(line).append("\n");
                }
                fullMessage.append("\n");

                compileErrors.add(compileError);
            }

            throw new CompilationsException(compileErrors, fullMessage.toString());
        }
    }

    public CompileMetaMgr getAppClassMgr() {
        return appClassMgr;
    }
}
