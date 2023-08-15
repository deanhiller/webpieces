package org.webpieces.webserver.test;

import org.webpieces.nio.api.Throttle;

public class NoThrottler implements Throttle {
    @Override
    public void increment() {

    }

    @Override
    public void decrement() {

    }

    @Override
    public boolean isThrottling() {
        return false;
    }

    @Override
    public void setFunctionToInvoke(Runnable turnThrottlingOff) {

    }
}
