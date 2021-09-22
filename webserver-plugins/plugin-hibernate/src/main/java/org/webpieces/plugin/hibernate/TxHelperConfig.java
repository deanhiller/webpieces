package org.webpieces.plugin.hibernate;

public class TxHelperConfig {

    private String serviceName = "unknown"; // Default to "unknown"

    public TxHelperConfig() {
    }

    public TxHelperConfig(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

}
