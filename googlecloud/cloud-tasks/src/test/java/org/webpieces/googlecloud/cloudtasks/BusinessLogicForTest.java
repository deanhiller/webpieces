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
        CreateRequest req = new CreateRequest();
        req.setName("dean");
        req.setJob("engineering");

        XFuture<JobReference> jobReference = scheduler.schedule(
                () -> api.create(req),
                1,
                TimeUnit.MINUTES);

        try {
            jobReference.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }
}
