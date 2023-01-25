package org.webpieces.googlecloud.cloudtasks.localimpl;

import org.webpieces.googlecloud.cloudtasks.api.TasksClientFactory;

public class LocalTasksClientFactory implements TasksClientFactory {
    @Override
    public <T> T createClient(Class<T> apiInterface) {
        return null;
    }
}
