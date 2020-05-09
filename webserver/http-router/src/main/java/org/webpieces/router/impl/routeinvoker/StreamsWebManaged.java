package org.webpieces.router.impl.routeinvoker;

public interface StreamsWebManaged {
    public String getCategory();

    public int getMaxBodySizeToSend();

    public void setMaxBodySizeSend(int maxSize);
}
