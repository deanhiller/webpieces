package WEBPIECESxPACKAGE;

import org.webpieces.ctx.api.ApplicationContext;

/**
 * You MUST restart the server on changes to this class or Server.java 
 *
 */
public class GlobalAppContext implements ApplicationContext, ApplicationCtxManaged {

	private String googleAnalytics;

	@Override
	public String getGoogleAnalyticsSnippet() {
		return googleAnalytics;
	}

	@Override
	public void setGoogleAnalyticsSnippet(String s) {
		this.googleAnalytics = s;
	}
	

}
