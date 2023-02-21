package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.CloudTasksSettings;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.googlecloud.cloudtasks.api.TasksClientFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;

public class GCPTasksClientFactory implements TasksClientFactory {

    private CloudTasksClient cloudTasksClient;

    public GCPTasksClientFactory(){
        this.cloudTasksClient = createCloudTasksClient();
    }

    public <T> T createClient(final Class<T> apiInterface) {

        //TODO : ask dean what to write here

        return (T) Proxy.newProxyInstance(
                apiInterface.getClassLoader(),
                new Class[]{apiInterface},
                new GCPTasksClient(cloudTasksClient)
        );
    }

    private CloudTasksClient createCloudTasksClient() {

        try {
            return CloudTasksClient.create(CloudTasksSettings.newBuilder().build());
        } catch (IOException ex) {
            throw SneakyThrow.sneak(ex);
        }

    }
}
