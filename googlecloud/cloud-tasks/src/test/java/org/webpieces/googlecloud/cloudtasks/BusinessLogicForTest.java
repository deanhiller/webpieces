package org.webpieces.googlecloud.cloudtasks;

import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.Scheduler;
import org.webpieces.util.futures.XFuture;

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

        XFuture<JobReference> jobReference = scheduler.schedule(
                () -> api.some(req),
                20,
                TimeUnit.SECONDS);

        try {
            jobReference.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }
}
