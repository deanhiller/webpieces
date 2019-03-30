package org.webpieces.plugins.hibernate.app;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@Singleton
public class HibernateController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAsyncController.class);

	@Inject
	private ServiceToFail svc;
	
	/**
	 * BIG NOTE: This is NOT the way you should use hibernate but is a base case for us to 
	 * just test out hibernate without filters and added complexity
	 * @return
	 */
	public Redirect saveThenFail() {
		
		EntityManager mgr = Em.get();
		
		UserTestDbo user = new UserTestDbo();
		user.setEmail("dean2222@sync.xsoftware.biz");
		user.setName("SomeName");
		
		mgr.persist(user);

		mgr.flush();
			
		svc.fail(user.getId());
		
		return Actions.redirect(HibernateRouteId.DISPLAY_ENTITY, "id", user.getId());
	}
	
	/**
	 * BIG NOTE: This is NOT the way you should use hibernate but is a base case for us to 
	 * just test out hibernate without filters and added complexity
	 * @return
	 */
	public Redirect save() {
		
		EntityManager mgr = Em.get();
		
		UserTestDbo user = new UserTestDbo();
		user.setEmail("dean@sync.xsoftware.biz");
		user.setName("SomeName");
		
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
	
	public Redirect postMergeUserTest(UserTestDbo user) {
		Current.flash().setMessage("User successfully saved");
		Em.get().merge(user);
        Em.get().flush();
        
		return Actions.redirect(HibernateRouteId.LIST_USERS);
	}
}
