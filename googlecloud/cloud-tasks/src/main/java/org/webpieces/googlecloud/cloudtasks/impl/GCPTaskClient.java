package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.GCPCloudTaskConfig;
import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;

import javax.inject.Inject;


public class GCPTaskClient {

    private static Logger log = LoggerFactory.getLogger(GCPTaskClient.class);
    private final GCPCloudTaskConfig config;
    private CloudTasksClient cloudTasksClient;

    @Inject
    public GCPTaskClient(GCPCloudTaskConfig config, CloudTasksClient cloudTasksClient) {
        this.config = config;
        this.cloudTasksClient = cloudTasksClient;
    }

    public JobReference createTask(Method method, InetSocketAddress addr, HttpMethod httpMethod, String path, String payload, ScheduleInfo scheduleInfo) {

        Endpoint endpoint = new Endpoint(addr, httpMethod.toString(), path);

        String url = getURL(endpoint,path);

        String queueNameStr = method.getDeclaringClass().getName()+"."+method.getName();
        queueNameStr = queueNameStr.replaceAll("\\.","-");
        log.info("queueName="+queueNameStr);

        QueueName queueName = QueueName.of(config.getProjectId(), config.getLocation(),queueNameStr);

        JobReference jobReference = createTaskImpl(queueName, url, httpMethod, payload, scheduleInfo);

        return jobReference;
    }

    // Create a task with a HTTP target using the Cloud Tasks client.
    private JobReference createTaskImpl(QueueName queue, String url, HttpMethod httpMethod, String payload, ScheduleInfo scheduleInfo) {

        // Instantiates a client.

        log.info("Got queue: " + queue);

        // Construct the task body.
        HttpRequest request = HttpRequest.newBuilder()
                .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                .setUrl(url)
                .setHttpMethod(getCloudTaskHttpMethod(httpMethod))
                //.putAllHeaders(headers)   //TODO: Ask dean what to send in headers
                .build();

        Task.Builder taskBuilder = Task.newBuilder()
                    .setHttpRequest(request);

        if(scheduleInfo.isScheduledInFuture()) {
            Timestamp timeStamp = getTimeStamp(scheduleInfo);
            log.info("Timestamp="+timeStamp);
            taskBuilder = taskBuilder.setScheduleTime(timeStamp);
        }

        // Send create task request
        Task task = cloudTasksClient.createTask(queue, taskBuilder.build());
        log.info("Task created: " + task.getName());
        JobReference jobReference = new JobReference();
        jobReference.setTaskId(task.getName());

        return jobReference;

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
        long currentTimeInMilliSeconds = System.currentTimeMillis();

        switch(scheduleInfo.getTimeUnit()) {
            case DAYS:
                currentTimeInMilliSeconds += scheduleInfo.getTime()*24*60*60*1000L;
                break;
            case HOURS:
                currentTimeInMilliSeconds += scheduleInfo.getTime()*60*60*1000L;
                break;
            case MINUTES:
                currentTimeInMilliSeconds += scheduleInfo.getTime()*60*1000L;
                break;
            case SECONDS:
                currentTimeInMilliSeconds += scheduleInfo.getTime()*1000L;
                break;
        }

        long currentTimeinSeconds = currentTimeInMilliSeconds/1000;

        return Timestamp.newBuilder().setSeconds(currentTimeinSeconds).build();
    }
}
