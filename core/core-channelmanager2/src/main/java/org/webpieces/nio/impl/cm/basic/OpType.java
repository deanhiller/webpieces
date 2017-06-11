package org.webpieces.nio.impl.cm.basic;

import java.nio.channels.SelectionKey;

public class OpType {

	public static String opType(int ops) {
		String retVal = "";
		if((ops & SelectionKey.OP_ACCEPT) > 0)
			retVal+="A";
		if((ops & SelectionKey.OP_CONNECT) > 0)
			retVal+="C";
		if((ops & SelectionKey.OP_READ) > 0)
			retVal+="R";
		if((ops & SelectionKey.OP_WRITE) > 0)
			retVal+="W";
		
		return retVal;
	}
}
