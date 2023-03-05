package org.webpieces.googlecloud.cloudtasks.api;

public class GCPCloudTaskConfig {
    private String projectId;
    private String location;

    public GCPCloudTaskConfig(String projectId, String location) {
        this.projectId = projectId;
        this.location = location;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
