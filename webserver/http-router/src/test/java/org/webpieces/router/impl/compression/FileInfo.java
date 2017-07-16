package org.webpieces.router.impl.compression;

import org.webpieces.util.file.VirtualFile;

public class FileInfo {

	public String urlPath;
	public VirtualFile src;

	public FileInfo(String urlPath, VirtualFile src) {
		this.urlPath = urlPath;
		this.src = src;
	}

}
