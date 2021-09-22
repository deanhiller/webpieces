package org.webpieces.plugin.hibernate.metrics;

public enum DatabaseMetric {

    DATABASE_ENTITY_LOADS("webpieces/database/entity/loads"),
    DATABASE_ENTITY_DELETES("webpieces/database/entity/deletes"),
    DATABASE_ENTITY_UPDATES("webpieces/database/entity/updates"),
    DATABASE_ENTITY_INSERTS("webpieces/database/entity/inserts"),

    TRANSACTION_TIME("webpieces/database/entity/inserts")
    ;

    private final String name;

    DatabaseMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDottedMetricName() {
        return name.replace("/", ".");
    }

}
