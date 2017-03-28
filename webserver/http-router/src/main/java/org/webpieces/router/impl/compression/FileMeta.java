package org.webpieces.router.impl.compression;

public class FileMeta {

	private String hash;

	public FileMeta() {
		this(null);
	}
	
	public FileMeta(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

}
