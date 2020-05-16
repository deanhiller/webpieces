package org.webpieces.router.impl.routeinvoker;

import javax.inject.Singleton;

import org.webpieces.data.api.TwoPools;

@Singleton
public class WebSettings implements StreamsWebManaged {
    //The max size of body for dynamic pages for Full responses and chunked responses.  This
    //is used to determine send chunks instead of full response as well since it won't fit
    //in full response sometimes
    private int maxBodySizeToSend = TwoPools.DEFAULT_MAX_BASE_BUFFER_SIZE;

    @Override
    public String getCategory() {
        return "Webpieces Webserver";
    }

    @Override
    public int getMaxBodySizeToSend() {
        return maxBodySizeToSend;
    }

    @Override
    public void setMaxBodySizeSend(int maxBodySize) {
        this.maxBodySizeToSend = maxBodySize;
    }
}
