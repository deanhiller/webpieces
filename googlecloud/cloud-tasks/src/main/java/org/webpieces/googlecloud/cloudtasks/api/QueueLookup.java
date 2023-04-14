package org.webpieces.googlecloud.cloudtasks.api;

public interface QueueLookup {

    /**
     * Each environment has a different queue so lookup the queue if they did a
     * queue name overrdie
     */
    public String fetchQueueName(String queueKey);

}
