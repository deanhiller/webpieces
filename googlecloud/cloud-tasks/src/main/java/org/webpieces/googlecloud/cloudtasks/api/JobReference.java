package org.webpieces.googlecloud.cloudtasks.api;

public class JobReference {
    private String taskId;

    public JobReference() {}

    public JobReference(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
