package org.webpieces.nio.impl.cm.basic;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class DelayedRunnables {
    private Map<SelectionKey, Runnable> delayedSvrSockets = new HashMap<>();
    private Map<SelectionKey, Runnable> delayedReads = new HashMap<>();

    public Map<SelectionKey, Runnable> getDelayedSvrSockets() {
        return delayedSvrSockets;
    }

    public Map<SelectionKey, Runnable> getDelayedReads() {
        return delayedReads;
    }
}
