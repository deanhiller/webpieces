package org.webpieces.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class SneakyThrow {

    public static <T extends Throwable> RuntimeException sneak(Throwable t) throws T {
        if(t instanceof ExecutionException) {
            //very important to keep this ones stack trace so rethrow
            throw (T)t;
        }
        while((t instanceof CompletionException) || (t instanceof InvocationTargetException)) {

            if (t.getCause() == null) {
                break;
            }

            t = t.getCause();

        }

        throw (T)t;
    }
}
