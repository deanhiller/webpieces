package org.webpieces.throughput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2translations.api.Http1_1ToHttp2;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.time.MsgRateRecorder;

import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class ClientHttp1_1Sync {
	private static final Logger log = LoggerFactory.getLogger(ClientHttp1_1Sync.class);

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private InetSocketAddress svrAddress;
	private HttpStatefulParser parser = HttpParserFactory.createStatefulParser(new BufferCreationPool());

	public ClientHttp1_1Sync(InetSocketAddress svrAddress) {
		this.svrAddress = svrAddress;
	}

	public void start() {
		try {
	    	log.error("SYNC CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
	    	log.info("info messages automatically show up in black");
	    	
			startImpl();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void startImpl() throws UnknownHostException, IOException {
        @SuppressWarnings("resource")
		Socket socket = new Socket(svrAddress.getHostName(), svrAddress.getPort());
        OutputStream output = socket.getOutputStream();
        
        Runnable client = new ClientWriter(parser, output);
        Thread t1 = new Thread(client);
        t1.setName("clientWriter");
        t1.start();
        
        InputStream input = socket.getInputStream();
        
        MsgRateRecorder recorder = new MsgRateRecorder(10);

        while(true) {
            byte[] bytes = new byte[1024];
            int read = input.read(bytes);
            if (read < 0) break;
            
            DataWrapper dataWrapper = dataGen.wrapByteArray(bytes, 0, read);
            List<HttpPayload> messages = parser.parse(dataWrapper);
            
            //simulate going all the way to http2 like the other test does as well
            for(HttpPayload p : messages) {
            	HttpResponse resp = (HttpResponse) p;
            	Http2Msg translate = Http1_1ToHttp2.responseToHeaders(resp);
            	translate.getMessageType();
            	recorder.increment();
            }
        }		
	}

    private static class ClientWriter implements Runnable {

		private HttpStatefulParser parser2;
		private OutputStream output;

		public ClientWriter(HttpStatefulParser parser, OutputStream output) {
			parser2 = parser;
			this.output = output;
		}

		@Override
		public void run() {
			try {
				runImpl();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		public void runImpl() throws IOException {
			int counter = 0;
			while(true) {
				counter++;
				HttpRequest request = RequestCreator.createHttp1_1Request();
				ByteBuffer buffer = parser2.marshalToByteBuffer(request);
				byte[] b = new byte[buffer.remaining()];
				buffer.get(b);
				output.write(b);
			}
		}
    	
    }
}
