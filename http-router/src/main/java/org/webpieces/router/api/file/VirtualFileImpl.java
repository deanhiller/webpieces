package org.webpieces.router.api.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class VirtualFileImpl implements VirtualFile {

	private File file;

	public VirtualFileImpl(File f) {
		this.file = f;
	}
	
	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public InputStream openInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File="+file.getAbsolutePath()+" was not found", e);
		}
	}

	@Override
	public String getName() {
		return file.getAbsolutePath();
	}

	@Override
	public String toString() {
		return "[file=" + file + "]";
	}
	
}
