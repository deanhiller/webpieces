package com.webpieces.recording.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.recording.api.Playback;
import org.webpieces.recording.api.Recorder;
import org.webpieces.recording.api.RecordingPlaybackFactory;


public class TestBasicRecordPlayback {

	private static final int BASE2 = 400;
	private static final int BASE1 = 200;

	@Test
	public void testRecordPlayback() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		runRecording(out);
		
		byte[] data = out.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		runPlayback(in);
	}

	private void runPlayback(ByteArrayInputStream in) {
		Playback playback = RecordingPlaybackFactory.createPlayback(in, 1);

		ByteBuffer first = playback.getNextPacket();
		testBufferContents(first, BASE1);
		
		ByteBuffer second = playback.getNextPacket();
		testBufferContents(second, BASE2);
		
		ByteBuffer nullPacket = playback.getNextPacket();
		Assert.assertNull(nullPacket);
	}

	private void runRecording(ByteArrayOutputStream out) {
		Recorder recorder = RecordingPlaybackFactory.createRecorder(out, 1);
		
		ByteBuffer first = createBufferWithBase(200);
		
		recorder.record(first);
		//buffer needs to be still readable/untouched so recorders don't interfere with client logic that uses it..
		testBufferContents(first, BASE1);
		
		ByteBuffer second = createBufferWithBase(BASE2);
		recorder.record(second);
	}

	private ByteBuffer createBufferWithBase(int base) {
		ByteBuffer first = ByteBuffer.allocate(400);
		for(int i = 0; i < 10; i++) {
			first.putInt(base+i);
		}
		first.flip();
		return first;
	}

	private void testBufferContents(ByteBuffer first, int base) {
		for(int i = 0; i < 10; i++) {
			Assert.assertEquals(base+i, first.getInt());
		}
	}
}
