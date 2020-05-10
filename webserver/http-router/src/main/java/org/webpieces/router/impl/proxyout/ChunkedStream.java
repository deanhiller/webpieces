package org.webpieces.router.impl.proxyout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.dto.DataFrame;

public class ChunkedStream extends OutputStream {

	//private static final Logger log = LoggerFactory.getLogger(ChunkedStream.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private ByteArrayOutputStream str = new ByteArrayOutputStream();

	private int size;
	private List<DataFrame> cache = new ArrayList<>();
	private boolean isClosed;

	public ChunkedStream(int size) {
		this.size = size;
		this.str = new ByteArrayOutputStream(size);
	}

	@Override
	public void write(int b) throws IOException {
		str.write(b);
		
		if(str.size() >= size) {
			writeDataOut();
		}
	}

	@Override
	public void flush() {
		if(str.size() > 0) {
			writeDataOut();
		}
	}
	
	@Override
	public void close() {
		flush();
		isClosed = true;
	}
	
	private void writeDataOut() {
		byte[] data = str.toByteArray();
		str = new ByteArrayOutputStream();
		DataWrapper body = wrapperFactory.wrapByteArray(data);

		DataFrame frame = new DataFrame();
		frame.setEndOfStream(false);
		frame.setData(body);
		cache.add(frame);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public List<DataFrame> getFrames() {
		List<DataFrame> result = cache;
		cache = new ArrayList<DataFrame>(); //reset the cache for new frames
		return result;
	}

}
