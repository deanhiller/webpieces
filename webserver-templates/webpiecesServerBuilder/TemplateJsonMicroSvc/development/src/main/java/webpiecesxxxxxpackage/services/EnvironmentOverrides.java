package webpiecesxxxxxpackage.services;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.util.cmdline2.JvmEnv;

import java.util.Map;

public class EnvironmentOverrides implements Module {
    private Map<String, String> simulatedEnvironment;

    public EnvironmentOverrides(Map<String, String> simulatedEnvironment) {
        this.simulatedEnvironment = simulatedEnvironment;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(JvmEnv.class).toInstance(new SimulatedEnvironment());
    }

    private class SimulatedEnvironment extends JvmEnv {
        @Override
        public String readEnvVar(String name) {
            return simulatedEnvironment.get(name);
        }
    }
}
