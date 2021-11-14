package webpiecesxxxxxpackage.service;

import javax.inject.Inject;
import java.net.InetSocketAddress;

public class YourServiceDirectory {

    private InetSocketAddress remoteSvc = new InetSocketAddress("localhost", LocalService.REMOTE_SVC.getDevHttpsPort());
    private InetSocketAddress addresService = new InetSocketAddress("localhost", LocalService.ADDRESS_SVC.getDevHttpsPort());

    @Inject
    public YourServiceDirectory() {

        if (false) {
            remoteSvc = new InetSocketAddress("remotesvc.orderlyhealth.com", 443);
            addresService = new InetSocketAddress("addresses.orderlyhealth.com", 443);
        }
    }

    public InetSocketAddress getRemoteSvc() {
        return remoteSvc;
    }

    public InetSocketAddress getAddresService() {
        return addresService;
    }
}
