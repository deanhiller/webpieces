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

	public VirtualFileImpl(File file) {
		//next one line is an odd fix needed for apple and 1.8.0_111.  remove and rerun tests to see if fixed ;)
		//for some reason, to work, the absolute file is needed which is there but not being used for some reason
		this.file = new File(file.getAbsolutePath());
	}
	
	public VirtualFileImpl(String fileName) {
		this(new File(fileName));
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
	public String contentAsString(Charset charset) {
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
		return new VirtualFileImpl(child);
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
