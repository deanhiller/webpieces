package org.webpieces.googlecloud.cloudtasks;

import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.Scheduler;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class BusinessLogicForTest {

    private DeansApi api;
    private Scheduler scheduler;

    @Inject
    public BusinessLogicForTest(DeansApi api, Scheduler scheduler) {
        this.api = api;
        this.scheduler = scheduler;
    }


    public void runDeveloperExperience() {
        SomeRequest req = new SomeRequest();
        req.setId(5);

        JobReference jobReference = scheduler.schedule(
                () -> api.some(req),
                20,
                TimeUnit.SECONDS);


    }
}
