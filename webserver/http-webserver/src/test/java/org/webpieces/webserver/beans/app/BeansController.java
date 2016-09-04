package org.webpieces.webserver.beans.app;

import java.util.List;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.webserver.basic.biz.Address;
import org.webpieces.webserver.basic.biz.SomeLib;
import org.webpieces.webserver.basic.biz.SomeOtherLib;
import org.webpieces.webserver.basic.biz.UserDbo;
import org.webpieces.webserver.tags.app.Account;

public class BeansController {
	
	@Inject
	private SomeLib lib1;
	@Inject
	private SomeOtherLib lib;
	
	public Action urlEncoding(String user) {
		return Actions.renderThis("user", user);
	}
	
	public Action pageParam() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Action userForm() {
		return Actions.renderThis();
	}
	
	public Redirect postUser(UserDbo user, String password) {
		//Validate any other stuff you need here adding errors and such
		lib1.validateUser(user);
		
		RequestContext ctx = Current.getContext();
		if(Current.validation().hasErrors()) {
			return Actions.redirectFlashAll(BeansRouteId.USER_FORM_ROUTE, ctx, "password");
		}
		
		lib.saveUser(user);
		return Actions.redirect(BeansRouteId.LIST_USERS_ROUTE);
	}
	
	public Action listUsers() {
		return Actions.renderThis();
	}
	
	public Action arrayForm() {
		UserDbo user = new UserDbo();
		//Add how many relatives you want added(adding null is fine here unless you have defaults in the user object
		//in which case you could add new UserDbo as well
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
	
	public Redirect postArray(UserDbo user, List<UserDbo> users) {
		lib1.validateUser(user);
		Current.validation().addError("user.accounts[0].addresses[0].street", "This is too ugly a street name");
		if(Current.validation().hasErrors()) {
			RequestContext ctx = Current.getContext();
			return Actions.redirectFlashAll(BeansRouteId.ARRAY_FORM_ROUTE, ctx, "password");
		}
		
		return Actions.redirect(BeansRouteId.ARRAY_FORM_ROUTE);
	}
}