package org.webpieces.compiler.impl;

import java.util.Arrays;
import java.util.List;

import org.webpieces.util.file.VirtualFile;

/**
 * A java compilation error
 */
public class CompilationException extends RuntimeException {

	private static final long serialVersionUID = -1733961137449071368L;
	private String problem;
    private VirtualFile source;
    private Integer line;
    private Integer start;
    private Integer end;

    public CompilationException(String problem) {
        super(problem);
        this.problem = problem;
    }

    public CompilationException(VirtualFile source, String problem, int line, int start, int end) {
        super(problem);
        this.problem = problem;
        this.line = line;
        this.source = source;
        this.start = start;
        this.end = end;
    }

    public String getErrorTitle() {
        return String.format("Compilation error");
    }

    public String getErrorDescription() {
        return String.format("The file <strong>%s</strong> could not be compiled.\nError raised is : <strong>%s</strong>", isSourceAvailable() ? source.getAbsolutePath() : "", problem.toString().replace("<", "&lt;"));
    }
    
    @Override
    public String getMessage() {
        return problem;
    }

    public List<String> getSource() {
        String sourceCode = source.contentAsString();
        if(start != -1 && end != -1) {
            if(start.equals(end)) {
                sourceCode = sourceCode.substring(0, start + 1) + "↓" + sourceCode.substring(end + 1);
            } else {
                sourceCode = sourceCode.substring(0, start) + "\000" + sourceCode.substring(start, end + 1) + "\001" + sourceCode.substring(end + 1);
            }
        }
        return Arrays.asList(sourceCode.split("\n"));
    }

    public Integer getLineNumber() {
        return line;
    }

    public String getSourceFile() {
        return source.getAbsolutePath();
    }

    public Integer getSourceStart() {
        return start;
    }

    public Integer getSourceEnd() {
        return end;
    }

    public boolean isSourceAvailable() {
        return source != null && line != null;
    }
    
}
