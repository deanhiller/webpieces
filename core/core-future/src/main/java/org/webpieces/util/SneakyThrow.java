package org.webpieces.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class SneakyThrow {

    public static <T extends Throwable> RuntimeException sneak(Throwable t) throws T {
        throw (T)t;
    }
}
