package org.webpieces.recording.api;

import java.io.InputStream;
import java.io.OutputStream;

import org.webpieces.recording.impl.PlaybackImpl;
import org.webpieces.recording.impl.RecorderImpl;

import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;

public class RecordingPlaybackFactory {

	private static final DataWrapperGenerator gen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	public static Recorder createRecorder(OutputStream f, int version) {
		return new RecorderImpl(f, gen, version);
	}
	
	public static Playback createPlayback(InputStream in, int version) {
		return new PlaybackImpl(in, version);
	}
}
