package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.Throttle;
import org.webpieces.nio.api.Throttler;

public class NoThrottle implements Throttle {
    @Override
    public boolean isThrottling() {
        return false;
    }
}
