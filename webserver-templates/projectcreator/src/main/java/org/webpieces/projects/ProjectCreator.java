package org.webpieces.projects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProjectCreator {

	private String version;

	public ProjectCreator(String version) {
		this.version = version;
	}

	public static void main(String[] args) throws IOException {
		String version = System.getProperty("webpieces.version");
		if(version == null)
			throw new IllegalArgumentException("We must have the version on project creation");
		System.out.println("Starting up VERSION="+version+" args.length="+args.length);

		ProjectCreator creator = new ProjectCreator(version);
		if(args.length > 0)
			creator.createProject(args);
		else
			creator.start();
	}

	private void createProject(String[] args) throws IOException {
		if(args.length != 3)
			throw new IllegalArgumentException("./createProject {templateNumber} {className} {package} {Directory} is the format");

		String templateName = args[0];
		String className = args[1];
		String packageStr = args[2];
		String dir = args[3];

		File webpiecesDir = fetchRunningDir();
		List<File> templateDirs = fetchTemplateDirs(webpiecesDir);
		File templateDir = findByName(templateName, templateDirs);

		createProject(templateDir, className, packageStr, dir);
	}

	private File findByName(String templateName, List<File> templateDirs) {
		for(File f : templateDirs) {
			if(f.getName().equals(templateName))
				return f;
		}

		System.err.println("Template '"+templateName+"' was not found in list of templates");
		System.exit(1);
		return null;
	}

	private void start() throws IOException {
		File webpiecesDir = fetchRunningDir();
		List<File> templateDirs = fetchTemplateDirs(webpiecesDir);

		try (Scanner scanner = new Scanner(System.in)) {
			for(File f : templateDirs) {
				System.out.println("* "+f.getName());
			}
			//  prompt for the user's template
			System.out.print("Enter the template name above to use(case sensitive): ");

			// get their input as a String
			String templateName = scanner.next();
			File templateDir = findByName(templateName, templateDirs);

			//  prompt for the user's name
		    System.out.print("Enter your camel case app name(used in class file names): ");
	
		    // get their input as a String
		    String appClassName = scanner.next();
		    String appDirectoryNameTmp = appClassName.toLowerCase();

		    
		    System.out.println("Enter your package with . separating each package(ie. org.webpieces.myapp): ");
		    String packageStr = scanner.next();
		    
		    System.out.println("\n\n\n");
		    String currentDir = System.getProperty("user.dir");
		    System.out.println("your current directory is '"+currentDir+"'");
		    System.out.println("Enter the path relative to the above directory or use an absolute directory for where");
		    System.out.println("we will create a directory called="+appDirectoryNameTmp+" OR will re-use an existing directory called "+ appDirectoryNameTmp+" to fill it in");
		    String directory = scanner.next();
		    
		    createProject(templateDir, appClassName, packageStr, directory);
		}
	}

	private List<File> fetchTemplateDirs(File webpiecesDir) {
		List<File> templateDirs = new ArrayList<>();
		File[] files = webpiecesDir.listFiles();
		for(File f : files) {
			if(f.isDirectory() && f.getName().startsWith("Template")) {
				templateDirs.add(f);
			}
		}
		return templateDirs;
	}

	private int convert(String templateNumber) {
		try {
			return Integer.parseInt(templateNumber);
		} catch (NumberFormatException e) {
			System.err.println("You were supposed to enter a number.  Good bye");
			System.exit(1);
		}

		throw new IllegalStateException("Should never reach here ever");
	}

	private File fetchRunningDir() {
		//we only allow execution from the jar file right now due to this...(so running this in the IDE probably won't work)
		String path = ProjectCreator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		File jarFile = new File(path);
		System.out.println("Running from jar file="+jarFile);

		File webpiecesDir = jarFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
		System.out.println("Base Directory="+webpiecesDir);
		return webpiecesDir;
	}

	private void createProject(File templateDir, String appClassName, String packageStr, String directory) throws IOException {
		String justAppName = appClassName.toLowerCase();

		if(packageStr.contains("/") || packageStr.contains("\\"))
			throw new IllegalArgumentException("package must contain '.' character and no '/' nor '\\' characters");
		
		File dirTheUserTypedIn = new File(directory);
		setupDirectory(dirTheUserTypedIn);

		File appDir = new File(dirTheUserTypedIn, justAppName);
		setupDirectory(appDir);
		
		new FileCopy(templateDir, appClassName, justAppName, packageStr, appDir, version).createProject();
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

}
