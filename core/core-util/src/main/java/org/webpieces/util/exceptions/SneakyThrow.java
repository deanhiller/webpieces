package org.webpieces.util.exceptions;

public class SneakyThrow {

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneak(Throwable t) throws T {
        throw (T)t;
    }

}
