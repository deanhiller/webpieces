package org.webpieces.webserver.beans;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.mock.lib.MockExecutor;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.basic.app.biz.UserDto;
import org.webpieces.webserver.mock.MockSomeLib;
import org.webpieces.webserver.mock.MockSomeOtherLib;
import org.webpieces.webserver.test.AbstractWebpiecesTest;
import org.webpieces.webserver.test.MockTcpChannel;
import org.webpieces.webserver.test.ResponseWrapper;
import org.webpieces.webserver.test.http11.Requests;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;

public class TestBeansAndSplit extends AbstractWebpiecesTest {
	
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private MockSomeLib mockSomeLib = new MockSomeLib();
	private MockSomeOtherLib mockSomeOtherLib = new MockSomeOtherLib();
	private MockExecutor mockExecutor = new MockExecutor();

	private MockTcpChannel channel = new MockTcpChannel();
	private DataListener dataListener;
	private DataWrapper dataReceived = dataGen.emptyWrapper();

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		VirtualFileClasspath metaFile = new VirtualFileClasspath("beansMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(getOverrides(false), new AppOverridesModule(), false, metaFile);
		webserver.start();
		
		channel.setDataListener(new DataReceiver());
		ConnectionListener listener = mgr.getHttpConnection();
		CompletableFuture<DataListener> future = listener.connected(channel, true);
		dataListener = future.get(2, TimeUnit.SECONDS);
	}

	private class DataReceiver implements DataListener {
		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			DataWrapper wrapper = dataGen.wrapByteBuffer(b);
			dataReceived = dataGen.chainDataWrappers(dataReceived, wrapper);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void farEndClosed(Channel channel) {
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
		}
	}
	
	@Test
	public void testIncomingDataAndDataSeperate() {
		HttpFullRequest req = Requests.createPostRequest("/postArray2",
				"user.accounts[1].name", "Account2Name",
				"user.accounts[1].color", "green",
				"user.accounts[2].addresses[0].number", "56",
				"user.firstName", "D&D",
				"user.lastName", "Hiller",
				"user.fullName", "Dean Hiller"
		);

		DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
		HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
		MarshalState state = parser.prepareToMarshal();
		ByteBuffer buffer = parser.marshalToByteBuffer(state, req.getRequest());
		DataWrapper d1 = dataGen.wrapByteBuffer(buffer);
		DataWrapper data = dataGen.chainDataWrappers(d1, req.getData());
		
		// Split the body in half
		List<? extends DataWrapper> split = dataGen.split(data, data.getReadableSize() - 20);

		ByteBuffer buffer1 = ByteBuffer.wrap(split.get(0).createByteArray());
		ByteBuffer buffer2 = ByteBuffer.wrap(split.get(1).createByteArray());
		dataListener.incomingData(channel, buffer1);
		dataListener.incomingData(channel, buffer2);

		ResponseWrapper response = create();
		response.assertStatusCode(KnownStatusCode.HTTP_303_SEEOTHER);

		UserDto user = mockSomeLib.getUser();
		Assert.assertEquals("D&D", user.getFirstName());
		Assert.assertEquals(3, user.getAccounts().size());
		Assert.assertEquals("Account2Name", user.getAccounts().get(1).getName());
		Assert.assertEquals(56, user.getAccounts().get(2).getAddresses().get(0).getNumber());
	}
	
	private HttpStatefulParser parser = HttpParserFactory.createStatefulParser(new BufferCreationPool());
	
	private ResponseWrapper create() {
		List<HttpPayload> payloads = parser.parse(dataReceived);
		HttpResponse response = (HttpResponse) payloads.get(0);
		DataWrapper all = dataGen.emptyWrapper();
		for(int i = 1; i < payloads.size(); i++) {
			HttpData d = (HttpData) payloads.get(i);
			all = dataGen.chainDataWrappers(all, d.getBodyNonNull());
		}
		
		HttpFullResponse fResponse = new HttpFullResponse(response, all);
		return new ResponseWrapper(fResponse);
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			binder.bind(SomeOtherLib.class).toInstance(mockSomeOtherLib);
			binder.bind(SomeLib.class).toInstance(mockSomeLib);
			binder.bind(Executor.class).toInstance(mockExecutor);
		}
	}
}
