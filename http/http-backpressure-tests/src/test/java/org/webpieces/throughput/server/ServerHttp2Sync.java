package org.webpieces.throughput.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackConfig;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.HpackStatefulParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class ServerHttp2Sync {
	private static final Logger log = LoggerFactory.getLogger(ServerHttp2Sync.class);

	public CompletableFuture<InetSocketAddress> start() {
		try {
			return startImpl();
		} catch (IOException e) {
			throw new RuntimeException(e);
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

    	private HpackStatefulParser parser = HpackParserFactory.createStatefulParser(new BufferCreationPool(), new HpackConfig("deansHpack"));
		private ServerSocket server;

		public ServerRunnable(ServerSocket server) {
			this.server = server;
		}

		public void runImpl() throws IOException {
	        Socket socket = server.accept();
	        InputStream input = socket.getInputStream();
	        OutputStream output = socket.getOutputStream();

	        while(true) {
		        byte[] bytes = new byte[1024];
	        	int numRead = input.read(bytes);
	        	DataWrapper data = dataGen.wrapByteArray(bytes, 0, numRead);
	        	UnmarshalState state = parser.unmarshal(data);
	        	List<Http2Msg> msgs = state.getParsedFrames();
	        	
	        	for(Http2Msg m : msgs) {
	        		Http2Response resp = RequestCreator.createHttp2Response(m.getStreamId());
	        		DataWrapper buffer = parser.marshal(resp);
	        		byte[] b = buffer.createByteArray();
	        		output.write(b);
	        	}
	        }
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
