package org.webpieces.throughput.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.throughput.RequestCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.time.RateRecorder;

import com.webpieces.hpack.api.HpackConfig;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.HpackStatefulParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Http2SynchronousClient implements SynchronousClient {
	private static final Logger log = LoggerFactory.getLogger(Http2SynchronousClient.class);

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private HpackStatefulParser parser = HpackParserFactory.createStatefulParser(new BufferCreationPool(), new HpackConfig("clientHpack"));

	@SuppressWarnings("unused")
	@Override
	public void start(InetSocketAddress svrAddress) {
		if(true)
			throw new UnsupportedOperationException("This is broken and needs ot use the http2 engine to work");
		try {
	    	log.error("SYNC HTTP2 CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
	    	log.info("info messages automatically show up in black");
	    	
			startImpl(svrAddress);
		} catch (Throwable e) {
			throw new RuntimeException(e);
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
            UnmarshalState state = parser.unmarshal(dataWrapper);
            List<Http2Msg> messages = state.getParsedFrames();
            
            //simulate going all the way to http2 like the other test does as well
            for(Http2Msg p : messages) {
            	Http2Response resp = (Http2Response) p;
            	resp.getStreamId();
            	recorder.increment();
            }
        }		
	}

    private static class ClientWriter implements Runnable {

		private HpackStatefulParser parser2;
		private OutputStream output;

		public ClientWriter(HpackStatefulParser parser, OutputStream output) {
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
			int streamId = 1;
			while(true) {
				Http2Request request = RequestCreator.createHttp2Request();
				request.setStreamId(streamId);
				streamId = streamId + 2;
				DataWrapper buffer = parser2.marshal(request);
				byte[] b = buffer.createByteArray();
				output.write(b);
			}
		}
    	
    }
}
