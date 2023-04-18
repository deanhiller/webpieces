package org.webpieces.webserver.test;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.util.cmdline2.AllowDefaultingRequiredVars;
import org.webpieces.util.cmdline2.FetchValue;
import org.webpieces.util.cmdline2.JvmEnv;

import java.util.Map;

public class EnvSimModule implements Module {

    private Map<String, String> simulatedEnv;

    public EnvSimModule(Map<String, String> simulatedEnv) {
        this.simulatedEnv = simulatedEnv;
    }
    @Override
    public void configure(Binder binder) {
        binder.bind(JvmEnv.class).toInstance(new SimulatedEnv(simulatedEnv));

        binder.bind(FetchValue.class).to(AllowDefaultingRequiredVars.class).asEagerSingleton();

    }
}
