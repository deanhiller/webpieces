package org.webpieces.googlecloud.cloudtasks.api;

import org.webpieces.googlecloud.cloudtasks.impl.SchedulerImpl;

public class SchedulerFactory {

    public Scheduler createScheduler() {
        return new SchedulerImpl();
    }
}
