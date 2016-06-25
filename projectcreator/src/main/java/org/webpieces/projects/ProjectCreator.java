package org.webpieces.projects;

import java.io.File;
import java.io.IOException;
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
		    
		    new FileCopy(appClassName, appName, packageStr, directory).createProject();
		}
	}


}
