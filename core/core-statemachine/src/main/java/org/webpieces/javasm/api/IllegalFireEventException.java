package org.webpieces.javasm.api;

/**
 */
public class IllegalFireEventException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance of IllegalFireEventException.
     */
    public IllegalFireEventException()
    {
        super();
    }


    /**
     * Creates an instance of IllegalFireEventException.
     * @param message
     */
    public IllegalFireEventException(String message)
    {
        super(message);
    }


    /**
     * Creates an instance of IllegalFireEventException.
     * @param message
     * @param cause
     */
    public IllegalFireEventException(String message, Throwable cause)
    {
        super(message, cause);
    }


    /**
     * Creates an instance of IllegalFireEventException.
     * @param cause
     */
    public IllegalFireEventException(Throwable cause)
    {
        super(cause);
    }
}
