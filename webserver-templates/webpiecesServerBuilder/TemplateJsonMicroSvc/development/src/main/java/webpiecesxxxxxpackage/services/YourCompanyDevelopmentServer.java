package webpiecesxxxxxpackage.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.devrouter.api.DevRouterModule;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.util.Modules;

import webpiecesxxxxxpackage.deleteme.basesvr.YourCompanyServer;
import webpiecesxxxxxpackage.meta.JavaCache;

import com.google.inject.Module;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public abstract class YourCompanyDevelopmentServer extends YourCompanyAbstractDevServer {

    private static final Logger log = LoggerFactory.getLogger(YourCompanyDevelopmentServer.class);

    private final YourCompanyServer server;

    public YourCompanyDevelopmentServer(
            String name,
            boolean usePortZero
    ) {
        super(name, usePortZero);
        VirtualFile metaFile = directory.child("production/src/main/resources/appmetadev.txt");

        //html and json template file encoding...
        TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths);

        //java source files encoding...
        CompileConfig devConfig = new CompileConfig(srcPaths, JavaCache.getByteCache());
        devConfig.setFailIfNotInSourcePaths("WEBPIECESxPACKAGE"); //FAIL FAST if a class with this package is not in our source directories so we can fix it!!
        
        Module platformOverrides = Modules.combine(
                new DevRouterModule(devConfig),
                new DevTemplateModule(templateConfig));

        ServerConfig config = new ServerConfig(false);

        //It is very important to turn off BROWSER caching or developers will get very confused when they
        //change stuff and they don't see changes in the website
        config.setStaticFileCacheTimeSeconds(null);
        config.setMetaFile(metaFile);
        log.info("LOADING from meta file="+config.getMetaFile().getCanonicalPath());

        server = createServer(platformOverrides, null, config, args);
    }

    protected abstract YourCompanyServer createServer(Module platformOverrides, Module appOverrides, ServerConfig config, String ... args);

    public final void start() {
        server.start();
    }

    public final void stop() {
        server.stop();
    }
}
