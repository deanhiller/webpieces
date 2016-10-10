package org.webpieces.plugins.hibernate;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.util.filters.Service;

public class TransactionFilter implements RouteFilter<Void> {

	private EntityManagerFactory factory;

	@Inject
	public TransactionFilter(EntityManagerFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		EntityManager em = factory.createEntityManager();
		Em.set(em);
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		return nextFilter.invoke(meta).handle((action, ex) -> commitOrRollback(em, action, ex));
	}

	private Action commitOrRollback(EntityManager em, Action action, Throwable t) {
		EntityTransaction tx = em.getTransaction();
		
		if(t != null) {
			rollbackTx(t, tx);
			closeEm(t, em);
			throw new RuntimeException(t);
		}
		
		commit(tx, em);
		
		return action;
	}
	
	private void commit(EntityTransaction tx, EntityManager em) {
		try {
			tx.commit();
			
			em.close();
		} catch(Throwable e) {
			closeEm(e, em);
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
			tx.rollback();
		} catch(Throwable e) {
			t.addSuppressed(e);
		}
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
