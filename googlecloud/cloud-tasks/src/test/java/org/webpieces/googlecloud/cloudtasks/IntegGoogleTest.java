package org.webpieces.googlecloud.cloudtasks;

import com.google.cloud.tasks.v2.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.cloudtasks.api.QueueClientCreator;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This is for when we have to talk to google to find out what google returns so we can
 * simulate google better
 */
public class IntegGoogleTest {

    private QueueClientCreator instance;

    @Before
    public void setup() {
        Module module = new FakeProdModule();
        Injector injector = Guice.createInjector(module);
        instance = injector.getInstance(QueueClientCreator.class);
    }

    @Test
    public void talkToGoogle() {
        //write simulation code here

        String projectId = "tray-dineqa";
        String locationId = "us-west1";
        String queueId = "com-tray-api-publish-PublishApi-test";

        try {
            createTask(projectId, locationId, queueId);
        } catch (IOException e) {
            throw SneakyThrow.sneak(e);
        }

    }

    // Create a task with a HTTP target using the Cloud Tasks client.
    public static void createTask(String projectId, String locationId, String queueId)
            throws IOException {

        // Instantiates a client.
        try (CloudTasksClient client = CloudTasksClient.create()) {
            String url = "https://reqres.in/api/users";
            String payload = "{\n" +
                    "    \"name\": \"morpheus\",\n" +
                    "    \"job\": \"leader\"\n" +
                    "}";

            // Construct the fully qualified queue name.
            String queuePath = QueueName.of(projectId, locationId, queueId).toString();

            for(int i = 0 ; i < 10 ; i++) {
                // Construct the task body.
                Task.Builder taskBuilder =
                        Task.newBuilder()
                                .setName(TaskName.format(projectId,locationId,queueId,"task-" + i))
                                //.setScheduleTime(Timestamp.newBuilder().setSeconds(3600).build())
                                .setHttpRequest(
                                        HttpRequest.newBuilder()
                                                .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                                                .setUrl(url)
                                                .setHttpMethod(HttpMethod.POST)
                                                .build());

                // Send create task request.
                Task task = client.createTask(queuePath, taskBuilder.build());

                System.out.println("Task created: " + task.getName() + " ResponseCount: " + task.getResponseCount());
            }
            //Thread.currentThread().sleep(300*1000L);
        }
    }

}
