package org.webpieces.logging.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class WebpiecesCallerDataConverter extends ClassicConverter {

    @Override
    public String convert(final ILoggingEvent event) {

        StackTraceElement[] ste = event.getCallerData();

        if ((ste == null) || (ste.length == 0)) {
            return CallerData.CALLER_DATA_NA;
        }

        return ste[0].toString();

    }

}