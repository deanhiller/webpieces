package org.playorm.util.api.safethread;

/**
 */
public interface ExceptionListener
{
    public void fireFailure(Throwable e, Object id);
}
