package org.webpieces.plugin.hibernate.metrics;

public enum DatabaseMetric {

    DATABASE_ENTITY_LOADS("webpieces/database/entity/loads"),
    DATABASE_ENTITY_DELETES("webpieces/database/entity/deletes"),
    DATABASE_ENTITY_UPDATES("webpieces/database/entity/updates"),
    DATABASE_ENTITY_INSERTS("webpieces/database/entity/inserts"),

    TRANSACTION_TIME("webpieces/database/transaction/time", "/max"),
    TRANSACTION_TIME_COUNT("webpieces/database/transaction/time", "/count")
    ;

    private final String name;
    private final String suffix; // Some micrometer Meters report metrics to a suffixed name (ex: Timer uses /max and /count)

    DatabaseMetric(String name) {
        this(name, "");
    }

    DatabaseMetric(String name, String suffix) {
        this.name = name;
        this.suffix = suffix;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return name + suffix;
    }

    public String getDottedMetricName() {
        return name.replace("/", ".");
    }

}
