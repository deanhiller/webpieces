package org.webpieces.nio.impl.ssl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.BufferPool;

public class ClientHelloParser {
	private static final short HANDSHAKE_CONTENT_TYPE = 22;
	private static final short CLIENTHELLO_MESSAGE_TYPE = 1;
	private static final short SSLV2_CLIENTHELLO = 128;
	
	private static final int SERVER_NAME_EXTENSION_TYPE = 0;
	private static final short HOST_NAME_TYPE = 0;
	
	private ByteBuffer cachedBuffer;
	private BufferPool pool;
	
	public ClientHelloParser(BufferPool pool) {
		this.pool = pool;
	}
	
	/**
	 * Returns null if we still need more data
	 * 
	 * @param b
	 * @return
	 */
	ParseResult fetchServerNamesIfEntirePacketAvailable(ByteBuffer b) {
		if(cachedBuffer != null) {
			//prefix cachedBuffer in front of b and assign to b as the packet that is coming in
			ByteBuffer newBuf = pool.nextBuffer(cachedBuffer.remaining()+b.remaining());
			newBuf.put(cachedBuffer);
			newBuf.put(b);
			newBuf.flip();
			pool.releaseBuffer(b); //release b that is now in the newBuf
			pool.releaseBuffer(cachedBuffer); //release cached buffer that is now in newBuf
			b = newBuf;
		}
		
		if(b.remaining() < 5) {
			cachedBuffer = b;
			return null; //wait for more data
		}
		
		int recordSize = 0;
		ByteBuffer duplicate = b.duplicate();
		short contentType = getUnsignedByte(duplicate);
		if(contentType == HANDSHAKE_CONTENT_TYPE) {
			getUnsignedByte(duplicate);
			getUnsignedByte(duplicate);
			recordSize = getUnsignedShort(duplicate);
			
			  // Now wait until we have the entire record
		    if (b.remaining() < (5 + recordSize)) {
		        // Keep buffering
		        return null;
		    }					
		} else if (contentType == SSLV2_CLIENTHELLO) {
		    short len = getUnsignedByte(duplicate);

		    // Decode the length
		    recordSize = ((contentType & 0x7f) << 8 | len);

		    // Now wait until we have the entire record
		    if (b.remaining() < (2 + recordSize)) {
		        // Keep buffering
		        return null;
		    }

		} else {
			throw new IllegalStateException("contentType="+contentType+" not supported in ssl hello handshake packet");
		}

	    short messageType = getUnsignedByte(duplicate);
	    if (messageType != CLIENTHELLO_MESSAGE_TYPE) {
	    	throw new IllegalStateException("something came before ClientHello :( messageType="+messageType);
	    }
	    
	    if (contentType == HANDSHAKE_CONTENT_TYPE) {
	        // If we're not an SSLv2 ClientHello, then skip the ClientHello
	        // message size.
	    	duplicate.get(new byte[3]);
	    	
	        // Use the ClientHello ProtocolVersion
	    	getUnsignedShort(duplicate);

	        // Skip ClientRandom
	        duplicate.get(new byte[32]);

	        // Skip SessionID
	        int sessionIDSize = getUnsignedByte(duplicate);
	        duplicate.get(new byte[sessionIDSize]);

	        //read in and discard cipherSuite...
	        int cipherSuiteSize = getUnsignedShort(duplicate);
	        duplicate.get(new byte[cipherSuiteSize]);

	        //read in compression methods size and discard..
	        short compressionMethodsLen = getUnsignedByte(duplicate);
	        duplicate.get(new byte[compressionMethodsLen]);
	        
	        int extensionLen = getUnsignedShort(duplicate);
	        List<String> names = readInExtensionServerNames(duplicate, extensionLen);
	        
	        return new ParseResult(b, names);
	    } else {
	        // SSLv2 ClientHello.
	        // Use the ClientHello ProtocolVersion
	        //SslVersion version = SslVersion.decode(getUnsignedByte(duplicate));

	    	throw new UnsupportedOperationException("not supported yet");
	    }
	}

	private List<String> readInExtensionServerNames(ByteBuffer duplicate, int len) {
		List<String> serverNames = new ArrayList<>();
		int byteCount = 0;
		while(byteCount < len) {
			byteCount += 4;  //reading in 4 bytes so add them in
			if(duplicate.remaining() < 4)
				throw new IllegalStateException("Corrupt packet with incorrect format");
			int type = getUnsignedShort(duplicate);
			int extLen = getUnsignedShort(duplicate);
			
			if(duplicate.remaining() < extLen)
				throw new IllegalStateException("Corrupt packet with incorrect format as len didn't match");
			
			if(type == SERVER_NAME_EXTENSION_TYPE) {
				String name = readServerNames(duplicate, extLen);
				serverNames.add(name);
			} else
				duplicate.get(new byte[extLen]);
			byteCount += extLen;
		}
		
		return serverNames;
	}

	private String readServerNames(ByteBuffer duplicate, int extLen) {
		int byteCount = 0;
		
		byteCount += 2; //for listLen 2 bytes
		int listLen = getUnsignedShort(duplicate);
		if(listLen + 2 != extLen)
			throw new RuntimeException("we have something we need to fix here as listLen is only two less bytes then extensionLength");
		
		byteCount += 1; //for serverNameType
		short serverNameType = getUnsignedByte(duplicate);
		if(serverNameType != HOST_NAME_TYPE)
			throw new IllegalStateException("Server name type="+serverNameType+" not supported yet");
		
		byteCount += 2; //for serverNameLen
		int serverNameLen = getUnsignedShort(duplicate);
		
		byteCount += serverNameLen;
		if(byteCount != extLen)
			throw new UnsupportedOperationException("bytes read in servernames extension does not match extLen(we need to loop here then)");
		
		byte[] data = new byte[serverNameLen];
		duplicate.get(data);
		
		String serverName = new String(data);
		return serverName;
	}

	public short getUnsignedByte(ByteBuffer bb) {
		return ((short)(bb.get() & 0xff));
	}
	
	public int getUnsignedShort (ByteBuffer bb)
	{
		return (bb.getShort() & 0xffff);
	}
}
