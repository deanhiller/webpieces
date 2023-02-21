package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
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

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.util.futures.XFuture;

import static javax.swing.text.html.CSS.getURL;

public class GCPTaskClient {

    private static Logger log = LoggerFactory.getLogger(GCPTaskClient.class);
    private CloudTasksClient cloudTasksClient;
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String LOCATION = "LOCATION";
    private Map<String, String> cloudConfig;

    public GCPTaskClient(String projectId, String location) {
        this.cloudTasksClient = createCloudTasksClient();
        cloudConfig = setCloudSettings(projectId, location);
    }

    private Map<String,String> setCloudSettings(String projectId, String location) {
        Map<String,String> map = new HashMap<>();
        map.put(PROJECT_ID,projectId);
        map.put(LOCATION,location);
        return map;
    }

    public void createTask(InetSocketAddress addr, HttpMethod httpMethod, String path, String payload) {

        Endpoint endpoint = new Endpoint(addr, httpMethod.toString(), path);

        String url = getURL(endpoint,path);

        QueueName queueName = QueueName.of(cloudConfig.get(PROJECT_ID), cloudConfig.get(LOCATION),"");

        try {
            createTaskImpl(queueName, url, httpMethod, payload);
        } catch (IOException e) {
            throw SneakyThrow.sneak(e);
        }

        /*
        XFuture<String> stringXFuture = client.sendHttpRequest(bodyAsText, endpoint);
        stringXFuture.exceptionally( (e) -> {
            log.error("Exception queueing the request", e);
            return null;
        });
        Context.clear();
         */
        

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

    // Create a task with a HTTP target using the Cloud Tasks client.
    private void createTaskImpl(QueueName queue, String url, HttpMethod httpMethod, String payload) {

        // Instantiates a client.
        try(CloudTasksClient client = createCloudTasksClient()) {

            log.info("Got queue: " + queue);

            // Construct the task body.
            HttpRequest request = HttpRequest.newBuilder()
                    .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                    .setUrl(url)
                    .setHttpMethod(getCloudTaskHttpMethod(httpMethod))
                    //.putAllHeaders(headers)   //TODO: What to send in headers now
                    .build();

            Duration deadline = Duration.newBuilder().setSeconds(1020).build(); //set 17 minutes (longer than cloud run timeout)
            Task.Builder taskBuilder = Task.newBuilder().setHttpRequest(request).setDispatchDeadline(deadline);

            // Send create task request.
            Task task = client.createTask(queue, taskBuilder.build());

            log.info("Task created: " + task.getName());

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
    }
}
