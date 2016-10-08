package org.webpieces.plugins.hibernate.app;

import javax.inject.Inject;

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
	
	private SessionFactory factory;

	@Inject
	public HibernateController(SessionFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * BIG NOTE: This is NOT the way you should use hibernate but is a base case for us to 
	 * just test out hibernate without filters and added complexity
	 * @return
	 */
	public Redirect save() {
		Session session = factory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		
		CompanyDbo company = new CompanyDbo();
		company.setName("WebPieces LLC");
		
		UserDbo user = new UserDbo();
		user.setEmail("dean@xsoftware.biz");
		user.setName("Soemthing");
		user.setCompany(company);
		
		try {
			session.save(company);
			session.save(user);
		
			session.flush();
			
			tx.commit();
		} catch(Throwable e) {
			rollbackIfNeeded(tx, e);
			closeSession(session, e);
			throw new RuntimeException(e);
		}
		
		return Actions.redirect(HibernateRouteId.DISPLAY_ENTITY, "user", user.getId());
	}
	
	public Render display(Integer id) {
		Session session = factory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		
		try {
			UserDbo user = session.find(UserDbo.class, id);

			tx.commit();

			return Actions.renderThis("user", user);

		} catch(Throwable e) {
			rollbackIfNeeded(tx, e);
			closeSession(session, e);
			throw new RuntimeException(e);
		}
	}

	private void closeSession(Session session, Throwable original) {
		try {
			session.close();
		} catch(Throwable e) {
			//original is what will be thrown but add a secondary suppressed exception
			//into the original...
			original.addSuppressed(e);
		}
	}

	private void rollbackIfNeeded(Transaction tx, Throwable original) {
		try {
			if(tx.isActive())
				tx.rollback();
		} catch(Throwable e) {
			original.addSuppressed(e);
		}
	}
}
