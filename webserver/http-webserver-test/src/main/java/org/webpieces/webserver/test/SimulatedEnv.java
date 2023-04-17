package org.webpieces.webserver.test;

import org.webpieces.util.cmdline2.JvmEnv;

import java.util.Map;

public class SimulatedEnv extends JvmEnv {
    private Map<String, String> simulatedEnv;

    public SimulatedEnv() {}

    public SimulatedEnv(Map<String, String> simulatedEnv) {
        this.simulatedEnv = simulatedEnv;
    }

    public void setSimulatedEnv(Map<String, String> simulatedEnv) {
        this.simulatedEnv = simulatedEnv;
    }

    @Override
    public String readEnvVar(String name) {
        return simulatedEnv.get(name);
    }
}
