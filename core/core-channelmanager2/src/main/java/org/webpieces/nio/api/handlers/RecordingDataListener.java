package org.webpieces.nio.api.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.recording.api.Recorder;
import org.webpieces.recording.api.RecordingPlaybackFactory;
import org.webpieces.util.file.FileFactory;

public class RecordingDataListener implements DataListener {

	private DataListener realListener;
	private Map<Channel, Recorder> channelToRecorder = new Hashtable<>();
	private String fileSuffix;
	private File baseDir;
	
	public RecordingDataListener(String fileSuffix, DataListener realListener) {
		this.baseDir = FileFactory.getBaseWorkingDir();
		this.fileSuffix = fileSuffix;
		this.realListener = realListener;
	}

	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		Recorder recorder = getOrCreate(channel);
		recorder.record(b);
		return realListener.incomingData(channel, b);
	}

	private Recorder getOrCreate(Channel channel) {
		Recorder recorder = channelToRecorder.get(channel);
		if(recorder == null) {
			recorder = createRecorder(channel);
			channelToRecorder.put(channel, recorder);
		}
		return recorder;
	}

	private Recorder createRecorder(Channel channel) {
		try {
			File f = FileFactory.newFile(baseDir, fileSuffix+channel.getChannelId()+".recording");
			FileOutputStream str = new FileOutputStream(f);
			return RecordingPlaybackFactory.createRecorder(str, 1);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void farEndClosed(Channel channel) {
		realListener.farEndClosed(channel);
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		realListener.failure(channel, data, e);
	}
	
}
