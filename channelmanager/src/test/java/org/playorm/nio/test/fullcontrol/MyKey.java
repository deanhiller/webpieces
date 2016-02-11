/**
 * 
 */
package org.playorm.nio.test.fullcontrol;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

class MyKey extends AbstractSelectionKey {
	private MockObject mockKey;
	private Key key;
	private SelectableChannel channel;
    
	public MyKey(SelectableChannel channel) {
		mockKey = MockObjectFactory.createMock(Key.class);
		key = (Key)mockKey;
		this.channel = channel;
	}
	
	public MockObject getMock() {
		return mockKey;
	}
	   
	@Override
	public SelectableChannel channel() {
		return channel;
	}

	@Override
	public Selector selector() {
		return key.selector();
	}

	@Override
	public int interestOps() {
		return key.interestOps();
	}

	@Override
	public SelectionKey interestOps(int ops) {
        key.interestOps(ops);
        return this;
	}

	@Override
	public int readyOps() {
		return key.readyOps();
	}
    
    
	
}