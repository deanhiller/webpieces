package org.webpieces.googlecloud.cloudtasks.api;

public class JobReference {
    private String taskId;

    public JobReference() {}

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "JobReference{" +
                "taskId='" + taskId + '\'' +
                '}';
    }
}
