package org.webpieces.router.impl.actions;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.actions.Render;

public class RenderImpl implements Render {

	private String relativeOrAbsoluteView;
	private Map<String, Object> pageArgs = new HashMap<>();

	private void addCurrentObjectsToPageArgs() {
		if(Current.isContextSet()) {
			this.pageArgs.put("_flash", Current.flash());
			this.pageArgs.put("_session", Current.session());
			this.pageArgs.put("_messages", Current.messages());
			this.pageArgs.put("_validation", Current.validation());
		}
	}

	public RenderImpl(String view, Object ... pageArgs) {
		this.relativeOrAbsoluteView = view;
		if(pageArgs.length % 2 != 0)
			throw new IllegalArgumentException("All arguments to render must be even with String, Object, String, Object (ie. key, value, key, value)");
		
		String key = null;
		for(int i = 0; i < pageArgs.length; i++) {
			Object obj = pageArgs[i];
			if(i % 2 == 0) {
				if(obj == null) 
					throw new IllegalArgumentException("Argument at position="+i+" cannot be null since it is a key and must be of type String");
				else if(!(obj instanceof String))
					throw new IllegalArgumentException("Argument at position="+i+" must be a String and wasn't.  obj.toString=="+obj);
				key = (String)obj;
			} else {
				this.pageArgs.put(key, obj);
			}
		}
		addCurrentObjectsToPageArgs();
	}
	
	public Map<String, Object> getPageArgs() {
		return pageArgs;
	}

	public String getRelativeOrAbsolutePath() {
		return relativeOrAbsoluteView;
	}
}
