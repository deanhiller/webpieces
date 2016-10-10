package org.webpieces.plugins.hibernate.app;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.webpieces.plugins.hibernate.app.dbo.CompanyDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserDbo;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class HibernateController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateController.class);
	
	private EntityManagerFactory factory;

	@Inject
	public HibernateController(EntityManagerFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * BIG NOTE: This is NOT the way you should use hibernate but is a base case for us to 
	 * just test out hibernate without filters and added complexity
	 * @return
	 */
	public Redirect save() {
		
		EntityManager mgr = factory.createEntityManager();
		
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();
		
		CompanyDbo company = new CompanyDbo();
		company.setName("WebPieces LLC");
		
		UserDbo user = new UserDbo();
		user.setEmail("dean@xsoftware.biz");
		user.setName("SomeName");
		user.setCompany(company);
		
		try {
			mgr.persist(company);
			mgr.persist(user);

			mgr.flush();
			
			tx.commit();
		} catch(Throwable e) {
			rollbackIfNeeded(tx, e);
			closeSession(mgr, e);
			throw new RuntimeException(e);
		}
		
		return Actions.redirect(HibernateRouteId.DISPLAY_ENTITY, "id", user.getId());
	}
	
	public Render display(Integer id) {
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();
		
		try {
			UserDbo user = mgr.find(UserDbo.class, id);

			tx.commit();

			return Actions.renderThis("user", user);

		} catch(Throwable e) {
			rollbackIfNeeded(tx, e);
			closeSession(mgr, e);
			throw new RuntimeException(e);
		}
	}

	private void closeSession(EntityManager mgr, Throwable original) {
		try {
			mgr.close();
		} catch(Throwable e) {
			//original is what will be thrown but add a secondary suppressed exception
			//into the original...
			original.addSuppressed(e);
		}
	}

	private void rollbackIfNeeded(EntityTransaction tx, Throwable original) {
		try {
			if(tx.isActive())
				tx.rollback();
		} catch(Throwable e) {
			original.addSuppressed(e);
		}
	}
}
