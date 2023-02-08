package org.webpieces.googlecloud.cloudtasks.api;

import org.webpieces.googlecloud.cloudtasks.impl.ScheduleInfo;
import org.webpieces.util.context.Context;

import java.util.concurrent.TimeUnit;

public class Scheduler {

    public void schedule(Runnable runnable, int time, TimeUnit timeUnit) {
        ScheduleInfo info = new ScheduleInfo(time, timeUnit);
        Context.put("scheduleInfo", info);

        try {
            runnable.run();
        } finally {
            Context.remove("scheduleInfo");
        }
    }
}
