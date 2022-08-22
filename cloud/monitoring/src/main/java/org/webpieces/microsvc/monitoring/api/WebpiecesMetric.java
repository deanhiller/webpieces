package org.webpieces.microsvc.monitoring.api;

import java.util.List;

public class WebpiecesMetric {

    private final String name;
    private final List<String> dimensions;

    public WebpiecesMetric(final String name, List<String> dimensions) {
        this.name = name;
        this.dimensions = (dimensions != null) ? List.copyOf(dimensions) : List.of();
    }

    public final String getName() {
        return name;
    }

    public final List<String> getDimensions() {
        return dimensions;
    }

}
