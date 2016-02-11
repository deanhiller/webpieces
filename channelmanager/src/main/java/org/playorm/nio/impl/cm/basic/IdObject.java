package org.playorm.nio.impl.cm.basic;

public class IdObject {

	private String id;
	private String cmId;
	private String channelId;
	private String name;
    
	private static final String LB = "[";
	private static final String RB = "]";
	private static final String S = " ";
	
	public IdObject(String cmId, String channelId) {
		this.id = LB+cmId+RB+LB+channelId+RB+S;
		this.cmId = cmId;
		this.channelId = channelId;
	}

	public IdObject(IdObject serverChannelId, String newSocketId) {
		this.id = LB+serverChannelId.getCmId()+RB+LB+serverChannelId.getChannelId()+RB+LB+newSocketId+RB+S;
		this.cmId = serverChannelId.getCmId();
		this.channelId = newSocketId;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getCmId() {
		return cmId;
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
        this.id = LB+cmId+RB+LB+channelId+RB+LB+name+RB+S;
    }
    
    public String getName() {
        return name;
    }
}
