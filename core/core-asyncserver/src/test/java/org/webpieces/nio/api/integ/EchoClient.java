package org.webpieces.nio.api.integ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.throughput.BytesRecorder;

public class EchoClient {

	private static final Logger log = LoggerFactory.getLogger(EchoClient.class);
	private BytesRecorder recorder = new BytesRecorder();
	
	public void start(int port) {
		try {
			startImpl(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    public void startImpl(int port) throws IOException {
        log.info("startin client on port="+port);

        @SuppressWarnings("resource")
		Socket s = new Socket();
        s.connect(new InetSocketAddress(port));
        
        InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();

        Runnable r = new Runnable() {
			@Override
			public void run() {
				readForever(in);
			}
        };
        Thread t = new Thread(r, "clientIn");
        t.start();
        
        Runnable r2 = new Runnable() {
			@Override
			public void run() {
				writeForever(out);
			}
        };
        Thread t2 = new Thread(r2, "clientOut");
        t2.start();
        
        log.info("writer started");
        recorder.start();
	}

	private void readForever(InputStream in) {
		try {
			while(true) {
				byte[] data = new byte[17000];
				int size = in.read(data);
				if(size < 0)
					throw new IllegalStateException("something when wrong");
				recorder.recordBytes(size);
			}
		} catch(IOException e) {
			log.error("exception reading", e);
		}
	}
	
	
	private void writeForever(OutputStream out) {
		try {
//			int counter = 0;
			byte[] data = new byte[17000];
			while(true) {
//				counter++;
				out.write(data);
				
//				if(counter % 5000 == 0) {
//					log.info("still writing packets");
////					Thread.sleep(1000);
//				}
			}
		} catch(IOException e) {
			log.error("exception reading", e);
//		} catch (InterruptedException e) {
//			log.error("exception", e);
		}
	}

}