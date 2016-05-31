package org.webpieces.router.api.file;

import java.io.InputStream;

public class VirtualFileInputStream implements VirtualFile {

	private InputStream in;
	private String name;

	public VirtualFileInputStream(InputStream in, String name) {
		this.in = in;
		this.name = name;
	}

	@Override
	public long lastModified() {
		return 0;
	}

	@Override
	public InputStream openInputStream() {
		return in;
	}

	@Override
	public String getName() {
		return name;
	}

}
