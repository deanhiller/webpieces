package org.playorm.util.api.serviceproxy;


public interface ServiceProxy {

	public void setService(Object realService);
	
	public void unsetService(Object defaultService);

	public void start();
	public void stop();
}
