package org.webpieces.util.cmdline2;

public class RealJvmEnv implements JvmEnv {
    @Override
    public String readEnvVar(String name) {
        return System.getenv(name);
    }
}
