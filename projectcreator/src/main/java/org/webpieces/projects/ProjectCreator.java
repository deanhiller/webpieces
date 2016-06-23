package org.webpieces.projects;

import java.io.File;
import java.util.Scanner;

public class ProjectCreator {

	public static void main(String[] args) {
		new ProjectCreator().start();
	}

	private void start() {
		Scanner scanner = new Scanner(System.in);
	    //  prompt for the user's name
	    System.out.print("Enter your camel case app name(used in class file names): ");

	    // get their input as a String
	    String appClassName = scanner.next();
	    String appName = appClassName.toLowerCase();
	    
	    System.out.println("Enter your package with . separating each package(ie. org.webpieces.myapp): ");
	    String packageStr = scanner.next();
	    
	    String currentDir = System.getProperty("user.dir");
	    System.out.println("your current directory is '"+currentDir+"'");
	    System.out.println("Enter the path relative to the above directory or use an absolute directory for where");
	    System.out.println("we will create a directory called="+appName+" OR will re-use an existing directory called "+ appName+" to fill it in");
	    String directory = scanner.next();
	    
	    createProject(appClassName, packageStr, directory);
	}

	private void createProject(String appClassName, String packageStr, String directory) {
		File f = new File(directory);
		if(!f.exists()) {
			f.mkdirs();
		} else if(!f.isDirectory())
			throw new IllegalArgumentException("directory="+f.getAbsolutePath()+" already exists BUT is not a directory and needs to be");
		
		//we only allow execution from the jar file right now due to this...
		String path = ProjectCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		File libDir = new File(path);
		File webpiecesDir = f.getParentFile();
		
	}
}
