import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;

import com.webpieces.hpack.api.HpackConfig;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.HpackStatefulParser;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestSplitHeaders {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HpackStatefulParser parser;

	@Before
	public void setup() {
		HpackConfig config = new HpackConfig("myhpack");
		config.setLocalMaxFrameSize(50);
		config.setRemoteMaxFrameSize(50);
		parser = HpackParserFactory.createStatefulParser(new TwoPools("pl", new SimpleMeterRegistry()), config);
	}
	
	@Test
	public void testBytesParsedBySplittingHeaders() {
		Http2Request req = new Http2Request();
		req.setStreamId(1);
		req.addHeader(new Http2Header(Http2HeaderName.SCHEME, "/"));
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, "/"));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, "/"));
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "/"));
		for(int i = 0; i < 20; i++) {
			req.addHeader(new Http2Header("someHeader"+i, "value"));
		}
		
		DataWrapper data = parser.marshal(req);
		byte[] first = data.readBytesAt(0, 60);
		DataWrapper data1 = dataGen.wrapByteArray(first);
		
		UnmarshalState state = parser.unmarshal(data1);
		Assert.assertEquals(0, state.getParsedFrames().size());
		Assert.assertEquals(0, state.getNumBytesJustParsed());
		Assert.assertEquals(first.length, state.getLeftOverDataSize());
		
		byte[] second = data.readBytesAt(60, data.getReadableSize()-60);
		DataWrapper data2 = dataGen.wrapByteArray(second);
		
		state = parser.unmarshal(data2);
		Assert.assertEquals(1, state.getParsedFrames().size());
		Assert.assertEquals(data.getReadableSize(), state.getNumBytesJustParsed());
		Assert.assertEquals(0, state.getLeftOverDataSize());		
		
	}
}
