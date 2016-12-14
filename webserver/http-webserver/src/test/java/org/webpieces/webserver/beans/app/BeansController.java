package org.webpieces.webserver.beans.app;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.webserver.basic.app.biz.Address;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.basic.app.biz.UserDto;
import org.webpieces.webserver.tags.app.Account;

@Singleton
public class BeansController {
	
	@Inject
	private SomeLib lib1;
	@Inject
	private SomeOtherLib lib;

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	public Action urlEncoding(String user) {
		return Actions.renderThis("user", user);
	}
	
	public Action pageParam() {
		Current.flash().put("testkey", "testflashvalue");
		return Actions.renderThis("user", "Dean Hiller");
	}

	public CompletableFuture<Action> pageParamAsync() {
		CompletableFuture<Action> future = new CompletableFuture<>();
		RequestContext ctx = Current.getContext();

		executor.schedule(new Runnable() {
			@Override
			public void run() {
				ctx.getFlash().put("testkey", "testflashvalue");
				future.complete(Actions.renderThis("user", "Dean Hiller"));
			}
		}, 2, TimeUnit.MILLISECONDS);

		return future;
	}

	public Action flashMessage() {
		Current.flash().setMessage("it worked");
		return Actions.renderThis();
	}

	public Action validationError() {
		Current.validation().setGlobalError("it failed");
		return Actions.renderThis();
	}

	public Action userForm() {
		return Actions.renderThis();
	}
	
	public Redirect postUser(UserDto user, String password) {
		//Validate any other stuff you need here adding errors and such
		lib1.validateUser(user);
		
		RequestContext ctx = Current.getContext();
		if(Current.validation().hasErrors()) {
			return Actions.redirectFlashAllSecure(BeansRouteId.USER_FORM_ROUTE, ctx, "password");
		}
		
		lib.saveUser(user);
		return Actions.redirect(BeansRouteId.LIST_USERS_ROUTE);
	}
	
	public Action listUsers() {
		return Actions.renderThis();
	}
	
	public Action arrayForm() {
		UserDto user = new UserDto();
		//Add how many relatives you want added(adding null is fine here unless you have defaults in the user object
		//in which case you could add new UserDto as well
		for(int i = 0; i < 3; i++) {
			Account acc = new Account();
			if(i == 0)
				acc.setName("FirstAccName");
			else
				acc.setName("SecondAccName");
			user.getAccounts().add(acc);
			acc.getAddresses().add(null);
			Address address2 = new Address();
			address2.setStreet("someStreet2-"+i);
			acc.getAddresses().add(address2);
		}
			
		return Actions.renderThis("user", user);
	}
	
	public Redirect postArray(UserDto user, List<UserDto> users) {
		lib1.validateUser(user);
		Current.validation().addError("user.accounts[0].addresses[0].street", "This is too ugly a street name");
		if(Current.validation().hasErrors()) {
			RequestContext ctx = Current.getContext();
			return Actions.redirectFlashAllSecure(BeansRouteId.ARRAY_FORM_ROUTE, ctx, "password");
		}
		
		return Actions.redirect(BeansRouteId.ARRAY_FORM_ROUTE);
	}
}
