package org.webpieces.router.impl.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ParamValueTreeCreator {

	//A Map comes in like this (key on left and value on right)
	//  user.account.company.address = xxxx
	//  user.account.company.name = yyy
	//  user.account.name = zzz
	//  user.account.stuff = { 111, 222, 333 }
	//  user.name = dean
	//  color = blue
	
	//  user.account = 111  #weird but ok, we could do this too, but on binding, what would that mean?
	//  BUT we need N maps, one for user, one for color, etc. etc.
	public void createTree(ParamTreeNode paramTree, Map<String, List<String>> params, FromEnum from) {
		List<String> listSubKeys = null;
		try {
			
			for(Map.Entry<String, List<String>> entry : params.entrySet()) {
				String key = entry.getKey();
				String[] subKeys = key.split("\\.");
				if(subKeys.length == 0) {
					listSubKeys = new ArrayList<>(Arrays.asList(entry.getKey()));
				} else {
					listSubKeys = new ArrayList<>(Arrays.asList(subKeys));
				}
				createTree(paramTree, listSubKeys, entry.getValue(), key, from);
			}
		} catch (RuntimeException e) {
			throw new RuntimeException("Something bad happened with key list="+listSubKeys, e);
		}
	}
	
	private void createTree(ParamTreeNode trees, List<String> asList, List<String> list, String fullKeyName, FromEnum from) {
		if(asList.size() == 0)
			return;
		
		String firstKey = asList.remove(0);
		ParamNode node = trees.get(firstKey);
		if(node != null) {
			if(!(node instanceof ParamTreeNode))
				throw new IllegalStateException("Bug, something went wrong with key="+firstKey);
			else if(asList.size() == 0)
				throw new IllegalArgumentException("Bug, not enough subkeys...conflict in param list like user.account.id=5 "
						+ "and user.account=99 which is not allowed(since user.account would be an object so we can't set it to 99)");
			ParamTreeNode tree = (ParamTreeNode) node;
			createTree(tree, asList, list, fullKeyName, from);
			return;
		} else if(asList.size() == 0) {
			ValueNode vNode = new ValueNode(list, fullKeyName, from);
			trees.put(firstKey, vNode);
			return;
		}

		ParamTreeNode p = new ParamTreeNode();
		trees.put(firstKey, p);
		createTree(p, asList, list, fullKeyName, from);
	}
}
