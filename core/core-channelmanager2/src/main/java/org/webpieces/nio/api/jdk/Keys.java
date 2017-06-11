package org.webpieces.nio.api.jdk;

import java.nio.channels.SelectionKey;
import java.util.Set;

public class Keys {

	private int keyCount;
	private Set<SelectionKey> selectedKeys;

	public Keys(int keyCount, Set<SelectionKey> selectedKeys) {
		this.keyCount = keyCount;
		this.selectedKeys = selectedKeys;
	}

	public int getKeyCount() {
		return keyCount;
	}

	public Set<SelectionKey> getSelectedKeys() {
		return selectedKeys;
	}
	
}
