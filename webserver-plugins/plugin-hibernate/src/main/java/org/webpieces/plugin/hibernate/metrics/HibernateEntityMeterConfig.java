package org.webpieces.plugin.hibernate.metrics;

public class HibernateEntityMeterConfig {

    private String serviceName = "unknown"; // Default to "unknown"

    public HibernateEntityMeterConfig() {
    }

    public HibernateEntityMeterConfig(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

}
