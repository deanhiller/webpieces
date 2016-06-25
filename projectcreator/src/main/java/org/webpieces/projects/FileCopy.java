package org.webpieces.projects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileCopy {

	private File newAppDirectory;
	private String appClassName;
	private String packageStr;
	private String[] packagePieces;
	private String appName;

	public FileCopy(String appClassName, String appName, String packageStr, String directory) {
		File newAppDirectory = new File(directory);
		if(!newAppDirectory.exists()) {
			newAppDirectory.mkdirs();
		} else if(!newAppDirectory.isDirectory())
			throw new IllegalArgumentException("directory="+newAppDirectory.getAbsolutePath()+" already exists BUT is not a directory and needs to be");
		else if(packageStr.contains("/") || packageStr.contains("\\"))
			throw new IllegalArgumentException("package must contain '.' character and no '/' nor '\\' characters");
		
		this.newAppDirectory = newAppDirectory;
		this.appClassName = appClassName;
		this.appName = appName;
		this.packageStr = packageStr;
		this.packagePieces = packageStr.split("\\.");
	}
	
	public void createProject() throws IOException {

		//we have 
		//   - ZAPPCLASS
		//   - CLASSNAME
		//   - PACKAGE
		// and need to replace those three things in file names, or file text
		//ALSO, must rename all *.GRA files to *.GRADLE so the build is in place
		//LASTLY, need to replace "TEMPLATE" with "" to make it disappear

		//we only allow execution from the jar file right now due to this...(so running this in the IDE probably won't work)
		String path = ProjectCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		File jarFile = new File(path);

		File webpiecesDir = jarFile.getParentFile().getParentFile();
		//currently, just the one template...
		File template = new File(webpiecesDir, "templateProject");

		System.out.println("/n");
		System.out.println("copy from directory="+template);
		System.out.println("copy to directory  ="+newAppDirectory);
		copyFiles(template, newAppDirectory);
	}

	private void copyFiles(File template, File targetDirectory) {
		for(File f : template.listFiles()) {
			if(f.isDirectory()) {
				copyDirectory(f, targetDirectory);
			} else {
				copyFile(f, targetDirectory);
			}
		}
	}

	private void copyFile(File f, File targetDirectory) {
		String newFileName = getFileName(f);
		File newFile = new File(targetDirectory, newFileName);

		try {
			Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Files.copy(newFile.toPath(), out);
			String contents = new String(out.toByteArray(), Charset.defaultCharset());

			contents.replace("PACKAGE", packageStr);
			contents.replace("TEMPLATE", "");
			contents.replace("CLASSNAME", appClassName);
		
			ByteArrayInputStream in = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
			Files.copy(in, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		} catch(IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void copyDirectory(File f, File targetDirectory) {
		String name = getFileName(f);
		
		String[] pieces = new String[] { name };
		if("PACKAGE".equals(name)) {
			pieces = packagePieces;
		}
		
		File toCreate = createPackageFile(targetDirectory, pieces);
		toCreate.mkdirs();
		
		System.out.println("copy from directory="+f.getAbsolutePath());
		System.out.println("copy to directory  ="+toCreate.getAbsolutePath());
		
		copyFiles(f, toCreate);
	}

	private String getFileName(File f) {
		String name = f.getName();
		if(name.contains("TEMPLATE"))
			name = name.replace("TEMPLATE", "");
		
		if(name.contains("APPNAME"))
			name = name.replace("APPNAME", appName);
		
		if(name.contains("GRA"))
			name = name.replace("GRA", "gradle");
		
		if(name.contains("CLASSNAME"))
			name = name.replace("CLASSNAME", appClassName);
		
		return name;
	}

	private File createPackageFile(File targetDirectory, String[] pieces) {
		File f = targetDirectory;
		for(String name : pieces) {
			System.out.println("piece="+name);
			f = new File(f, name);
		}
		return f;
	}
}
