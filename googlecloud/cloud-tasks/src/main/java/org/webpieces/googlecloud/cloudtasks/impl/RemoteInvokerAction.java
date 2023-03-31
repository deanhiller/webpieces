package org.webpieces.googlecloud.cloudtasks.impl;

public enum RemoteInvokerAction {

    GCP_CREATE_TASK("Create a task"),
    GCP_DELETE_TASK("Deleting a task from a queue");

    //TODO Purging all tasks from a queue
    //TODO Pausing queues
    //TODO Deleting queues

    private String description;

    RemoteInvokerAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
