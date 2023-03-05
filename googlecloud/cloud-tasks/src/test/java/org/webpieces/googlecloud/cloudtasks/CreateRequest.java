package org.webpieces.googlecloud.cloudtasks;

public class CreateRequest {
    private String name;

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    private String job;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

