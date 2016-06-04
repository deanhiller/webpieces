package org.webpieces.compiler.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class VirtualFileImpl implements VirtualFile {

	private File file;

	public VirtualFileImpl(File file) {
		this.file = file;
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public List<VirtualFile> list() {
		File[] files = file.listFiles();
		List<VirtualFile> theFiles = new ArrayList<>();
		for(File f : files) {
			theFiles.add(new VirtualFileImpl(f));
		}
		return theFiles;
	}

	@Override
	public String contentAsString() {
		try (InputStream in = inputstream()) {
			return IOUtils.toString(in);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public VirtualFile child(String fileName) {
		File child = new File(file, fileName);
		return new VirtualFileImpl(child);
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public InputStream inputstream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "[file=" + file + "]";
	}

	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public URL toURL() {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public boolean mkdirs() {
		return file.mkdirs();
	}

	@Override
	public OutputStream openOutputStream() {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean delete() {
		return file.delete();
	}

}
