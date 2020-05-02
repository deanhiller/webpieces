package org.webpieces.plugins.hibernate;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.HttpException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;

@Singleton
public class TransactionFilter extends RouteFilter<Void> {

	private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);
	private EntityManagerFactory factory;

	//for test only.  I could not figure out a good way to test ALL the way from the edges without this
	//BUT NOW webpieces has metrics so IF we metered it, we could just check the metrics but not sure this is something
	//we want metered?
	private static int state = 0; //0 for start, 1 for in progress, 2 for rolled back, 3 for committed
	private TxCompleters txCompleters;
	
	@Inject
	public TransactionFilter(EntityManagerFactory factory, TxCompleters txCompleters) {
		this.factory = factory;
		this.txCompleters = txCompleters;
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

	private Action commitOrRollback(EntityManager em, Action action, Throwable t) throws HttpException {
		EntityTransaction tx = em.getTransaction();
		
		if(t != null) {
			log.info("Transaction being rolled back");
			rollbackCloseSuppress(t, em, tx);
			if(t instanceof HttpException)
				throw (HttpException)t; //the platform needs the original HttpException to translate to an http code
			else
				throw new RuntimeException(t);
		}
		
		log.info("Transaction being committed");
		commit(tx, em);
		
		return action;
	}
	
	private void commit(EntityTransaction tx, EntityManager em) {
		state = 3;
		txCompleters.commit(tx, em);
	}

	private void rollbackCloseSuppress(Throwable t, EntityManager mgr, EntityTransaction tx) {
		state = 2;
		txCompleters.rollbackCloseSuppress(t, mgr, tx);
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
