package org.webpieces.router.impl.compression;

import java.io.File;

public class FileInfo {

	public String urlPath;
	public File src;

	public FileInfo(String urlPath, File src) {
		this.urlPath = urlPath;
		this.src = src;
	}

}
