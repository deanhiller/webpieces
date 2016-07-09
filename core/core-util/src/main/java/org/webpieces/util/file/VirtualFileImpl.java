package org.webpieces.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class VirtualFileImpl implements VirtualFile {

	private File file;
	private Charset charset;

	public VirtualFileImpl(File file) {
		this(file, Charset.defaultCharset());
	}
	
	public VirtualFileImpl(File file, Charset charset) {
		this.file = file;
		this.charset = charset;
	}

	public VirtualFileImpl(String fileName) {
		this(new File(fileName), Charset.defaultCharset());
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
			theFiles.add(new VirtualFileImpl(f, charset));
		}
		return theFiles;
	}

	@Override
	public String contentAsString() {
		try {
			return new String(Files.readAllBytes(file.toPath()), charset);
		} catch (IOException e) {
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
		return new VirtualFileImpl(child, charset);
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public InputStream openInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "[file=" + getCanonicalPath() + "]";
	}

	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public String getCanonicalPath() {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException("exception resolving path to full form="+file.getAbsolutePath(), e);
		}
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
