package org.webpieces.plugins.hibernate.app;

import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.CompanyTestDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@Singleton
public class HibernateController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAsyncController.class);
	
	/**
	 * BIG NOTE: This is NOT the way you should use hibernate but is a base case for us to 
	 * just test out hibernate without filters and added complexity
	 * @return
	 */
	public Redirect save() {
		
		EntityManager mgr = Em.get();
		
		CompanyTestDbo company = new CompanyTestDbo();
		company.setName("WebPieces LLC");
		
		UserTestDbo user = new UserTestDbo();
		user.setEmail("dean@sync.xsoftware.biz");
		user.setName("SomeName");
		user.setCompany(company);
		
		mgr.persist(company);
		mgr.persist(user);

		mgr.flush();
			
		return Actions.redirect(HibernateRouteId.DISPLAY_ENTITY, "id", user.getId());
	}
	
	public Render display(Integer id) {
		EntityManager mgr = Em.get();
		UserTestDbo user = mgr.find(UserTestDbo.class, id);
		return Actions.renderThis("user", user);
	}
	
	public Render entityLoad(Integer id) {
		EntityManager mgr = Em.get();
		UserTestDbo user = mgr.find(UserTestDbo.class, id);	
		log.info("loaded user");
		return Actions.renderThis("user", user);
	}
}
