package org.webpieces.recording.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.webpieces.recording.api.Recorder;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;

public class RecorderImpl implements Recorder {

	private DataWrapperGenerator gen;
	private WritableByteChannel channel;
	private int version;

	public RecorderImpl(OutputStream out, DataWrapperGenerator gen, int version) {
		if(version != 1)
			throw new IllegalArgumentException("only version=1 supported right now");
		channel = Channels.newChannel(out);
		this.gen = gen;
		this.version = version;
	}

	@Override
	public void record(ByteBuffer b) {
		try {
			if(version == 1)
				recordImpl(b);
			else
				throw new IllegalArgumentException("version="+version+" not supported");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void recordImpl(ByteBuffer b) throws IOException {
		DataWrapper wrapper = gen.wrapByteBuffer(b);
		int size = wrapper.getReadableSize();

		ByteBuffer sizeBuf = ByteBuffer.allocate(4);
		sizeBuf.putInt(size);
		sizeBuf.flip();
		
		channel.write(sizeBuf);
		
		ByteBuffer shallowCopy = b.duplicate();
		channel.write(shallowCopy);
		
		byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6 };
		ByteBuffer trailer = ByteBuffer.wrap(data);
		channel.write(trailer);
	}

}
