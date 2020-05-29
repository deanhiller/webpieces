package org.webpieces.compiler.api;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.webpieces.util.file.VirtualFile;

public class CompileError {

	private VirtualFile javaFile;
	private Charset fileEncoding;
	private String message;
	private IProblem problem;

	public CompileError(VirtualFile javaFile, Charset fileEncoding, String message, IProblem problem) {
		this.javaFile = javaFile;
		this.fileEncoding = fileEncoding;
		this.message = message;
		this.problem = problem;
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

	public IProblem getProblem() {
		return problem;
	}

    public List<String> getSource() {
    	int start = problem.getSourceStart();
    	int end = problem.getSourceEnd();
    	
        String sourceCode = javaFile.contentAsString(fileEncoding);
        if(start != -1 && end != -1) {
            if(start == end) {
                sourceCode = sourceCode.substring(0, start + 1) + "↓" + sourceCode.substring(end + 1);
            } else {
                sourceCode = sourceCode.substring(0, start) + "\000" + sourceCode.substring(start, end + 1) + "\001" + sourceCode.substring(end + 1);
            }
        }
        return Arrays.asList(sourceCode.split("\n"));
    }
    
}
