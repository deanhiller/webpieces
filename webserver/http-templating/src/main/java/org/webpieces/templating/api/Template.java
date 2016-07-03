package org.webpieces.templating.api;

import java.io.Writer;
import java.util.Map;

public interface Template {

	public void run(Map<String, Object> properties, Writer str);
	
}
