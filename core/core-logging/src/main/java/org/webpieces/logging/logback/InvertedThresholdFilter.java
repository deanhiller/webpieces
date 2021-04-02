package org.webpieces.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filters events below the threshold level.
 * <p>
 * Events with a level above the specified
 * level will be denied, while events with a level
 * equal or below the specified level will trigger a
 * FilterReply.NEUTRAL result, to allow the rest of the
 * filter chain process the event.
 * <p>
 * For more information about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html#thresholdFilter
 */
public class InvertedThresholdFilter extends Filter<ILoggingEvent> {

    Level level;

    @Override
    public FilterReply decide(final ILoggingEvent event) {

        if(!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if(event.getLevel().toInt() <= level.toInt()) {
            return FilterReply.NEUTRAL;
        }
        else {
            return FilterReply.DENY;
        }

    }

    public void setLevel(final String level) {
        this.level = Level.toLevel(level);
    }

    public void start() {

        if(this.level != null) {
            super.start();
        }

    }

}

