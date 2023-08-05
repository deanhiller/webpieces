package org.webpieces.util.threading;

public class Throttling {

    private boolean isThrottling;

    public boolean isThrottling() {
        return isThrottling;
    }

    public void setThrottling(boolean throttling) {
        isThrottling = throttling;
    }
}
