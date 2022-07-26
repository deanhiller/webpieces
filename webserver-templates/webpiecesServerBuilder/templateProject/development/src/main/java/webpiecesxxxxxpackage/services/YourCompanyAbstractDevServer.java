package webpiecesxxxxxpackage.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.webserver.api.IDESupport;
import org.webpieces.webserver.api.WebpiecesServer;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public abstract class YourCompanyAbstractDevServer {

	private static final Logger log = LoggerFactory.getLogger(YourCompanyAbstractDevServer.class);
    protected VirtualFile directory;
    protected ArrayList<VirtualFile> srcPaths;
    protected String[] args;

    private ExecutorService fileWatchThread = Executors.newFixedThreadPool(1, new MyFileWatchThreadFactory());

    public YourCompanyAbstractDevServer(
            String name,
            boolean usePortZero
    ) {
        //In DevServer or ProdServerForIDE, if platform is upgraded, bad things can happen so we shutdown on platform upgrade
        //if we are currently running.
        String file = "/"+ WebpiecesServer.class.getName().replaceAll("\\.", "/")+".class";
        URL res = getClass().getResource(file);
        if (res.getProtocol().equals("jar")) {
            watchForDangerousJarChanges(res);
        }
    	
    	
        DevConfig config = getConfig();
        
        directory = IDESupport.modifyForIDE(name);

        

        //list all source paths here(DYNAMIC html files and java) as you add them(or just create for loop)
        //These are the list of directories that we detect java file changes under.  static source files(html, css, etc) do
        //not need to be recompiled each change so don't need to be listed here.
        srcPaths = new ArrayList<>();

        //Next you need to add the source code directories so we can re-compile the files you change
        //If you add libraries, add the directories here BUT prefer looping to find them
        srcPaths.add(directory.child(name+"/src/main/java"));
        srcPaths.add(directory.child(name+"-dev/src/main/java"));
        
      /* Here(see below commented out code as example) you should programmatically add all source to your servers
       * Ideally, you can add just the source from the libraries you depend on BUT you can start for speed and agility
       * by just adding all as if you have N directories and M files, we are O(N) lookup since we are O(1) in each
       * directory you add.  ie. you can add around 50 libs no problem for now
       *
       */
//        String path = directory.getCanonicalPath();
//        File f = new File(path);
//        File javaDirectory = f.getParentFile().getParentFile();
//        File libraries = new File(javaDirectory, "libraries");
//        File[] allLibs = libraries.listFiles();
//        for(File lib : allLibs) {
//        	//At some point you need to suck in all other source directories...
//            VirtualFile virtLib = new VirtualFileImpl(lib);
//            VirtualFile child = virtLib.child("src/main/java");
//            if(child.exists())
//                srcPaths.add(child);
//        }


        List<String> tempArgs = new ArrayList<>();
        if(usePortZero) {
            tempArgs.add("-http.port=:0");
            tempArgs.add("-https.port=:0");
        } else {
            tempArgs.add("-http.port=:" + config.getHttpPort());
            tempArgs.add("-https.port=:" + config.getHttpsPort());
        }

        tempArgs.add("-https.over.http=true");
        if(config.getHibernateSettingsClazz() != null)
            tempArgs.add("-hibernate.persistenceunit=" + config.getHibernateSettingsClazz());

        String[] args = config.getExtraArguments();

        if(args != null) {
            for (String a : args) {
                tempArgs.add(a);
            }
        }

        this.args = tempArgs.toArray(new String[0]);
    }

    protected abstract DevConfig getConfig();

    public abstract void start();
    
    private void watchForDangerousJarChanges(URL res) {
        try {
            watchForDangerousJarChangesImpl(res);
        } catch (IOException e) {
            throw new RuntimeException("Weird but this code can be safely removed BUT just make sure you restart your servers when upgrading any 3rd party jars");
        }
    }

    private void watchForDangerousJarChangesImpl(URL res) throws IOException {
        //It's always a jar in your code but sometimes we run Dev Server in webpieces where the code is not a jar
        //ie(you can delete the if statement if you like)
        log.info("res="+res.getFile()+" res="+res+" res1"+res.getPath());
        //register jar file listener so on changes, we shutdown the server on the developer and make them reboot
        String filePath = res.getFile();
        String absPath = filePath.substring("file:".length());
        String fullJarPath = absPath.split("!")[0];
        File f = new File(fullJarPath);
        Path directoryPath = f.getParentFile().toPath();

        WatchService watcher = FileSystems.getDefault().newWatchService();
        directoryPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        fileWatchThread.execute(new MyFileWatchRunnable(watcher));
    }
    
    private class MyFileWatchRunnable implements Runnable {

        private WatchService watcher;

        public MyFileWatchRunnable(WatchService watcher) {
            this.watcher = watcher;
        }

        @Override
        public void run() {
            WatchKey key;
            try {
                log.info("Starting to watch files");
                // wait for a key to be available
                key = watcher.take();
            } catch (Throwable ex) {
                log.error("Exception", ex);
            }

            log.error("\n-------------------------------------------------------------------------------\n"
                + "Webpiecees was upgraded so we need to shutdown the server to use the new jar files or bad things happen\n"
                +"-------------------------------------------------------------------------------");

            System.exit(9492);
        }
    }

    private class MyFileWatchThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("fileWatchThread");
            return t;
        }
    }
}
