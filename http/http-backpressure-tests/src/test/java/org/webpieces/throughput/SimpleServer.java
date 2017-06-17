package org.webpieces.throughput;

import java.net.InetSocketAddress;

public class SimpleServer {

    public static void main(String[] args) throws Exception {
    	
    	ServerHttp1_1Sync server = new ServerHttp1_1Sync();
    	server.start();
    	
    	Thread.sleep(1000);
    	
    	ClientHttp1_1Sync client = new ClientHttp1_1Sync(new InetSocketAddress("127.0.0.1", 6666));
    	client.start();
    }

}

