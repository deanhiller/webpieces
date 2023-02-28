package org.webpieces.microsvc.client.impl;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.httpclient11.api.SocketClosedException;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RequestCloseListener<T> implements Http2SocketListener {

    private XFuture<T> aFutureException = new XFuture<>();
    private ScheduledExecutorService executorService;

    public RequestCloseListener(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void socketFarEndClosed(Http2Socket socket) {

        //schedule in the future to avoid race and give time of response to get through
        //TODO(dhiller): look into this more...is this needed.  we were seeing socket closed exceptions
        //resulting in customer 500's and this seems to solve it(we think).  If this comment is stilll here,
        //it solved it.
        executorService.schedule(() -> {
            //cancel the responseFuture if not already cancelled...
            aFutureException.completeExceptionally(new SocketClosedException("Socket closed=" + socket));
        }, 2, TimeUnit.SECONDS);

    }

    public void setFuture(XFuture<T> responseFuture) {

        //when aFutureException resolves to an exception, THEN run the code below which
        //tells the responseFuturee to complete with an exception or else it may never
        //complete(unless there is a timeout).
        aFutureException.exceptionally(t -> {
            responseFuture.completeExceptionally(t);
            return null;
        });

    }

}
