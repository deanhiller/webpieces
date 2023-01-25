package org.webpieces.googlecloud.cloudtasks.api;

public interface TasksClientFactory {

    <T> T createClient(final Class<T> apiInterface);

}
