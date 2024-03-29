package org.webpieces.compiler.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.webpieces.util.file.VirtualFile;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompileError {

    private VirtualFile javaFile;
    private String className;
    private Charset fileEncoding;
    private String message;
    private Diagnostic<? extends JavaFileObject> diagnostic;

    public CompileError(VirtualFile javaFile, String className, Charset fileEncoding, String message, Diagnostic<? extends JavaFileObject> diagnostic) {
        this.javaFile = javaFile;
        this.className = className;
        this.fileEncoding = fileEncoding;
        this.message = message;
        this.diagnostic = diagnostic;
    }

    public VirtualFile getJavaFile() {
        return javaFile;
    }

    public Charset getFileEncoding() {
        return fileEncoding;
    }

    public String getMessage() {
        return message;
    }

    public Diagnostic<? extends JavaFileObject> getDiagnostic() {
        return diagnostic;
    }

    public List<String> getBadSourceLine() {
        int lineNumberWithProblem = 0;
        int start = 0;
        int end = 0;


        lineNumberWithProblem = (int) diagnostic.getLineNumber();
        start = (int) diagnostic.getStartPosition();
        end = (int) diagnostic.getEndPosition();


        String sourceCode = javaFile.contentAsString(fileEncoding);
        if (start != -1 && end != -1) {
            sourceCode = sourceCode.substring(0, start) + "\000" + sourceCode.substring(start, end + 1) + "\001" + sourceCode.substring(end + 1);
        }

        List<String> asList = Arrays.asList(sourceCode.split("\n"));

        //let's return 3 lines
        List<String> lines = new ArrayList<String>();
        if (lineNumberWithProblem - 2 >= 0) {
            String line1 = asList.get(lineNumberWithProblem - 2);
            lines.add("linenumber " + (lineNumberWithProblem - 1) + ":" + line1);
        }

        if (lineNumberWithProblem - 1 >= 0) {
            String line2 = asList.get(lineNumberWithProblem - 1);
            lines.add("linenumber " + (lineNumberWithProblem) + ":" + line2);
        }

        if (lineNumberWithProblem + 1 < asList.size()) {
            String line3 = asList.get(lineNumberWithProblem + 1);
            lines.add("linenumber " + (lineNumberWithProblem + 1) + ":" + line3);
        }
        return lines;
    }

    public String getClassName() {
        return className;
    }

}
