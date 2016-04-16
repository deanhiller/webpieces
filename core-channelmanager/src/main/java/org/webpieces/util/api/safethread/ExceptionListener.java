package org.webpieces.util.api.safethread;

/**
 */
public interface ExceptionListener
{
    public void fireFailure(Throwable e, Object id);
}
