package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;


public class GCPTaskClient {

    private static Logger log = LoggerFactory.getLogger(GCPTaskClient.class);
    private CloudTasksClient cloudTasksClient;
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String LOCATION = "LOCATION";
    public static final String WEBPIECES_SCHEDULE_RESPONSE = "webpieces-scheduleResponse";
    private Map<String, String> cloudConfig;

    public GCPTaskClient(String projectId, String location) {
        cloudConfig = setCloudSettings(projectId, location);
    }

    public GCPTaskClient() {
        cloudConfig = setCloudSettings("tray-dineqa", "us-west1");
    }

    private Map<String,String> setCloudSettings(String projectId, String location) {
        Map<String,String> map = new HashMap<>();
        map.put(PROJECT_ID,projectId);
        map.put(LOCATION,location);
        return map;
    }

    public XFuture<Void> createTask(InetSocketAddress addr, HttpMethod httpMethod, String path, String payload, ScheduleInfo scheduleInfo) {

        Endpoint endpoint = new Endpoint(addr, httpMethod.toString(), path);

        String url = getURL(endpoint,path);

        QueueName queueName = QueueName.of(cloudConfig.get(PROJECT_ID), cloudConfig.get(LOCATION),"com-tray-api-publish-PublishApi-test");

        XFuture<Void> jobReferenceXFuture = createTaskImpl(queueName, url, httpMethod, payload, scheduleInfo);

        return jobReferenceXFuture;
    }

    // Create a task with a HTTP target using the Cloud Tasks client.
    private XFuture<Void> createTaskImpl(QueueName queue, String url, HttpMethod httpMethod, String payload, ScheduleInfo scheduleInfo) {

        // Instantiates a client.
        try(CloudTasksClient client = createCloudTasksClient()) {

            log.info("Got queue: " + queue);

            // Construct the task body.
            HttpRequest request = HttpRequest.newBuilder()
                    .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                    .setUrl(url)
                    .setHttpMethod(getCloudTaskHttpMethod(httpMethod))
                    //.putAllHeaders(headers)   //TODO: Kamlesh - Ask dean what to send in headers
                    .build();

            Duration deadline = Duration.newBuilder().setSeconds(1020).build(); //set 17 minutes (longer than cloud run timeout)

            final Task.Builder taskBuilder = Task.newBuilder()
                        .setHttpRequest(request)
                        .setScheduleTime(scheduleInfo.isScheduledInFuture() ? getTimeStamp(scheduleInfo): Timestamp.newBuilder().setSeconds(0L).build())
                        .setDispatchDeadline(deadline);

            // Send create task request
            CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(()-> {
                Task task = client.createTask(queue, taskBuilder.build());
                log.info("Task created: " + task.getName());
                JobReference jobReference = new JobReference();
                jobReference.setTaskId(task.getName());
                Context.put(WEBPIECES_SCHEDULE_RESPONSE,jobReference);
                return null;
            });
            completableFuture.exceptionally( (e) -> {
                log.error("Exception queueing cloud-task for url {} {} ",httpMethod.name(),url, e);
                return null;
            });

            XFuture<Void> jobReferenceXFuture = XFuture.convert(completableFuture,null);

            return jobReferenceXFuture;

        }

    }

    private CloudTasksClient createCloudTasksClient() {

        try {
            return CloudTasksClient.create(CloudTasksSettings.newBuilder().build());
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }

    private com.google.cloud.tasks.v2.HttpMethod getCloudTaskHttpMethod(HttpMethod httpMethod) {
        if(httpMethod.equals(HttpMethod.POST))
            return com.google.cloud.tasks.v2.HttpMethod.POST;
        else if(httpMethod.equals(HttpMethod.PUT))
            return com.google.cloud.tasks.v2.HttpMethod.PUT;
        else if(httpMethod.equals(HttpMethod.PATCH))
            return com.google.cloud.tasks.v2.HttpMethod.PATCH;
        return null;
    }

    private String getURL(Endpoint endpoint,String path) {
        InetSocketAddress socketAddress = endpoint.getServerAddress();
        String scheme = (socketAddress.getPort() != 443) ? "http" : "https";
        URI uri = null;
        try {
            uri = new URI(scheme, null, socketAddress.getHostName(), socketAddress.getPort(), path, null, null);
        } catch (URISyntaxException e) {
            throw SneakyThrow.sneak(e);
        }
        return uri.toString();
    }

    private Timestamp getTimeStamp(ScheduleInfo scheduleInfo) {
        Timestamp timestamp = null;
        long currentTimeInSec = System.currentTimeMillis()/1000;

        switch(scheduleInfo.getTimeUnit()) {
            case DAYS:
                currentTimeInSec += scheduleInfo.getTime()*24*60*60*1000L;
                break;
            case HOURS:
                currentTimeInSec += scheduleInfo.getTime()*60*60*1000L;
                break;
            case MINUTES:
                currentTimeInSec += scheduleInfo.getTime()*60*1000L;
                break;
            case SECONDS:
                currentTimeInSec += scheduleInfo.getTime()*1000L;
                break;
        }

        return Timestamp.newBuilder().setSeconds(currentTimeInSec).build();
    }
}
