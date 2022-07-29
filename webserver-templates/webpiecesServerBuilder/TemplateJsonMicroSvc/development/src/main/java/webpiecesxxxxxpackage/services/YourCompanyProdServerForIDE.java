package webpiecesxxxxxpackage.services;

import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.webserver.api.ServerConfig;

import com.google.inject.util.Modules;

import webpiecesxxxxxpackage.basesvr.YourCompanyServer;

import com.google.inject.Module;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public abstract class YourCompanyProdServerForIDE extends YourCompanyAbstractDevServer {
    private final YourCompanyServer server;

    public YourCompanyProdServerForIDE(String name, boolean usePortZero) {
        super(name, usePortZero);

        //html and json template file encoding...
        TemplateCompileConfig templateConfig = new TemplateCompileConfig(srcPaths);

        Module platformOverrides = Modules.combine(new DevTemplateModule(templateConfig));

        ServerConfig config = new ServerConfig(false);

        //It is very important to turn off caching or developers will get very confused when they
        //change stuff and they don't see changes in the website
        config.setStaticFileCacheTimeSeconds(null);
        //config.setMetaFile(metaFile);

        server = createServer(platformOverrides, config, args);
    }

    protected abstract YourCompanyServer createServer(Module platformOverrides, ServerConfig config, String ... args);

    @Override
    public final void start() {
        server.start();
    }

}
