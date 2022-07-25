package org.webpieces.recording.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.webpieces.recording.api.Playback;
import org.digitalforge.sneakythrow.SneakyThrow;

public class PlaybackImpl implements Playback {

	private ReadableByteChannel channel;
	private int version;

	public PlaybackImpl(InputStream in, int version) {
		if(version != 1)
			throw new IllegalArgumentException("only version=1 supported right now");
		this.version = version;
		channel = Channels.newChannel(in);
	}

	@Override
	public ByteBuffer getNextPacket() {
		try {
			if(version == 1)
				return startImpl();
			else
				throw new IllegalArgumentException("version="+version+" not supported");
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public ByteBuffer startImpl() throws IOException {
		ByteBuffer sizeBuf = ByteBuffer.allocate(4);
		int bytesRead = channel.read(sizeBuf);
		if(bytesRead < 0)
			return null;
		
		sizeBuf.flip();
		
		int size = sizeBuf.getInt();
		ByteBuffer data = ByteBuffer.allocate(size);
		channel.read(data);
		data.flip();
		
		ByteBuffer trailerBuf = ByteBuffer.allocate(7);
		channel.read(trailerBuf);
		trailerBuf.flip();
		
		byte[] trailer = new byte[7];
		trailerBuf.get(trailer);
		//If the trailer is not there, fail fast so they don't end up debugging the wrong issue...(which is a badly written file)
		for(int i = 0; i < trailer.length; i++) {
			int d = trailer[i];
			if(i != d)
				throw new IllegalStateException("corruption on input stream.  read in size="+size+" but the trailer at the end is not matching");
		}
		
		return data;
	}

}
