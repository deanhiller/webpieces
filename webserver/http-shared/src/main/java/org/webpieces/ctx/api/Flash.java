package org.webpieces.ctx.api;

public interface Flash extends FlashScope {
    boolean hasMessage();

    String message();

    void setMessage(String msg);
}
