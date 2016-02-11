/*
Copyright (c) 2002, Dean Hiller
All rights reserved.

*****************************************************************
IF YOU MAKE CHANGES TO THIS CODE AND DO NOT POST THEM, YOU 
WILL BE IN VIOLATION OF THE LICENSE I HAVE GIVEN YOU.  Contact
me at deanhiller@users.sourceforge.net if you need a different
license.
*****************************************************************

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Also, just to clarify a point in the GNU license, this software 
can only be bundled with your software if your software is free.

*/
package org.playorm.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.CorruptPacketException;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketListener;
import org.playorm.nio.api.libs.PacketProcessor;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.testutil.CloneByteBuffer;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.testcase.MockTestCase;

public class TestUnpacketizer extends MockTestCase {
//--------------------------------------------------------------------
//	FIELDS/MEMBERS
//--------------------------------------------------------------------
	private static final Logger log = Logger.getLogger(TestUnpacketizer.class.getName());
	private static final String PACKET_METHOD = "incomingPacket";
	private static final String HALF1 = "01234";
	private static final String HALF2 = "56789";
	private static final byte[] PACKET_SEPARATOR = new byte[] { Byte.MAX_VALUE, Byte.MAX_VALUE,
												Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE };
	
	private PacketProcessor unpacketizer;
	private BufferHelper helper;
	private MockObject listener;

//--------------------------------------------------------------------
//	CONSTRUCTORS
//--------------------------------------------------------------------
	/**
	 * FILL IN JAVADOC HERE
	 * @param arg0
	 */
	public TestUnpacketizer(String arg0) {
		super(arg0);
	}
//--------------------------------------------------------------------
//	BUSINESS METHODS
//--------------------------------------------------------------------
	protected void setUpImpl() {
		listener = createMock(PacketListener.class);
		
		listener.setDefaultBehavior("incomingPacket", new CloneByteBuffer());
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(FactoryCreator.KEY_PACKET_SEPARATOR, PACKET_SEPARATOR);
		FactoryCreator creator = FactoryCreator.createFactory(null);
		PacketProcessorFactory factory = creator.createPacketProcFactory(p);

		unpacketizer = factory.createPacketProcessor("someId");
		helper = ChannelServiceFactory.bufferHelper(null);		
		unpacketizer.setPacketListener((PacketListener)listener);
	}
	
	protected void tearDownImpl() {
	}
	
	/**
	 *  Test normal behavior by running 2 full normal packets through.
	 *
	 */
	public void testNormalBehavior() throws IOException {
		ByteBuffer b = ByteBuffer.allocate(100);
		for(int i = 0; i < 2; i++) {
			b.clear();

			String fullString = HALF1+HALF2+i;
			helper.putString(b, fullString);
			helper.doneFillingBuffer(b);
			
			ByteBuffer outgoing = unpacketizer.processOutgoing(b);
			
			//contract is a rewound buffer that it can read to begin with.
			unpacketizer.incomingData(outgoing, null);
			CalledMethod method = listener.expect(PACKET_METHOD);
			
			verifyBuffer(method, fullString, 11);
		}
	}

	public void testHalfAPacket() throws IOException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "started");
				
		ByteBuffer b = ByteBuffer.allocate(30);
		String fullString = HALF1+HALF2;
		int size = fullString.getBytes().length;
		b.putInt(size);

		helper.putString(b, HALF1);
		helper.doneFillingBuffer(b);

		unpacketizer.incomingData(b, null);
		listener.expect(MockObject.NONE);		
		
		helper.eraseBuffer(b);

		helper.putString(b, HALF2);
		b.put(PACKET_SEPARATOR);
		helper.doneFillingBuffer(b);

		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "FEED NEXT BUFFER********************");
		
		unpacketizer.incomingData(b, null);
		CalledMethod method = listener.expect(PACKET_METHOD);
			
		verifyBuffer(method, fullString, size);

		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "ended");
	}
	/**
	 * 
	 * Throw 2 and 1/2 packets at the unpacketizer and then
	 * throw last half and first half of next one and then finally
	 * the very last packet.
	 *
	 */
	public void testTwoAndHalfPackets() throws IOException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "started");
			
		ByteBuffer b = ByteBuffer.allocate(200);
		String fullString = HALF1+HALF2;
		int size = fullString.getBytes().length;
		b.putInt(size); //2*10 because chars are 2 bytes.
		helper.putString(b, fullString);
		b.put(PACKET_SEPARATOR);
		b.putInt(size);
		helper.putString(b, fullString);
		b.put(PACKET_SEPARATOR);
		b.putInt(size);
		helper.putString(b, HALF1);
		helper.doneFillingBuffer(b);
		
		String[] methods = new String[2];
		methods[0] = PACKET_METHOD;
		methods[1] = PACKET_METHOD;		
		
		unpacketizer.incomingData(b, null);
		CalledMethod[] calledMethods = listener.expect(methods);			
		
		//need to verify first 2 packets and then send 
		//both half of last and half of next to finish off tests.
		verifyBuffer(calledMethods[0], fullString, size);
		verifyBuffer(calledMethods[1], fullString, size);
		
		helper.eraseBuffer(b);
		helper.putString(b, HALF2);
		b.put(PACKET_SEPARATOR);
		b.putInt(size); //2*10 because chars are 2 bytes.
		helper.putString(b, HALF1);
		helper.doneFillingBuffer(b);
		
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "FEED NEXT BUFFER********************");
			
		unpacketizer.incomingData(b, null);
		CalledMethod method = listener.expect(PACKET_METHOD);
		
		verifyBuffer(method, fullString, size);

		//finish up by feeding last half of last packet
		helper.eraseBuffer(b);
		helper.putString(b, HALF2);
		b.put(PACKET_SEPARATOR);
		helper.doneFillingBuffer(b);
		
		unpacketizer.incomingData(b, null);
		method = listener.expect(PACKET_METHOD);
		
		verifyBuffer(method, fullString, size);
				
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "ended");		
	}
	
	public void testSplitInVeryFirstHeader() throws IOException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "started");		

		String test = "";
		for(int i = 0; i < 260; i++) {
			test += i%10;
		}
		int len = test.getBytes().length;
		
		//259 so b1 and b2 contain parts of the size.
		//this test must then split b1 and b2.
		assertTrue("size should be greater than or equal to 260 for this test", len >= 260);
		byte b1 = (byte) (len & 0x000000FF);
		byte b2 = (byte) ((len & 0x0000FF00) >> 8);
		byte b3 = (byte) ((len & 0x00FF0000) >> 16);
		byte b4 = (byte) ((len & 0xFF000000) >> 32);
		
		ByteBuffer b = ByteBuffer.allocate(300);
		b.put(b4);
		b.put(b3);
		b.put(b2);

		//leave b1 off and do it later for the split
		//b.put(b1);
		helper.doneFillingBuffer(b);

		unpacketizer.incomingData(b, null);
		//Object evt = 
		listener.expect(MockObject.NONE);
		
		doLastPartOfSplitHeaderVerification(b, b1, test);

		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "ended");		
	}

	public void testSplitInSecondHeader() throws IOException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "started");		

		String test = "";
		for(int i = 0; i < 260; i++) {
			test += i%10;
		}
		int len = test.getBytes().length;
		byte b1 = (byte) (len & 0x000000FF);
		byte b2 = (byte) ((len & 0x0000FF00) >> 8);
		byte b3 = (byte) ((len & 0x00FF0000) >> 16);
		byte b4 = (byte) ((len & 0xFF000000) >> 32);
		
		ByteBuffer b = ByteBuffer.allocate(400);

		String fullString = HALF1+HALF2;
		int size = fullString.getBytes().length;
		b.putInt(size); 
		helper.putString(b, fullString);
		b.put(PACKET_SEPARATOR);
		b.put(b4);
		b.put(b3);
		b.put(b2);
		//leave b1 off and do it later for the split
		//b.put(b1);
		helper.doneFillingBuffer(b);

		//contract is a donePut()(flip() is donePut() buffer that it can read to begin with.
		unpacketizer.incomingData(b, null);
		CalledMethod method = listener.expect(PACKET_METHOD);
		
		verifyBuffer(method, fullString, size);

		doLastPartOfSplitHeaderVerification(b, b1, test); //numChars);

	}
	
	public void testExceptions() throws IOException {
		setNumberOfExpectedWarnings(2);
		
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "started");
			
		ByteBuffer b = ByteBuffer.allocate(200);

		
		b.putInt(-6); //2*10 because chars are 2 bytes.
		helper.putString(b, HALF1+HALF2);
		b.putInt(20);
		helper.doneFillingBuffer(b);
		
		try {
			//feed packet with negative size.....
			unpacketizer.incomingData(b, null);
			fail("Should have thrown a RuntimeException");
		} catch(CorruptPacketException e) {
			assertTrue("header should be corrupt", e.isHeaderCorrupt());
		}
		
		listener.expect(MockObject.NONE);
		
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "FEEDING NEXT BAD PACKET************");
		helper.eraseBuffer(b);
		b.putInt(100000000); //2*10 because chars are 2 bytes.
		helper.putString(b, HALF1+HALF2);
		b.putInt(20);
		helper.doneFillingBuffer(b);
		
		try {
			//feed incoming packet with two large a header(ie. size is less than max size of packet)
			unpacketizer.incomingData(b, null);
			fail("Should have thrown an Exception");
		} catch(CorruptPacketException e) {
			assertTrue("header should be corrupt", e.isHeaderCorrupt());			
		}		
		listener.expect(MockObject.NONE);		

		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "FEEDING NULL************");
		try {
			unpacketizer.incomingData(null, null);
			fail("Should have thrown a RuntimeException");
		} catch(IllegalArgumentException e) {
			assertEquals("Message was incorrect", "someId evt cannot be null", e.getMessage());
		}		
		listener.expect(MockObject.NONE);	

		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "ended");
	}
	
//	public void testNoTrailerFailure() {
//		String fullString = half1+half2;
//		int size = fullString.getBytes().length;
//		ByteBuffer b = ByteBuffer.allocate(size+30);
//		b.putInt(size);
//		helper.putString(b, fullString);
//		//look, no packet separator mom!!!!
//		//put the string again just for some junk data
//		helper.putString(b, "a");
//		b.put(packetSeparator); //now put the packet separator
//		b.putInt(size);
//		helper.putString(b, fullString);
//		helper.doneFillingBuffer(b);
//
//		//contract is a rewound buffer that it can read to begin with.
//		try {
//			unpacketizer.notify(b);
//			fail("Should have thrown an exception");
//		} catch(CorruptPacketException e) {
//			assertTrue("trailer should have failed", e.isTrailerCorrupt());
//		}
//		
//		//should still recover and get the good packet behind the bad one....
//		CalledMethod method = listener.expect(PACKET_METHOD);
//		
//		verifyBuffer(method, fullString, size);		
//	}
//	
//	public void testFailureWithPacketSeparatorInPacket() {
//		
//	}
//	
//	//trailer failure should be thrown back to client rather than throwing listener's
//	//failure as the first failure is what is needed usually to debug the problem and
//	//maybe causing the second failure!!!
//	public void testListenerFailureAfterTrailerFailure() {
//		
//	}
	
	private void verifyBuffer(CalledMethod method, String expectedValue, int expectedSize) {
		Object evt = method.getParameter(0);
		assertTrue("Class of evt should be ByteBuffer", evt instanceof ByteBuffer);
			
		ByteBuffer b = (ByteBuffer)evt;
		
		assertEquals("remaining incorrect", expectedSize, b.remaining());
		
		if (log.isLoggable(Level.FINEST)) {
			log.finest("10byteBuf pos="+b.position()+"  lim="+b.limit()+"  remain="+b.remaining());
		}		
		String s = helper.readString(b, expectedSize);

		log.info("s1="+s);
		log.info("s2="+expectedValue);
		assertEquals("Should have retrieved the string that we put", expectedValue, s);
		assertEquals("buffer should be all read", 0, b.remaining());
	}

	private void doLastPartOfSplitHeaderVerification(ByteBuffer b, byte b1, String test) throws IOException {
		helper.eraseBuffer(b);
		b.put(b1);
		helper.putString(b, test);
		b.put(PACKET_SEPARATOR);
		helper.doneFillingBuffer(b);

		unpacketizer.incomingData(b, null);
		CalledMethod method = listener.expect(PACKET_METHOD);		
		assertTrue("evt should have been an instance of ByteBuffer", method.getAllParams()[0] instanceof ByteBuffer);
			
		b = (ByteBuffer)method.getAllParams()[0];
		String s = helper.readString(b, b.remaining());
		
		assertEquals("remaining once read should be 0", 0, b.remaining());

		assertEquals("strings should be equal", test, s);
	}	

}