package org.webpieces.plugins.hibernate.app;

import javax.persistence.EntityManager;

import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.CompanyDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserDbo;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;

public class HibernateController {
	
	//private static final Logger log = LoggerFactory.getLogger(HibernateAsyncController.class);
	
	/**
	 * BIG NOTE: This is NOT the way you should use hibernate but is a base case for us to 
	 * just test out hibernate without filters and added complexity
	 * @return
	 */
	public Redirect save() {
		
		EntityManager mgr = Em.get();
		
		CompanyDbo company = new CompanyDbo();
		company.setName("WebPieces LLC");
		
		UserDbo user = new UserDbo();
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
		UserDbo user = mgr.find(UserDbo.class, id);
		return Actions.renderThis("user", user);
	}
}
