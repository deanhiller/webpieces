package org.webpieces.util.futures;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LogbackServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.lang.reflect.Field;

public class Logging {

    public static boolean alreadyRun = false;

    /**
     * BIG NOTE: IF installed too late, the mdcAdapter ends up everywhere!!!
     */
    public static void setupMDCForLogging() {
        //To test this flag, start Server.java (calls this method once) and
        // start ProdServerForIDE.java (calls this method twice)
        if(alreadyRun)
            return;

        long start = System.currentTimeMillis();
        LogbackServiceProvider provider = fetch();
        if(provider != null)
            throw new IllegalStateException("You called MDCSetup.setupLogging() too late.  call it in a static {} block very early BEFORE any loggers are created");

        //trigger logging initialization
        Logger log = LoggerFactory.getLogger(Logging.class);
        provider = fetch();

        LoggerContext ctx = fetchCtx(provider);

        MDCAdapter asyncAdapter = new XFutureMDCAdapter();
        replaceWithOurMDCAdapter(asyncAdapter, provider);
        replaceWithOurMDCAdapter(asyncAdapter, ctx);

        long time = System.currentTimeMillis() - start;
        log.info("Logback initialization took="+time+"ms");
        alreadyRun = true;
    }

    private static void replaceWithOurMDCAdapter(MDCAdapter asyncAdapter, Object target) {
        try {
            Field adapterField = target.getClass().getDeclaredField("mdcAdapter");
            adapterField.setAccessible(true);
            adapterField.set(target, asyncAdapter);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static LoggerContext fetchCtx(LogbackServiceProvider provider) {
        try {
            Field f = LogbackServiceProvider.class.getDeclaredField("defaultLoggerContext");
            f.setAccessible(true);
            return (LoggerContext) f.get(provider);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static LogbackServiceProvider fetch() {
        try {
            Field f = LoggerFactory.class.getDeclaredField("PROVIDER");
            f.setAccessible(true);
            SLF4JServiceProvider prov = (SLF4JServiceProvider)f.get(LoggerFactory.class);
            if(prov == null) {
                return null;
            } else if(!(prov instanceof LogbackServiceProvider)) {
                throw new IllegalStateException("Expected logback provider:" + LogbackServiceProvider.class.getSimpleName() + " instead was=" + prov.getClass().getName());
            }
            return (LogbackServiceProvider) prov;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
