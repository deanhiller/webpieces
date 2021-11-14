package com.orderlyhealth.googlecloud.cloudtasks;

public interface TasksClientFactory {

    <T> T createClient(final Class<T> apiInterface);

}
