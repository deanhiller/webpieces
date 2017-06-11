package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import org.webpieces.nio.api.channels.RegisterableChannel;



/**
 * @author Dean Hiller
 */
abstract class RegisterableChannelImpl implements RegisterableChannel {

	protected IdObject id;
	protected SelectorManager2 selMgr;
	private SelectionKey key;

	public RegisterableChannelImpl(IdObject id, SelectorManager2 selMgr) {
		if(id == null)
			throw new IllegalArgumentException("id cannot be null");		
		this.id = id;
		this.selMgr = selMgr;
	}
	
    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#setName(java.lang.String)
     */
    public void setName(String name)
    {
        id.setName(name);
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#getName()
     */
    public String getName()
    {
        return id.getName();
    }
    
	@Override
	public String getChannelId() {
		return id.getChannelId();
	}
	
	public IdObject getIdObject() {
		return id;
	}
	
	public String toString() {
		return id.toString();
	}
	
	//public abstract SelectableChannel getRealChannel();

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

//    /**
//     */
//    public SelectionKey keyFor(Select select) {
//        return select.getKeyFromChannel(getRealChannel());
//    }
//
//    /**
//     * @param allOps
//     * @param struct
//     */
//    public SelectionKey register(Select select, int allOps, WrapperAndListener struct) {
//        SelectableChannel s = getRealChannel();
//        return select.register(s, allOps, struct);
//    }

	protected abstract SelectionKey keyFor();

	protected abstract SelectionKey register(int allOps, WrapperAndListener struct);

	protected abstract void resetRegisteredOperations(int ops);

	public SelectorManager2 getSelectorManager() {
		return selMgr;
	}
	
}
