package org.webpieces.ctx.api;

public interface Flash extends FlashScope {
    boolean hasMessage();

    String getMessage();

    void setMessage(String msg);

	void setShowEditPopup(boolean b);

	boolean isShowEditPopup();
}
