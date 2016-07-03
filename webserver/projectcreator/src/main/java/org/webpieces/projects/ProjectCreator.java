package org.webpieces.projects;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class ProjectCreator {

	public static void main(String[] args) throws IOException {
		new ProjectCreator().start();
	}

	private void start() throws IOException {
		System.out.println("Starting up");
		
		try (Scanner scanner = new Scanner(System.in)) {
		    //  prompt for the user's name
		    System.out.print("Enter your camel case app name(used in class file names): ");
	
		    // get their input as a String
		    String appClassName = scanner.next();
		    String appName = appClassName.toLowerCase();
		    
		    System.out.println("Enter your package with . separating each package(ie. org.webpieces.myapp): ");
		    String packageStr = scanner.next();
		    
		    System.out.println("\n\n\n");
		    String currentDir = System.getProperty("user.dir");
		    System.out.println("your current directory is '"+currentDir+"'");
		    System.out.println("Enter the path relative to the above directory or use an absolute directory for where");
		    System.out.println("we will create a directory called="+appName+" OR will re-use an existing directory called "+ appName+" to fill it in");
		    String directory = scanner.next();
		    
			//we only allow execution from the jar file right now due to this...(so running this in the IDE probably won't work)
			String path = ProjectCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			File jarFile = new File(path);

			File webpiecesDir = jarFile.getParentFile().getParentFile();

			if(packageStr.contains("/") || packageStr.contains("\\"))
				throw new IllegalArgumentException("package must contain '.' character and no '/' nor '\\' characters");
			
			File dirTheUserTypedIn = new File(directory);
			setupDirectory(dirTheUserTypedIn);

			File appDir = new File(dirTheUserTypedIn, appName);
			setupDirectory(appDir);
			
		    new FileCopy(webpiecesDir, appClassName, appName, packageStr, appDir).createProject();
		    
		    copyPlatformJars(webpiecesDir, appDir, appName);
		}
	}

	private void setupDirectory(File dirTheUserTypedIn) throws IOException {
		if(!dirTheUserTypedIn.exists()) {
			System.out.println("Directory not exist="+dirTheUserTypedIn.getCanonicalPath()+" so we are creating it");
			dirTheUserTypedIn.mkdirs();
		} else if(!dirTheUserTypedIn.isDirectory()) {
			throw new IllegalArgumentException("directory="+dirTheUserTypedIn.getAbsolutePath()+" already exists BUT is not a directory and needs to be");
		} else
			System.out.println("Directory already exists so we are filling it in="+dirTheUserTypedIn.getCanonicalPath());
	}

	private void copyPlatformJars(File webpiecesDir, File newAppDirectory, String appName) throws IOException {
		
		File appDir = new File(newAppDirectory, appName);
		File devDir = new File(newAppDirectory, "dev-server");
		
		copyJars(new File(webpiecesDir, "lib-prod"), appDir);
		copyJars(new File(webpiecesDir, "lib-development"), devDir);
	}

	private void copyJars(File file, File appDir) throws IOException {
		File prodLib = new File(appDir, "lib");
		prodLib.mkdirs();
		
		copyDirToDir(file, prodLib);
	}

	private void copyDirToDir(File file, File lib) throws IOException {
		for(File f : file.listFiles()) {
			File newFile = new File(lib, f.getName());
			Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		}
	}
}
