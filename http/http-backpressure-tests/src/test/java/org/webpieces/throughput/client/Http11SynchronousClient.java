package org.webpieces.throughput.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2translations.api.Http11ToHttp2;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.throughput.RequestCreator;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.util.time.RateRecorder;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class Http11SynchronousClient implements SynchronousClient {
	private static final Logger log = LoggerFactory.getLogger(Http11SynchronousClient.class);

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private HttpStatefulParser parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));

	@Override
	public void start(InetSocketAddress svrAddress) {
		try {
	    	log.error("SYNC HTTP1.1 CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
	    	log.info("info messages automatically show up in black");
	    	
			startImpl(svrAddress);
		} catch (Throwable e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public void startImpl(InetSocketAddress svrAddress) throws UnknownHostException, IOException {
        @SuppressWarnings("resource")
		Socket socket = new Socket(svrAddress.getHostName(), svrAddress.getPort());
        OutputStream output = socket.getOutputStream();
        
        Runnable client = new ClientWriter(parser, output);
        Thread t1 = new Thread(client);
        t1.setName("clientWriter");
        t1.start();
        
        InputStream input = socket.getInputStream();
        
        RateRecorder recorder = new RateRecorder(10);

        while(true) {
            byte[] bytes = new byte[1024];
            int read = input.read(bytes);
            if (read < 0) break;
            
            DataWrapper dataWrapper = dataGen.wrapByteArray(bytes, 0, read);
            List<HttpPayload> messages = parser.parse(dataWrapper);
            
            //simulate going all the way to http2 like the other test does as well
            for(HttpPayload p : messages) {
            	HttpResponse resp = (HttpResponse) p;
            	Http2Msg translate = Http11ToHttp2.responseToHeaders(resp);
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
				log.error("exception", e);
			}
		}
		
		public void runImpl() throws IOException {
			while(true) {
				HttpRequest request = RequestCreator.createHttp1_1Request();
				ByteBuffer buffer = parser2.marshalToByteBuffer(request);
				byte[] b = new byte[buffer.remaining()];
				buffer.get(b);
				output.write(b);
			}
		}
    	
    }
}
