package org.webpieces.templating.api;

import org.webpieces.templating.impl.source.ScriptCode;

public interface Tag {

	void generateStartAndEnd(ScriptCode sourceCode);

	String getName();

	void generateStart(ScriptCode sourceCode);

	void generateEnd(ScriptCode sourceCode);

}
