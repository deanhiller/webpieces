package org.webpieces.plugins.hibernate;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@Singleton
public class TransactionFilter extends RouteFilter<Void> {

	private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);
	private EntityManagerFactory factory;

	//for test only.  I could not figure out a good way to test ALL the way from the edges without this
	private static int state = 0; //0 for start, 1 for in progress, 2 for rolled back, 3 for committed
	
	@Inject
	public TransactionFilter(EntityManagerFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		state = 0;
		if(Em.get() != null)
			throw new IllegalStateException("Are you stacking two TransactionFilters as this Em should not be set yet.  be aware you do not need to call addFilter for this filter and should just include the HibernateRouteModule");
		
		EntityManager em = factory.createEntityManager();
		Em.set(em);

		try {
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			log.info("Transaction beginning");
			
			return nextFilter.invoke(meta).handle((action, ex) -> commitOrRollback(em, action, ex));
		} finally {
			Em.set(null);
		}
	}

	private Action commitOrRollback(EntityManager em, Action action, Throwable t) {
		EntityTransaction tx = em.getTransaction();
		
		if(t != null) {
			log.info("Transaction being rolled back");
			rollbackTx(t, tx);
			closeEm(t, em);
			throw new RuntimeException(t);
		}
		
		log.info("Transaction being committed");
		commit(tx, em);
		
		return action;
	}
	
	private void commit(EntityTransaction tx, EntityManager em) {
		try {
			state = 3;
			tx.commit();
			
			em.close();
		} catch(Throwable e) {
			closeEm(e, em);
			throw new RuntimeException(e);
		}
	}

	private void closeEm(Throwable t, EntityManager em) {
		try {
			em.close();
		} catch(Throwable e) {
			t.addSuppressed(e);
		}
	}

	private void rollbackTx(Throwable t, EntityTransaction tx) {
		try {
			state = 2;
			tx.rollback();
		} catch(Throwable e) {
			t.addSuppressed(e);
		}
	}

	@Override
	public void initialize(Void initialConfig) {
	}

	//FOR TESTING ONLY.  not thread safe.  I HATE doing this, but verifying rollback on exception is critical that
	//we don't break it.
	public static int getState() {
		return state;
	}

}
