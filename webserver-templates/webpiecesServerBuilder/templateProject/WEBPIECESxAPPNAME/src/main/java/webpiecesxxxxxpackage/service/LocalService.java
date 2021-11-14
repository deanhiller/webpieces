package webpiecesxxxxxpackage.service;

public enum LocalService {
    REMOTE_SVC(8020, "remotesvc"),
    ADDRESS_SVC(8021, "addresssvc");

    private final int devHttpPort;
    private final int devHttpsPort;
    private final String serviceName;

    private LocalService(int httpPort, String name) {
        this.devHttpPort = httpPort;
        this.devHttpsPort = httpPort+1000;
        this.serviceName = name;
    }

    public int getDevHttpPort() {
        return devHttpPort;
    }

    public int getDevHttpsPort() {
        return devHttpsPort;
    }

    public String getServiceName() {
        return serviceName;
    }
}
