package org.webpieces.nio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Throttle {

    static final Logger log = LoggerFactory.getLogger(Throttle.class);

    public void increment();

    public void decrement();

    public boolean isThrottling();

    public void setFunctionToInvoke(Runnable turnThrottlingOff);

}
