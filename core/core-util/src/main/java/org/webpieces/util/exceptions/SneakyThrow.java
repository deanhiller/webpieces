package org.webpieces.util.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class SneakyThrow {

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneak(Throwable t) throws T {
        while((t instanceof ExecutionException) || (t instanceof CompletionException) || (t instanceof InvocationTargetException)) {
            t = t.getCause();
        }
        throw (T)t;
    }

}
