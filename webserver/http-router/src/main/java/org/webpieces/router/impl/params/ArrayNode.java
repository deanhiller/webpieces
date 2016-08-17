package org.webpieces.router.impl.params;

import java.util.ArrayList;
import java.util.List;

public class ArrayNode extends ParamNode {

	private List<ParamNode> nodes = new ArrayList<>();
	
	public ParamTreeNode setOrGetTree(int arrayIndex) {
		expandArrayIfNeeded(arrayIndex);

		ParamNode paramNode = nodes.get(arrayIndex);
		if(paramNode != null)
			return (ParamTreeNode) paramNode;
		
		ParamTreeNode treeNode = new ParamTreeNode();
		nodes.set(arrayIndex, treeNode);
		return treeNode;
	}

	private void expandArrayIfNeeded(int arrayIndex) {
		if(arrayIndex >= nodes.size()) {
			for(int i = nodes.size(); i < arrayIndex+1; i++)
				nodes.add(null);
		}
	}

	public void setElement(int arrayIndex, ValueNode valueNode) {
		expandArrayIfNeeded(arrayIndex);
		nodes.set(arrayIndex, valueNode);
	}

	public List<ParamNode> getList() {
		return nodes;
	}

}
