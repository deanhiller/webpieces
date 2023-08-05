package org.webpieces.nio.api;

public interface Throttle {

    public boolean isThrottling();

    public void setFunctionToInvoke(Runnable turnThrottlingOff);

}
