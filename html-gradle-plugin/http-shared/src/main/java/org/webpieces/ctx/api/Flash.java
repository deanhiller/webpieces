package org.webpieces.ctx.api;

public interface Flash extends FlashScope {
    boolean hasMessage();

    String getMessage();

    /**
     * For messages like "User successfully saved" on any next page OR for errors like
     * "Errors in form below" for non-ajax popup add/edits
     * 
     * @param msg
     */
    void setMessage(String msg);

    /**
     * Mainly for ajax popups to keep error message separate from a master template that has 
     *     _flash.message at the top of the page
     * @return
     */
    String getError();

    /**
     * Mainly for ajax popups to keep error message separate from a master template that has 
     *     _flash.message at the top of the page
     * @return
     */
    void setError(String msg);
    
	void setShowEditPopup(boolean b);

	boolean isShowEditPopup();
	
}
