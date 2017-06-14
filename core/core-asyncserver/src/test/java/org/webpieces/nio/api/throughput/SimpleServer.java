package org.webpieces.nio.api.throughput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    public static void main(String[] args) throws Exception {
    	Runnable r = new MyRunnable();

    	Thread t = new Thread(r);
    	t.start();
    	
    	Thread.sleep(1000);
    	
        Socket socket = new Socket("127.0.0.1", 6666);
        InputStream input = socket.getInputStream();
        long total = 0;
        long start = System.currentTimeMillis();

        byte[] bytes = new byte[32*1024]; // 32K
        for(int i=1;;i++) {
            int read = input.read(bytes);
            if (read < 0) break;
            total += read;
            if (i % 500000 == 0) {
                long cost = System.currentTimeMillis() - start;
                System.out.printf("Read %,d bytes, speed: %,d MB/s%n", total, total/cost/1000);
            }
        }

    }
    
    private static class MyRunnable implements Runnable {

		public void runImpl() throws IOException {
	        ServerSocket server = new ServerSocket(6666);
	        Socket socket = server.accept();
	        OutputStream output = socket.getOutputStream();

	        byte[] bytes = new byte[32*1024]; // 32K
	        while (true) {
	            output.write(bytes);
	        }			
		}

		@Override
		public void run() {
			try {
				runImpl();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
}

