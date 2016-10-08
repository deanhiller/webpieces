package org.webpieces.router.impl.actions;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.actions.Render;

public class RenderImpl implements Render {

	private String relativeOrAbsoluteView;
	private Map<String, Object> pageArgs = new HashMap<>();

	public RenderImpl(String view, Object ... pageArgTupleList) {
		this.relativeOrAbsoluteView = view;
		pageArgs = PageArgListConverter.createPageArgMap(pageArgTupleList);
	}

	public Map<String, Object> getPageArgs() {
		return pageArgs;
	}

	public String getRelativeOrAbsolutePath() {
		return relativeOrAbsoluteView;
	}
}
