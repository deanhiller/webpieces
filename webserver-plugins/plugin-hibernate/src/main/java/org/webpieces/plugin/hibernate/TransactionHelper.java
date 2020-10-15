package org.webpieces.plugin.hibernate;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class TransactionHelper {

	private EntityManagerFactory factory;
	private TxCompleters txCompleters;

	TransactionHelper h = new TransactionHelper(null, null);

	@Inject
	public TransactionHelper(EntityManagerFactory factory, TxCompleters txCompleters) {
		this.factory = factory;
		this.txCompleters = txCompleters;
	}
	
	public <Resp> Resp runWithEm(Function<EntityManager, Resp> function) {
		EntityManager mgr = factory.createEntityManager();
		
		Em.set(mgr);
		try {
			Resp resp = function.apply(mgr);
			mgr.close();	
			return resp;
		} catch(RuntimeException e) {
			tryClose(mgr, e);
			throw e;
		} finally {
			Em.set(null);
		}
	}

	public <Resp> Resp runWithEm(Supplier<Resp> function) {
		EntityManager mgr = factory.createEntityManager();
		
		Em.set(mgr);
		try {
			Resp resp = function.get();
			mgr.close();	
			return resp;
		} catch(RuntimeException e) {
			tryClose(mgr, e);
			throw e;
		} finally {
			Em.set(null);
		}
	}
	
	public <Resp> Resp runTransaction(EntityManager mgr, Supplier<Resp> function) {
		EntityTransaction tx = mgr.getTransaction();

		if(tx.isActive()) {
			throw new IllegalStateException("You cannot call runTransaction in a transaction.  Call this method inside the function you pass to runWithEm.");
		}
		
		tx.begin();
				
		try {
			Resp resp = function.get();
			txCompleters.commit(tx, mgr);
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackCloseSuppress(e, mgr, tx);
			throw e; //rethrow
		}
	}
	
	private void tryClose(EntityManager mgr, RuntimeException e) {
		try {
			mgr.close();
		} catch(RuntimeException nextExc) {
			//This exception needs to be added to original.
			//The original exception is more important
			e.addSuppressed(nextExc);
		}
	}

	public <Resp> Resp runTransaction(Function<EntityManager, Resp> function) {
		EntityManager mgr = factory.createEntityManager();
		
		if(mgr.getTransaction().isActive()) {
			throw new IllegalStateException("You cannot call runTransaction in a transaction");
		}
		
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();
		
		Em.set(mgr);
		
		try {
			Resp resp = function.apply(mgr);
			txCompleters.commit(tx, mgr);
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackCloseSuppress(e, mgr, tx);
			throw e; //rethrow
		} finally {
			Em.set(null);
		}
	}


	public <Resp> Resp runTransaction(Supplier<Resp> function) {
		EntityManager mgr = factory.createEntityManager();
		
		if(mgr.getTransaction().isActive()) {
			throw new IllegalStateException("You cannot call runTransaction in a transaction");
		}
		
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();
		
		Em.set(mgr);
		
		try {
			Resp resp = function.get();
			txCompleters.commit(tx, mgr);
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackCloseSuppress(e, mgr, tx);
			throw e; //rethrow
		} finally {
			Em.set(null);
		}
	}
}
