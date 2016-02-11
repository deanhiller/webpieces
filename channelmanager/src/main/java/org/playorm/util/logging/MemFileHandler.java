package org.playorm.util.logging;

import java.io.IOException;
import java.util.logging.FileHandler;

public class MemFileHandler extends FileHandler {

	public MemFileHandler() throws IOException {
	}

	public MemFileHandler(String pattern) throws IOException {
		super(pattern);
	}

	public MemFileHandler(String pattern, boolean append) throws IOException {
		super(pattern, append);
	}

	public MemFileHandler(String pattern, int limit, int count)
			throws IOException {
		super(pattern, limit, count);
	}

	public MemFileHandler(String pattern, int limit, int count, boolean append)
			throws IOException {
		super(pattern, limit, count, append);
	}

}
