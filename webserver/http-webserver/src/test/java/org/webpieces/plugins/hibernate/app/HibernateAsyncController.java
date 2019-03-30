package org.webpieces.plugins.hibernate.app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@Singleton
public class HibernateAsyncController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateController.class);
	private final Executor exec;
	
	@Inject
	public HibernateAsyncController(Executor exec) {
		this.exec = exec;
	}
	public CompletableFuture<Redirect> save() {
		EntityManager mgr = Em.get();		
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		kickOffAsyncResponse(future);
		return future.thenApply(intVal -> runSave(mgr));
	}
		
	public Redirect runSave(EntityManager mgr) {
		UserTestDbo user = new UserTestDbo();
		user.setEmail("dean@async.xsoftware.biz");
		user.setName("SomeName");
		
		mgr.persist(user);

		mgr.flush();

		return Actions.redirect(HibernateRouteId.ASYNC_DISPLAY_ENTITY, "id", user.getId());
	}
	
	public CompletableFuture<Render> display(Integer id) {
		EntityManager mgr = Em.get();
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		kickOffAsyncResponse(future);
		return future.thenApply(intVal -> runDisplay(mgr, id));
	}
	
	private Render runDisplay(EntityManager mgr, Integer id) {
		UserTestDbo user = mgr.find(UserTestDbo.class, id);
		return Actions.renderThis("user", user);
	}

	public CompletableFuture<Render> entityLoad(Integer id) {
		EntityManager mgr = Em.get();
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		kickOffAsyncResponse(future);
		return future.thenApply(intVal -> runEntityLoad(mgr, id));
	}
	
	private Render runEntityLoad(EntityManager mgr, Integer id) {
		UserTestDbo user = mgr.find(UserTestDbo.class, id);
		log.info("user loaded");
		return Actions.renderThis("user", user);
	}
	
	private void kickOffAsyncResponse(CompletableFuture<Integer> future) {
		exec.execute(new Runnable() {
			@Override
			public void run() {
				future.complete(1);
			}
		});
	}
}
