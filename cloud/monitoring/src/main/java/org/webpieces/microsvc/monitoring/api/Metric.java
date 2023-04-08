package org.webpieces.microsvc.monitoring.api;

import java.util.List;

public interface Metric {

    String getName();

    List<String> getDimensions();

}
