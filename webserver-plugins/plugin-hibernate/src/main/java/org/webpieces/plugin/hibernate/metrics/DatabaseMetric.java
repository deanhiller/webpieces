package org.webpieces.plugin.hibernate.metrics;

import java.util.ArrayList;
import java.util.List;

public enum DatabaseMetric {

    DATABASE_ENTITY_LOADS("webpieces/database/entity/loads"),
    DATABASE_ENTITY_DELETES("webpieces/database/entity/deletes"),
    DATABASE_ENTITY_UPDATES("webpieces/database/entity/updates"),
    DATABASE_ENTITY_INSERTS("webpieces/database/entity/inserts"),

    EXECUTION_TIME("webpieces/database/execution/time", "/max"),
    EXECUTION_COUNT("webpieces/database/execution/time", "/count")
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
