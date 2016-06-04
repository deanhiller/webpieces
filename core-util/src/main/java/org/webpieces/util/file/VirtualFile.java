package org.webpieces.util.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * Just use new VirtualFileImpl(File) unless you want to do something more advanced
 * 
 * @author dhiller
 *
 */
public interface VirtualFile {

	boolean isDirectory();

	String getName();

	List<VirtualFile> list();

	String contentAsString();

	long lastModified();

	VirtualFile child(String fileName);

	boolean exists();

	InputStream openInputStream();

	String getAbsolutePath();

	URL toURL();

	long length();

	boolean mkdirs();

	OutputStream openOutputStream();

	boolean delete();

}
