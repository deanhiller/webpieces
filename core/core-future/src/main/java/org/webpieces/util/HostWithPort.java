package org.webpieces.util;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostWithPort that = (HostWithPort) o;
        return port == that.port && Objects.equals(hostOrIpAddress, that.hostOrIpAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostOrIpAddress, port);
    }
}
