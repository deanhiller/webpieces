package org.webpieces.router.impl.params;

import java.util.HashMap;
import java.util.Map;

public class ParamTreeNode extends ParamNode {

	private Map<String, ParamNode> subKeyToNode = new HashMap<>();

	public ParamNode get(String firstKey) {
		return subKeyToNode.get(firstKey);
	}

	public void put(String firstKey, ParamNode vNode) {
		subKeyToNode.put(firstKey, vNode);
	}
}
