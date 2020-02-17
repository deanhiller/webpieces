package org.webpieces.plugins.hibernate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.HttpException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			log.info("Transaction beginning");

			CompletableFuture<Action> retVal = nextFilter.invoke(meta);

			//Controller's must create their own transaction IF they want to interact with database asynchonously
			//we have to commit or rollback right when done as we don't want to remote calls during a transaction
			//except those to the database
			commit(em);
			return retVal;
		} catch (Exception e) {
			//Controller's must create their own transaction IF they want to interact with database asynchonously
			//we have to commit or rollback right when done as we don't want to remote calls during a transaction
			//except those to the database
			throw rollback(em, e);;
		} finally {
			Em.set(null);
		}
	}

	private RuntimeException rollback(EntityManager em, Throwable t) {
		EntityTransaction tx = em.getTransaction();

		log.info("Transaction being rolled back");
		rollbackTx(t, tx);
		closeEm(t, em);
		if(t instanceof HttpException)
			return (HttpException)t; //the platform needs the original HttpException to translate to an http code
		else
			return new RuntimeException(t);
	}
	
	private void commit(EntityManager em) {
		EntityTransaction tx = em.getTransaction();
		log.info("Transaction being committed");
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
