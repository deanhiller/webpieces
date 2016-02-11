package org.playorm.nio.impl.cm.basic.nioimpl;

import java.nio.channels.SelectionKey;

import org.playorm.nio.api.testutil.nioapi.SelectKey;


/**
 */
public class SelectKeyImpl implements SelectKey
{

    private SelectionKey key;

    /**
     * Creates an instance of SelectKeyImpl.
     * @param key
     */
    public SelectKeyImpl(SelectionKey key)
    {
        if(key == null)
            throw new IllegalArgumentException("key must not be null");
        this.key = key;
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.SelectKey#attachment()
     */
    public Object attachment()
    {
        return key.attachment();
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.SelectKey#interestOps()
     */
    public int interestOps()
    {
        return key.interestOps();
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.SelectKey#interestOps(int)
     */
    public SelectKey interestOps(int opsNow)
    {
        key.interestOps(opsNow);
        return this;
    }

}
