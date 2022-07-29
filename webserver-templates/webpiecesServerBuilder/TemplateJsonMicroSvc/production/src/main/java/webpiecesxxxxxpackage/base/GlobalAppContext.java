package webpiecesxxxxxpackage.base;

import javax.inject.Singleton;

import org.webpieces.ctx.api.ApplicationContext;

/**
 * You MUST restart the server on changes to this class or Server.java 
 *
 */
@Singleton
public class GlobalAppContext implements ApplicationContext, ApplicationCtxManaged {

	private String googleAnalytics;

	@Override
	public String getGoogleAnalyticsCode() {
		return googleAnalytics;
	}

	@Override
	public void setGoogleAnalyticsCode(String s) {
		this.googleAnalytics = s;
	}
	

}
