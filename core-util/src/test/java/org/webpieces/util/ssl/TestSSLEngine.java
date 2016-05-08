package org.webpieces.util.ssl;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import junit.framework.TestCase;

public class TestSSLEngine extends TestCase {

	//private static final Logger log = Logger.getLogger(TestSSLEngine.class.getName());
	
	/**
	 * Sunny day scenaior of normal two normal SSLEngines...no split packets,
	 * no failures, etc.
	 * @throws Exception
	 */
	public void testRawSSLEngine() throws Exception {
		SSLEngine server = getServerEngine();
		SSLEngine client = getClientEngine();
		
		SSLSession s = client.getSession();
		ByteBuffer unencrPacket = ByteBuffer.allocate(s.getApplicationBufferSize());
		ByteBuffer encPacket = ByteBuffer.allocate(s.getPacketBufferSize());
		
		encPacket.clear();
		unencrPacket.clear();
		doHandshakeAndVerify(client, server, unencrPacket, encPacket);
		
		doRehandshake();
		
		//connection is now established....
		encPacket.clear();
		client.closeOutbound();
		SSLEngineResult result = client.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
		assertEquals(Status.CLOSED, result.getStatus());		
		
		encPacket.flip();
		result = server.unwrap(encPacket, unencrPacket);
		assertEquals(HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
		assertEquals(Status.CLOSED, result.getStatus());
		
		encPacket.clear();
		result = server.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());
		assertEquals(Status.CLOSED, result.getStatus());		

		encPacket.flip();
		result = client.unwrap(encPacket, unencrPacket);
		assertEquals(HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());
		assertEquals(Status.CLOSED, result.getStatus());		
		

	}	

	private void doRehandshake() {
//		/*****************************************************
//		 * REHANDSHAKE BEGINS HERE.............
//		 *****************************************************/
//		
//		client.beginHandshake();
//		assertEquals(HandshakeStatus.NEED_WRAP, client.getHandshakeStatus());		
//		
//		encPacket.clear();
//		SSLEngineResult result = client.wrap(unencrPacket, encPacket); //CLIENT HANDSHAKE MSG
//		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
//		assertEquals(Status.OK, result.getStatus());
//
//		String expected = "abc";
//		ByteBuffer encData = ByteBuffer.allocate(s.getPacketBufferSize());
//		ByteBuffer data = ByteBuffer.allocate(10);
//		putString(data, expected);
//		data.flip();
//		log.fine("data1="+data+" encData="+encData);		
//		result = client.wrap(data, encData);           //CLIENT WRAP DATA
//		log.fine("data2="+data+" encData="+encData);
//		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
//		assertEquals(Status.OK, result.getStatus());
//		
//		unencrPacket.clear();
//		encPacket.flip();
//		result = server.unwrap(encPacket, unencrPacket); //SERVER UNWRAP HANDSHAKE MSG
//		assertEquals(HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
//		assertEquals(Status.OK, result.getStatus());	
//		server.getDelegatedTask(); //get task but don't run it yet...wait until after decrypt of real data
//		
//		/********************************************************
//		 * Found out this is expected behavior....until runnable is run SSLEngine can't be used.
//		 * BIG NOTE: Notice, I did not run the Runnable yet.  If I put 
//		 * r.run() right here, and change the assert statements below from NEED_TASK
//		 * to NEED_WRAP, the test will then pass!!!!!
//		 * 
//		 *******************************************************/
//		
//		ByteBuffer dataOut = ByteBuffer.allocate(server.getSession().getApplicationBufferSize());
//		dataOut.clear();
//		encData.flip();
//		log.fine("datain1="+encData+" out="+dataOut);
//		result = server.unwrap(encData, dataOut);        //SERVER UNWRAP DATA
//		log.fine("datain2="+encData+" out="+dataOut);
//		assertEquals(HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
//		assertEquals(Status.OK, result.getStatus());
//		
//		dataOut.flip();
//		String actual = readString(dataOut, dataOut.remaining());
//		assertEquals(expected, actual);		
	}
	
	private void doHandshakeAndVerify(SSLEngine client, SSLEngine server, ByteBuffer unencrPacket, ByteBuffer encPacket) throws Exception {
		startOfHandshake(client, server, unencrPacket, encPacket);
		
		continueHandshake(client, server, unencrPacket, encPacket);		
	}

	private void startOfHandshake(SSLEngine client, SSLEngine server, ByteBuffer unencrPacket, ByteBuffer encPacket) throws SSLException {
		client.beginHandshake();
			
		SSLEngineResult result = client.wrap(unencrPacket, encPacket);
		assertEquals(result.getHandshakeStatus(), HandshakeStatus.NEED_UNWRAP);
		assertEquals(result.getStatus(), Status.OK);
		
		encPacket.flip();
		
		result = server.unwrap(encPacket, unencrPacket);
		
		assertEquals(HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		Runnable r = server.getDelegatedTask();
		r.run();
		
		assertEquals(HandshakeStatus.NEED_WRAP, server.getHandshakeStatus());
		encPacket.clear();
		result = server.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());		
		
		encPacket.flip();
		result = client.unwrap(encPacket, unencrPacket);		
		assertEquals(HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		r = client.getDelegatedTask();
		r.run();
		assertEquals(HandshakeStatus.NEED_WRAP, client.getHandshakeStatus());
		
		encPacket.clear();
		result = client.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());

		encPacket.flip();
		result = server.unwrap(encPacket, unencrPacket);
		assertEquals(HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());		
		r = server.getDelegatedTask();
		r.run();
		assertEquals(HandshakeStatus.NEED_UNWRAP, server.getHandshakeStatus());
	}

	private void continueHandshake(SSLEngine client, SSLEngine server, ByteBuffer unencrPacket, ByteBuffer encPacket) throws SSLException {
		SSLEngineResult result;
		encPacket.clear();
		result = client.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		
		encPacket.flip();
		result = server.unwrap(encPacket, unencrPacket);
		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		
		encPacket.clear();
		result = client.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		
		encPacket.flip();
		result = server.unwrap(encPacket, unencrPacket);
		assertEquals(HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		
		encPacket.clear();
		result = server.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());		
		
		encPacket.flip();
		result = client.unwrap(encPacket, unencrPacket);		
		assertEquals(HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
		
		encPacket.clear();
		result = server.wrap(unencrPacket, encPacket);
		assertEquals(HandshakeStatus.FINISHED, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());	
		
		encPacket.flip();
		result = client.unwrap(encPacket, unencrPacket);		
		assertEquals(HandshakeStatus.FINISHED, result.getHandshakeStatus());
		assertEquals(Status.OK, result.getStatus());
	}
	
	private	String password = "root01";
	private String clientKeystore = "src/test/resources/client.keystore";
	private String serverKeystore = "src/test/resources/server.keystore";	
	private SSLEngine getServerEngine() throws Exception {
		char[] passphrase = password.toCharArray();
		// First initialize the key and trust material.
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(serverKeystore), passphrase);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		
		//****************Server side specific*********************
		// KeyManager's decide which key material to use.
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, passphrase);
		sslContext.init(kmf.getKeyManagers(), null, null);		
		//****************Server side specific*********************
		
		SSLEngine engine = sslContext.createSSLEngine();
		engine.setUseClientMode(false);
		SSLEngine server = engine;		
		return server;
	}
	
	private SSLEngine getClientEngine() throws Exception {
		// Create/initialize the SSLContext with key material
		char[] passphrase = password.toCharArray();
		// First initialize the key and trust material.
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(clientKeystore), passphrase);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		
		//****************Client side specific*********************
		// TrustManager's decide whether to allow connections.
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);
		sslContext.init(null, tmf.getTrustManagers(), null);		
		//****************Client side specific*********************
		
		SSLEngine engine = sslContext.createSSLEngine();
		engine.setUseClientMode(true);
		SSLEngine client = engine;	
		return client;
	}
	
//	private void putString(ByteBuffer b, String fullString) {
//		if(b == null)
//			throw new IllegalArgumentException("Cannot pass in a null buffer");
//		else if(fullString == null)
//			throw new IllegalArgumentException("Cannot pass in a null string");
//		byte[] encodedString;
//		try {
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			OutputStreamWriter writer = new OutputStreamWriter(out);
//			writer.write(fullString);
//			writer.flush();
//			encodedString = out.toByteArray();
//		} catch(IOException e) {
//			throw new RuntimeException("Should never happen", e);
//		}
//
//		b.put(encodedString);	
//	}
	
	public String readString(ByteBuffer b, int numBytesToRead) {
		byte[] buffer = new byte[numBytesToRead];
		b.get(buffer);
		String s = new String(buffer);
		return s;
	}	
}
