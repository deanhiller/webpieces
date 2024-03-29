package org.webpieces.webserver.impl;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.Throttle;
import org.webpieces.util.futures.XFuture;

import java.util.concurrent.atomic.AtomicInteger;

public class ThrottleProxy implements StreamWriter {
    private static final Logger log = LoggerFactory.getLogger(ThrottleProxy.class);
    public static final Logger THROTTLE_LOGGER = Throttle.LOG;

    private StreamWriter str;
    private Throttle throttler;
    private boolean decremented;

    private static AtomicInteger count = new AtomicInteger();

    public ThrottleProxy(StreamWriter str, Throttle throttler, boolean decremented) {
        this.str = str;
        this.throttler = throttler;
        this.decremented = decremented;
    }

    @Override
    public XFuture<Void> processPiece(StreamMsg data) {
        return str.processPiece(data).thenApply((v) -> {
            if(data.isEndOfStream()) {
                if (decremented) {
                    log.warn("BUG, should only occur once per message" + data, new RuntimeException().fillInStackTrace());
                } else {
                    throttler.decrement();
                }

                if(THROTTLE_LOGGER.isDebugEnabled()) {
                    int i = count.addAndGet(1);
                    if (i % 10 == 0) {
                        THROTTLE_LOGGER.debug("Response data EOM=" + i);
                    }
                }
                decremented = true;
            }

            return null;
        });
    }
}
