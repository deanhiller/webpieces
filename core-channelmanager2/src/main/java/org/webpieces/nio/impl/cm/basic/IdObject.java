package org.webpieces.nio.impl.cm.basic;

public class IdObject {

	private String id;
	private String channelId;
	private String name;
    
	private static final String LB = "[";
	private static final String RB = "]";
	private static final String S = " ";
	
	public IdObject(String channelId) {
		this.id = LB+channelId+RB+S;
		this.channelId = channelId;
	}

	public IdObject(IdObject serverChannelId, int newSocketNum) {
		channelId = serverChannelId.getChannelId()+newSocketNum;
		this.id = LB+channelId+RB+S;
	}

	public String getChannelId() {
		return channelId;
	}

	public String toString() {
		return id;
	}

	public void setChannelId(String o) {
		this.channelId = o;
	}

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
        this.id = LB+channelId+RB+LB+name+RB+S;
    }
    
    public String getName() {
        return name;
    }
}
