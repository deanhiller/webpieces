package org.webpieces.nio.api.handlers;

public interface ConsumerFunc<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws Exception;

}
