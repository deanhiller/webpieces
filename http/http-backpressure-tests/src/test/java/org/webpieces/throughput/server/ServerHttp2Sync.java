package org.webpieces.throughput.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.exceptions.SneakyThrow;

import com.webpieces.hpack.api.HpackConfig;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.HpackStatefulParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class ServerHttp2Sync {
	private static final Logger log = LoggerFactory.getLogger(ServerHttp2Sync.class);

	private static final String PREFACE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
	
	@SuppressWarnings("unused")
	public CompletableFuture<InetSocketAddress> start() {
		if(true)
			throw new UnsupportedOperationException("This is broken and needs ot use the http2 engine to work");
		try {
			return startImpl();
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public CompletableFuture<InetSocketAddress> startImpl() throws IOException {
    	log.error("running SYNC HTTP2 SERVER");

		ServerSocket server = new ServerSocket(0);
		
    	Runnable r = new ServerRunnable(server);

    	Thread t = new Thread(r);
    	t.setName("echoServer");
    	t.start();
    	
    	InetSocketAddress address = (InetSocketAddress) server.getLocalSocketAddress();
    	return CompletableFuture.completedFuture(address);
	}

    private static class ServerRunnable implements Runnable {
    	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    	private HpackStatefulParser parser = HpackParserFactory.createStatefulParser(new TwoPools("pl", new SimpleMeterRegistry()), new HpackConfig("deansHpack"));
		private ServerSocket server;

		public ServerRunnable(ServerSocket server) {
			this.server = server;
		}

		public void runImpl() throws IOException {
	        Socket socket = server.accept();
	        InputStream input = socket.getInputStream();
	        OutputStream output = socket.getOutputStream();

	        DataWrapper all = dataGen.emptyWrapper();
	        //read preface....
	        while(true) {
		        byte[] bytes = new byte[1024];
	        	int numRead = input.read(bytes);
	        	DataWrapper data = dataGen.wrapByteArray(bytes, 0, numRead);
	        	all = dataGen.chainDataWrappers(all, data);
	        	if(all.getReadableSize() >= PREFACE.length()) {
	        		List<? extends DataWrapper> split = dataGen.split(all, PREFACE.length());
	        		all = split.get(1);
	        		String preface = split.get(0).createStringFromUtf8(0, PREFACE.length());
	        		if(!preface.equals(PREFACE))
	        			throw new IllegalStateException("Http2 client is not sending preface!!!");
	        		break;
	        	}
	        }
	        
	        //read responses and send responses
	        while(true) {
		        byte[] bytes = new byte[1024];
	        	int numRead = input.read(bytes);
	        	DataWrapper data = dataGen.wrapByteArray(bytes, 0, numRead);
	        	UnmarshalState state = parser.unmarshal(data);
	        	List<Http2Msg> msgs = state.getParsedFrames();
	        	
	        	for(Http2Msg m : msgs) {
	        		if(m instanceof Http2Request) {
		        		Http2Response resp = RequestCreator.createHttp2Response(m.getStreamId());
		        		DataWrapper buffer = parser.marshal(resp);
		        		byte[] b = buffer.createByteArray();
		        		output.write(b);
	        		} else if(m instanceof SettingsFrame) {
	        			dealWithSettings(output, m);
	        		}
	        	}
	        }
		}

		private void dealWithSettings(OutputStream output, Http2Msg m) throws IOException {
			SettingsFrame s = (SettingsFrame) m;
			if(s.isAck()) {
				log.info("received settings ack frame");
				return;
			}
			
			log.info("received client settings="+s);
			SettingsFrame f = new SettingsFrame();
			DataWrapper buffer = parser.marshal(f);
			byte[] b = buffer.createByteArray();
			output.write(b);
		}

		@Override
		public void run() {
			try {
				runImpl();
			} catch (IOException e) {
				log.error("Exception", e);
			}
		}
    }
}
