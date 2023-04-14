package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class GCPTaskClient {

    private static Logger log = LoggerFactory.getLogger(GCPTaskClient.class);

    private final GCPCloudTaskConfig config;
    private CloudTasksClient cloudTasksClient;
    private final Set<String> secureList = new HashSet<>();
    private final Set<PlatformHeaders> toTransfer = new HashSet<>();
    @Inject
    public GCPTaskClient(
            GCPCloudTaskConfig config,
            CloudTasksClient cloudTasksClient,
            ClientServiceConfig clientServiceConfig
    ) {
        this.config = config;
        this.cloudTasksClient = cloudTasksClient;

        if(clientServiceConfig.getHcl() == null)
            throw new IllegalArgumentException("clientServiceConfig.getHcl() cannot be null and was");

        List<PlatformHeaders> listHeaders = clientServiceConfig.getHcl().listHeaderCtxPairs();

        Context.checkForDuplicates(listHeaders);

        for(PlatformHeaders header : listHeaders) {
            if(header.isSecured())
                secureList.add(header.getHeaderName());
            if(header.isWantTransferred())
                toTransfer.add(header);
        }
    }

    public JobReference createTask(Method method, InetSocketAddress addr, HttpMethod httpMethod, String path, String payload, ScheduleInfo scheduleInfo, QueueLookup lookup) {

        Endpoint endpoint = new Endpoint(addr, httpMethod.toString(), path);

        String url = getURL(endpoint,path);

        String queueNameStr;
        QueueKey annotation = method.getAnnotation(QueueKey.class);
        if(annotation == null) {
            queueNameStr = method.getDeclaringClass().getName() + "." + method.getName();
            queueNameStr = queueNameStr.replaceAll("\\.","-");
        } else if(lookup == null) {
            throw new IllegalStateException("should not be possible as we put precondition on createClient");
        } else {
            String key = annotation.value();
            queueNameStr = lookup.fetchQueueName(key);
        }
        log.info("queueName="+queueNameStr);

        QueueName queueName = QueueName.of(config.getProjectId(), config.getLocation(),queueNameStr);

        JobReference jobReference = createTaskImpl(queueName, url, httpMethod, payload, scheduleInfo);

        return jobReference;
    }

    public void deleteTask(JobReference reference) {
        log.info("deleteTask reference "+reference);
        cloudTasksClient.deleteTask(reference.getTaskId());
    }

    // Create a task with a HTTP target using the Cloud Tasks client.
    private JobReference createTaskImpl(QueueName queue, String url, HttpMethod httpMethod, String payload, ScheduleInfo scheduleInfo) {

        Map<String, String> headers = new HashMap<>();
        for(PlatformHeaders header : toTransfer) {
            String magic = Context.getMagic(header);
            if(magic != null) {
                headers.put(header.getHeaderName(), magic);
            }
        }

        // Construct the task body.
        HttpRequest request = HttpRequest.newBuilder()
                .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                .setUrl(url)
                .setHttpMethod(getCloudTaskHttpMethod(httpMethod))
                .putAllHeaders(headers)   //TODO: Ask dean what to send in headers
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
        long epochMs = TimeUnit.SECONDS.convert(scheduleInfo.getTime(), scheduleInfo.getTimeUnit());
        return Timestamp.newBuilder().setSeconds(epochMs).build();
    }
}
