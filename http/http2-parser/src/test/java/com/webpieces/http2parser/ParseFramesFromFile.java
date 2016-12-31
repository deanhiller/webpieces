package com.webpieces.http2parser;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class ParseFramesFromFile {
    public static void main(String[] args) throws Exception {
        File aFile = new File("httpSock-oneTimer.recording");
        FileInputStream inFile = new FileInputStream(aFile);
        FileChannel inChannel = inFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate((int) aFile.length());
        Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());
        DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
        Decoder decoder = new Decoder(4096, 4096);
        List<Http2Setting> settings = new ArrayList<>();

        settings.add(new Http2Setting(SettingsParameter.SETTINGS_MAX_FRAME_SIZE, 16384L));

        while (inChannel.read(buf) != -1) {
            buf.flip();
            buf.getInt();
            ParserResult result = parser.parse(dataGen.wrapByteBuffer(buf.slice()), dataGen.emptyWrapper(), decoder, settings);
            List<AbstractHttp2Frame> frames = result.getParsedFrames();
            System.out.print(frames.toString());
            buf.clear();
        }
        inFile.close();
    }
}
