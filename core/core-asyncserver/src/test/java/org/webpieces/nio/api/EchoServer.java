package org.webpieces.nio.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServer {

	private static final Logger log = LoggerFactory.getLogger(EchoServer.class);
	
	public void start() {
		try {
			startImpl();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    public void startImpl() throws IOException {
        // create socket
        int port = 4444;
        @SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(port);
        log.info("started server on port="+port);

        // repeatedly wait for connections, and process
        while (true) {

            // a "blocking" call which waits until a connection is requested
            Socket clientSocket = serverSocket.accept();
            System.err.println("Accepted connection from client");

            try (
	            InputStream inputStream = clientSocket.getInputStream();
	            OutputStream out = clientSocket.getOutputStream()) {
	            while(true) {
	            	byte[] data = new byte[16000];
	            	inputStream.read(data);
	            	out.write(data);
	            }
            } finally {
            	clientSocket.close();
            }
        }
	}

	public static void main(String[] args) {
    	new EchoServer().start();
    }
}
