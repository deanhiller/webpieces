package webpiecesxxxxxpackage.services;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.file.VirtualFile;
import org.webpieces.webserver.api.IDESupport;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public abstract class YourCompanyAbstractDevServer {

    protected VirtualFile directory;
    protected ArrayList<VirtualFile> srcPaths;
    protected String[] args;

    public YourCompanyAbstractDevServer(
            String name,
            boolean usePortZero
    ) {
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
}
