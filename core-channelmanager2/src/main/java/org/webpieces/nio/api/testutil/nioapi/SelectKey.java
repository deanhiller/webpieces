package org.webpieces.nio.api.testutil.nioapi;


/**
 */
public interface SelectKey
{

    /**
     */
    Object attachment();

    /**
     */
    int interestOps();

    /**
     * @param opsNow
     */
    SelectKey interestOps(int opsNow);

}
