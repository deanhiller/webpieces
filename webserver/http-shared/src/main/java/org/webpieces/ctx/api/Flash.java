package org.webpieces.ctx.api;

public interface Flash extends FlashScope {
    boolean hasMessage();

    String getMessage();

    void setMessage(String msg);

    /**
     * Mainly for ajax popups to keep error message separate from a master template that has 
     *     _flash.message at the top of the page
     * @return
     */
    String getError();

    void setError(String msg);
    
	void setShowEditPopup(boolean b);

	boolean isShowEditPopup();
}
