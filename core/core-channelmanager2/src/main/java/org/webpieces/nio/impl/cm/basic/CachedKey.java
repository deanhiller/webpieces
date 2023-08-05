package org.webpieces.nio.impl.cm.basic;

import java.nio.channels.SelectionKey;

public class CachedKey {
    private final SelectionKey key;
    private final ChannelInfo info;

    public CachedKey(SelectionKey key, ChannelInfo info) {
        this.key = key;
        this.info = info;
    }

    public SelectionKey getKey() {
        return key;
    }

    public ChannelInfo getInfo() {
        return info;
    }
}
