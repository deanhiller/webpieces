package org.webpieces.router.impl.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.Constants;
import org.webpieces.router.api.actions.Render;

public class RenderImpl implements Render {

	private String relativeOrAbsoluteView;
	private Map<String, Object> pageArgs = new HashMap<>();
	private Set<String> keys = new HashSet<>();

	public RenderImpl(String view, Object ... pageArgTupleList) {
		this.relativeOrAbsoluteView = view;
		pageArgs = PageArgListConverter.createPageArgMap(pageArgTupleList);
		
		//create a Set<String> of named properties that controller passed in so any tags can validate the controller
		//passed it in, specifically the field tag would like to ensure the programmer didn't have a typo
		for(int i = 0; i < pageArgTupleList.length; i++) {
			Object obj = pageArgTupleList[i];
			if(i % 2 == 0) {
				keys.add((String)obj);
			}
		}
		
		//A VERY special key for the FieldTag.java or other tags to use to verify a key existed from
		//the controller
		pageArgs.put(Constants.KEYS, keys);
	}

	public Map<String, Object> getPageArgs() {
		return pageArgs;
	}

	public String getRelativeOrAbsolutePath() {
		return relativeOrAbsoluteView;
	}
}
