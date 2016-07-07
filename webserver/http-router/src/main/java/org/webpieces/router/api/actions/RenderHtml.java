package org.webpieces.router.api.actions;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.dto.View;

public class RenderHtml implements Action {

	private View view;
	private Map<String, Object> pageArgs = new HashMap<>();

//	protected RenderHtml(String view, Object ... pageArgs) {
//		this.view = view;
//		this.pageArgs = pageArgs;
//	}
	
	protected RenderHtml(Object ... pageArgs) {
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
	}

	
	public View getView() {
		return view;
	}

	public Map<String, Object> getPageArgs() {
		return pageArgs;
	}

}
