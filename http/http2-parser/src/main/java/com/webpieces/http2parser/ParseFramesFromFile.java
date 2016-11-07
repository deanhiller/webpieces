package com.webpieces.http2parser;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Settings;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFramesFromFile {
    public static void main(String[] args) throws Exception {
        File aFile = new File("httpSock-oneTimer.recording");
        FileInputStream inFile = new FileInputStream(aFile);
        FileChannel inChannel = inFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate((int) aFile.length());
        Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());
        DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
        Decoder decoder = new Decoder(4096, 4096);
        Http2SettingsMap settings = new Http2SettingsMap();

        settings.put(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE, 16384L);

        while (inChannel.read(buf) != -1) {
            buf.flip();
            buf.getInt();
            ParserResult result = parser.parse(dataGen.wrapByteBuffer(buf.slice()), dataGen.emptyWrapper(), decoder, settings);
            List<Http2Frame> frames = result.getParsedFrames();
            System.out.print(frames.toString());
            buf.clear();
        }
        inFile.close();
    }
}
