package org.webpieces.googlecloud.cloudtasks;

import com.google.cloud.tasks.v2.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.google.protobuf.ByteString;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.QueueClientCreator;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.googlecloud.cloudtasks.api.Scheduler;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is for when we have to talk to google to find out what google returns so we can
 * simulate google better
 */
public class IntegGoogleTest {

    private BusinessLogicForTest businessLogic;

    @Before
    public void setup() {
        Module testModule = new FakeProdModule();
        Injector injector = Guice.createInjector(testModule);
        businessLogic = injector.getInstance(BusinessLogicForTest.class);
    }

    @Test
    public void talkToGoogle() {
        Map<String, String> headerMap = new HashMap<>();
        Context.put(Context.HEADERS, headerMap);

        businessLogic.runDeveloperExperience();
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
