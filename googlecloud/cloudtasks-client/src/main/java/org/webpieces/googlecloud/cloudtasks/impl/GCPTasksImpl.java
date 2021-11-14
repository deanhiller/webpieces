package com.orderlyhealth.googlecloud.cloudtasks.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orderlyhealth.api.Endpoint;
import com.orderlyhealth.api.Environment;
import com.orderlyhealth.googlecloud.cloudtasks.GCPTasks;

@Singleton
public class GCPTasksImpl implements GCPTasks {

    private static final Logger log = LoggerFactory.getLogger(GCPTasksImpl.class);
    private static final String PROD_PREFIX = "prod-";
    private static final String DEMO_PREFIX = "demo-";
    private static final String STAGING_PREFIX = "staging-";

    private final Environment environment;
    private final ObjectMapper mapper;

    @Inject
    public GCPTasksImpl(final Environment environment, final ObjectMapper mapper) {
        this.environment = environment;
        this.mapper = mapper;
    }

    @Override
    public String getPrefixedQueueId(String queueId) {

        String prefix = "";

        switch(environment) {

            case PRODUCTION:
                prefix = PROD_PREFIX;
                break;

            case DEMO:
                prefix = DEMO_PREFIX;
                break;

            case STAGING:
                prefix = STAGING_PREFIX;
                break;

        }

        return prefix + queueId;

    }

    @Override
    public void createTask(Endpoint endpoint, String queueId, Map<String,String> headers, Object jacksonObj) {

        if("localhost".equals(endpoint.getApiAddress().getHostString())) {
            throw new IllegalArgumentException("localhost tasks shouldn't use GCPTasksImpl!");
        }

        try {

            QueueName queue = getQueue(queueId);
            String url = "https://" + endpoint.getApiAddress().getHostString() + endpoint.getPath();
            String payload = mapper.writeValueAsString(jacksonObj);

            log.info("Submitting task to queue {}", queue);
            log.info("Wrote: " + jacksonObj.toString() + " to a string");

            createTaskImpl(queue, url, payload, headers);

        }
        catch(IOException ex) {
            log.error("Exception submitting task!", ex);
            throw new RuntimeException(ex);
        }

    }

    // Create a task with a HTTP target using the Cloud Tasks client.
    private void createTaskImpl(QueueName queue, String url, String payload, Map<String,String> headers) throws IOException {

        // Instantiates a client.
        try(CloudTasksClient client = createCloudTasksClient()) {

            log.info("Got queue: " + queue);

            // Construct the task body.
            HttpRequest request = HttpRequest.newBuilder()
                .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                .setUrl(url)
                .setHttpMethod(HttpMethod.POST)
                .putAllHeaders(headers)
                .build();

            Duration deadline = Duration.newBuilder().setSeconds(1020).build(); //set 17 minutes (longer than cloud run timeout)
            Task.Builder taskBuilder = Task.newBuilder().setHttpRequest(request).setDispatchDeadline(deadline);

            // Send create task request.
            Task task = client.createTask(queue, taskBuilder.build());

            log.info("Task created: " + task.getName());
            log.info("New curl request created: " + getCurlCommand(request, url, payload));

        }

    }

    public CloudTasksClient createCloudTasksClient() throws IOException {
        return CloudTasksClient.create();
    }

    private QueueName getQueue(final String queueId) {

        QueueName queue;

        if(queueId.startsWith(STAGING_PREFIX)) {
            queue = QueueName.of("staging-orderly-gcp", "us-west2", queueId);
        }
        else if(queueId.startsWith(DEMO_PREFIX)) {
            queue = QueueName.of("demoenv-orderly-gcp", "us-west2", queueId);
        }
        else {
            queue = QueueName.of("orderly-gcp", "us-central1", queueId);
        }

        return queue;

    }

    private String getCurlCommand(final HttpRequest request, final String url, final String payload) {

        StringBuilder sb = new StringBuilder();

        sb.append("curl -X ");
        sb.append(request.getHttpMethod());
        sb.append(' ');

        for(Map.Entry<String,String> entry : request.getHeadersMap().entrySet()) {
            sb.append("-H \"").append(entry.getKey()).append(':').append(entry.getValue()).append("\" ");
        }

        sb.append("--data '");
        sb.append(payload);
        sb.append("' \"");
        sb.append(url);
        sb.append('"');

        return sb.toString();

    }

}
