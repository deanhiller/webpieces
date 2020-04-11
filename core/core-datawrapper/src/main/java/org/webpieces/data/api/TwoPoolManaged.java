package org.webpieces.data.api;

public interface TwoPoolManaged {

    public String getCategory();

    public void setBaseBufferPoolSize(int size);
    public int getBaseBufferPoolSize();

    public void setSslBufferPoolSize(int size);
    public int getSslBufferPoolSize();


}
