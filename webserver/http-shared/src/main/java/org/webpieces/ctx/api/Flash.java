package org.webpieces.ctx.api;

public interface Flash extends FlashScope {
    public boolean hasMessage();

    public String message();

    public void setMessage(String msg);
}
