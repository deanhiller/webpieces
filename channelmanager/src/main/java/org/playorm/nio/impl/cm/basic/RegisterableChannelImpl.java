package org.playorm.nio.impl.cm.basic;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.testutil.nioapi.Select;



/**
 * @author Dean Hiller
 */
abstract class RegisterableChannelImpl implements RegisterableChannel {

	private IdObject id;
	private SelectorManager2 selMgr;
	private SelectionKey key;

	public RegisterableChannelImpl(IdObject id, SelectorManager2 selMgr) {
		if(id == null)
			throw new IllegalArgumentException("id cannot be null");		
		this.id = id;
		this.selMgr = selMgr;
	}
	
    /**
     * @see org.playorm.nio.api.channels.RegisterableChannel#setName(java.lang.String)
     */
    public void setName(String name)
    {
        id.setName(name);
    }

    /**
     * @see org.playorm.nio.api.channels.RegisterableChannel#getName()
     */
    public String getName()
    {
        return id.getName();
    }
    
	public IdObject getIdObject() {
		return id;
	}
	
	public String toString() {
		return id.toString();
	}
	
	public abstract SelectableChannel getRealChannel();

	/**
	 */
	public SelectorManager2 getSelectorManager() {
		return selMgr;
	}
	/**
	 */
	public void setKey(SelectionKey k) {
		this.key = k;
	}
	protected SelectionKey getKey() {
		return key;
	}

    /**
     * This method exists because you can close a channel but the TCP FIN(finish) will
     * not be sent until the selector wakes up.  This was tested on jdk1.5.0_01, 03, 05
     * AND 06 version of the jdk!!!!!  The other method is to put this on the selector thread
     * and wake up the selector
     * @throws IOException
     */
	public void wakeupSelector() throws IOException {
		selMgr.wakeUpSelector();
	}

    /**
     */
    public SelectionKey keyFor(Select select) {
        return select.getKeyFromChannel(getRealChannel());
    }

    /**
     * @param allOps
     * @param struct
     * @throws ClosedChannelException 
     */
    public SelectionKey register(Select select, int allOps, WrapperAndListener struct) throws ClosedChannelException {
        SelectableChannel s = getRealChannel();
        return select.register(s, allOps, struct);
    }
}
