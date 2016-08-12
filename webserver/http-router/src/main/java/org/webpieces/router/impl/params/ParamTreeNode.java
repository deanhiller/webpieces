package org.webpieces.router.impl.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ParamTreeNode extends ParamNode {

	private Map<String, ParamNode> subKeyToNode = new HashMap<>();

	public ParamNode get(String firstKey) {
		return subKeyToNode.get(firstKey);
	}

	public void put(String firstKey, ParamNode vNode) {
		subKeyToNode.put(firstKey, vNode);
	}
	
	public Set<Entry<String, ParamNode>> entrySet() {
		return subKeyToNode.entrySet();
	}

	@Override
	public String toString() {
		return "{"+toString(this, 1)+"\n}";
	}
	
	public String toString(ParamTreeNode treeNode, int numberOfTabs) {
		Map<String, ParamNode> tree = treeNode.subKeyToNode;
		String result = "";
		for(Map.Entry<String, ParamNode> entry : tree.entrySet()) {
			result += "\n"+generateTabs(numberOfTabs)+entry.getKey() +"=";
			ParamNode value = entry.getValue();
			if(value instanceof ParamTreeNode) {
				result += "\n"+generateTabs(numberOfTabs)+"{";
				result += toString((ParamTreeNode) value, numberOfTabs+1);
				result += "\n"+generateTabs(numberOfTabs)+"}";
			} else {
				result += entry.getValue();
			}
		}
		
		return result;
	}

	private String generateTabs(int numberOfTabs) {
		String s = "";
		for(int i = 0; i < numberOfTabs; i++) {
			s += "\t";
		}
		return s;
	}
	
}
