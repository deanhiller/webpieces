package org.webpieces.ctx.api;

public interface Validation extends FlashScope {

	void addError(String fullKeyName, String string);

	String getError(String fieldName);

	boolean hasErrors();

}
