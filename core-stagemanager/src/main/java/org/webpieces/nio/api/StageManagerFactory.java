package org.webpieces.nio.api;

/**
 * @author Dean Hiller
 */
public abstract class StageManagerFactory {

	/**
	 * All Keys(and some values) to put in the map variable can be found 
	 * as the constants in ChannelManaagerFactory
	 * @param map
	 */
	public static StageManagerFactory createFactory() {
		return null;
	}
	
	public abstract StageManager createStageManager(ChannelManager mgr);

	
	
}
