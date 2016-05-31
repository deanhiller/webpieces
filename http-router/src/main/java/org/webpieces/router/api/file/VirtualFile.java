package org.webpieces.router.api.file;

import java.io.InputStream;

public interface VirtualFile {

	public long lastModified();
	
	public InputStream openInputStream();

	public String getName();
	
}
