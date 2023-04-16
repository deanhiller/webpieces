package org.webpieces.util.cmdline2;

import javax.inject.Singleton;

@Singleton
public class JvmEnv {
    public String readEnvVar(String name) {
        return System.getenv(name);
    }
}
