package com.webpieces.http2parser;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class ParseFramesFromFile {
    public static void main(String[] args) throws Exception {
        File aFile = new File("httpSock-oneTimer.recording");
        FileInputStream inFile = new FileInputStream(aFile);
        FileChannel inChannel = inFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate((int) aFile.length());
        HpackParser parser = HpackParserFactory.createParser(new BufferCreationPool(), true);
        DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

        while (inChannel.read(buf) != -1) {
            buf.flip();
            buf.getInt();
        	UnmarshalState result = parser.prepareToUnmarshal(4096, 4096, 16384L);
            result = parser.unmarshal(result, dataGen.wrapByteBuffer(buf.slice()));
            List<Http2Msg> frames = result.getParsedFrames();
            System.out.print(frames.toString());
            buf.clear();
        }
        inFile.close();
    }
}
