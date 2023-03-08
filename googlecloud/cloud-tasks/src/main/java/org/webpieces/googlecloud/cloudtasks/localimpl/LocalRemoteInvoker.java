package org.webpieces.googlecloud.cloudtasks.localimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.impl.Constants;
import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.RemoteInvoker;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LocalRemoteInvoker implements RemoteInvoker {

    private final Logger log = LoggerFactory.getLogger(LocalRemoteInvoker.class);

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
    private HttpClientWrapper client;

    @Inject
    public LocalRemoteInvoker(HttpClientWrapper client) {
        this.client = client;
        this.client.init(executorService);
    }

    @Override
    public XFuture<Void> invoke(Method method, InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {
        Map<String, Object> copy = Context.copyContext();


        //TODO: implement Map and map jobId to scheduled task to allow deletion of tasks as well.


        if(info.isScheduledInFuture()) {
            log.info("scheduling in the future"+info.getTime()+" "+info.getTimeUnit());
            executorService.schedule(
                    () -> pretendToBeCallFromGCPCloudTasks(copy, addr, path, httpMethod, bodyAsText),
                    info.getTime(), info.getTimeUnit());
        } else {
            log.info("adding to queue");
            executorService.execute(
                    () -> pretendToBeCallFromGCPCloudTasks(copy, addr, path, httpMethod, bodyAsText)
            );
        }

        String jobId = UUID.randomUUID().toString();
        JobReference ref = new JobReference();
        ref.setTaskId(jobId);
        copy.put(Constants.WEBPIECES_SCHEDULE_RESPONSE, ref);

        return XFuture.completedFuture(null);
    }

    private void pretendToBeCallFromGCPCloudTasks(Map<String, Object> copy, InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText) {
        Context.restoreContext(copy);
        Endpoint endpoint = new Endpoint(addr, httpMethod.toString(), path);
        log.info("Running simulated call to remote endpoint="+endpoint);
        XFuture<String> stringXFuture = client.sendHttpRequest(bodyAsText, endpoint);
        stringXFuture.exceptionally( (e) -> {
            log.error("Exception queueing the request", e);
            return null;
        });
        Context.clear();
    }
}
