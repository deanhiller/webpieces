package org.webpieces.templating.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.webpieces.util.file.VirtualFile;

public class VirtualFileClasspath implements VirtualFile {

	
	private URL resource;
	private String path;

	public VirtualFileClasspath(String path, ClassLoader classLoader) {
		resource = classLoader.getResource(path);
		this.path = path;
	}

	@Override
	public boolean isDirectory() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public List<VirtualFile> list() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public String contentAsString() {
		return null;
	}

	@Override
	public long lastModified() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public VirtualFile child(String fileName) {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public boolean exists() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public InputStream openInputStream() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public String getAbsolutePath() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public URL toURL() {
		return resource;
	}

	@Override
	public long length() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public boolean mkdirs() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public OutputStream openOutputStream() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public String getCanonicalPath() {
		return getAbsolutePath();
	}

}
