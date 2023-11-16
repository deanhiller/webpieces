package org.webpieces.util;

public class HostWithPort {
    private String hostOrIpAddress;
    private final int port;

    public HostWithPort(int port) {
        this.port = port;
    }

    public HostWithPort(String hostOrIpAddress, int port) {
        this.hostOrIpAddress = hostOrIpAddress;
        this.port = port;
    }

    public String getHostOrIpAddress() {
        return hostOrIpAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "HostWithPort{" +
                "hostOrIpAddress='" + hostOrIpAddress + '\'' +
                ", port=" + port +
                '}';
    }
}
