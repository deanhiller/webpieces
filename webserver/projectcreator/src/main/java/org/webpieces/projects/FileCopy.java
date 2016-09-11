package org.webpieces.projects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FileCopy {

	private File newAppDirectory;
	private String appClassName;
	private String packageStr;
	private String[] packagePieces;
	private String appName;
	private File webpiecesDir;
	private String packageDir;
	private String version;
	private String secretKeyBase64;

	public FileCopy(File webpiecesDir, String appClassName, String appName, String packageStr, File newAppDirectory, String version) {
		this.webpiecesDir = webpiecesDir;
		this.newAppDirectory = newAppDirectory;
		this.appClassName = appClassName;
		this.appName = appName;
		this.packageStr = packageStr;
		this.packageDir = convert(packageStr);
		this.packagePieces = packageStr.split("\\.");
		this.version = version;
		
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
			SecretKey key = keyGen.generateKey();
			byte[] encoded = key.getEncoded();
			secretKeyBase64 = Base64.getEncoder().encodeToString(encoded);
			
			//Run test of decoding to make sure it will decode when server is started..
			byte[] decoded = Base64.getDecoder().decode(secretKeyBase64);
			
			System.out.println("secret Key="+secretKeyBase64+" bytes length="+decoded.length);
			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String convert(String packageStr2) {
		return packageStr2.replace(".", "/");
	}

	public void createProject() throws IOException {

		//we have 
		//   - WEBPIECESxCLASS
		//   - WEBPIECESxPACKAGE
		// and need to replace those three things in file names, or file text
		//ALSO, must rename all *.GRA files to *.GRADLE so the build is in place

		//currently, just the one template...
		File template = new File(webpiecesDir, "templateProject");

		System.out.println("/n");
		System.out.println("copy from directory="+template.getCanonicalPath());
		System.out.println("copy to directory  ="+newAppDirectory.getCanonicalPath());
		copyFiles(template, newAppDirectory);
	}

	private void copyFiles(File template, File targetDirectory) throws IOException {
		if(!template.exists())
			throw new IllegalArgumentException("Directory="+template.getCanonicalPath()+"does not exist");
		
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
			String original = contents;

			contents = contents.replace("/WEBPIECESxPACKAGE/", "/"+packageDir+"/");
			contents = contents.replace("WEBPIECESxPACKAGE", packageStr);
			contents = contents.replace("WEBPIECESxCLASS", appClassName);
			contents = contents.replace("WEBPIECESxAPPNAME", appName);
			contents = contents.replace("//@Ignore", "@Ignore");
			contents = contents.replace("//import org.junit.Ignore;", "import org.junit.Ignore;");
			contents = contents.replace("WEBPIECESxVERSION", version);
			contents = contents.replace("_SECRETKEYHERE_", secretKeyBase64);
			
			if(contents.equals(original))
				return;
		
			//System.out.println("contents="+contents);
			
			ByteArrayInputStream in = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
			Files.copy(in, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		} catch(IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void copyDirectory(File f, File targetDirectory) throws IOException {
		String name = getFileName(f);
		
		String[] pieces = new String[] { name };
		if("WEBPIECESxPACKAGE".equals(name)) {
			pieces = packagePieces;
		}
		
		File toCreate = createPackageFile(targetDirectory, pieces);
		toCreate.mkdirs();
		
		System.out.println("copy from directory="+f.getCanonicalPath());
		System.out.println("copy to directory  ="+toCreate.getCanonicalPath());
		
		copyFiles(f, toCreate);
	}

	private String getFileName(File f) {
		String name = f.getName();
		if(name.contains("WEBPIECESxAPPNAME"))
			name = name.replace("WEBPIECESxAPPNAME", appName);
		
		if(name.endsWith("GRA"))
			name = name.replace("GRA", "gradle");
		
		if(name.contains("WEBPIECESxCLASS"))
			name = name.replace("WEBPIECESxCLASS", appClassName);
		
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
