package org.webpieces.util.cmdline;

import org.webpieces.util.cmdline2.JvmEnv;

import java.util.Map;

public class MockEnv extends JvmEnv {

    private Map<String, String> environment;

    @Override
    public String readEnvVar(String name) {
        return environment.get(name);
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }
}
