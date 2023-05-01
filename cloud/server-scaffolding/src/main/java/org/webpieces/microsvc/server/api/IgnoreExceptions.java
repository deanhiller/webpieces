package org.webpieces.microsvc.server.api;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.http.exception.HttpClientErrorException;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class IgnoreExceptions {

    private List<Class> successExceptions;

    @Inject
    public IgnoreExceptions(ClientServiceConfig config) {
        successExceptions = config.getSuccessExceptions();
        if(successExceptions == null) {
            successExceptions = Arrays.asList(HttpClientErrorException.class, BadRequestException.class);
        }
    }

    public boolean exceptionIsSuccess(Throwable e) {
        Throwable unwrapped = unwrapEx(e); // stupid checked exceptions wrapping up the important one
        for(Class clazz : successExceptions) {
            if(clazz.isInstance(unwrapped)) {
                return true;
            }
        }

        return false;
    }

    public Throwable unwrapEx(Throwable t) {
        while ((t instanceof ExecutionException) || (t instanceof CompletionException) || (t instanceof InvocationTargetException)) {
            if (t.getCause() == null) {
                break;
            }
            t = t.getCause();
        }
        return t;
    }
}
