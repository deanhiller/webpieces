package org.webpieces.plugin.hibernate;

/**
 * If you want to aggregate your transactions by service name, @Provide this in a Guice module and supply your
 * service name. TransactionHelper will @Inject it and use the service name as an extra tag on transaction time metrics
 */
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
