package org.webpieces.util;

import java.util.function.Supplier;

public class SingletonSupplier<T> implements Supplier<T> {

    private volatile boolean initialized;
    private Supplier<T> delegate;
    private T value;

    public SingletonSupplier(final Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {

        if(initialized) {
            return value;
        }

        synchronized(this) {

            if(!initialized) {

                value = delegate.get();
                initialized = true;
                delegate = null;

            }

        }

        return value;

    }

}