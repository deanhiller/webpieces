package org.webpieces.util;

public class SneakyThrow {

    public static <T extends Throwable> RuntimeException sneak(Throwable t) throws T {
        throw (T)t;
    }
}
