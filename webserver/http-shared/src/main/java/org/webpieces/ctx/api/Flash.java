package org.webpieces.ctx.api;

public interface Flash extends FlashScope {
    public Boolean isSuccess();
    public Boolean isError();

    public String success();
    public String error();

    public void setSuccess(String msg);
    public void setError(String msg);
}
