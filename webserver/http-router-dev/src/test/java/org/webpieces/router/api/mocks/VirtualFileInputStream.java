package org.webpieces.router.api.mocks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.webpieces.util.file.VirtualFile;

public class VirtualFileInputStream implements VirtualFile {

	private String name;
	private byte[] data;

	public VirtualFileInputStream( byte[] data, String name) {
		this.data = data;
		this.name = name;
	}

	@Override
	public long lastModified() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public List<VirtualFile> list() {
		throw new IllegalStateException("This is a file, not a directory");
	}

	@Override
	public String contentAsString() {
		return new String(data);
	}

	@Override
	public VirtualFile child(String fileName) {
		throw new IllegalStateException("This is a file, not a directory");
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public InputStream openInputStream() {
		return new ByteArrayInputStream(data);
	}

	@Override
	public String getAbsolutePath() {
		return name;
	}

	@Override
	public URL toURL() {
		throw new IllegalStateException("no url");
	}

	@Override
	public long length() {
		return data.length;
	}

	@Override
	public boolean mkdirs() {
		throw new IllegalStateException("This is a file, not a directory");
	}

	@Override
	public OutputStream openOutputStream() {
		throw new IllegalStateException("This is read only");
	}

	@Override
	public boolean delete() {
		throw new IllegalStateException("This is read only");
	}

	@Override
	public String getCanonicalPath() {
		return getAbsolutePath();
	}

}
