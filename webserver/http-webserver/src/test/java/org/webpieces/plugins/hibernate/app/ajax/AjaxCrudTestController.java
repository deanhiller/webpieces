package org.webpieces.plugins.hibernate.app.ajax;

import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_LIST_USERS;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.plugin.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class AjaxCrudTestController {

	private static Logger log = LoggerFactory.getLogger(AjaxCrudTestController.class);
	
	public Action userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserTestDbo> users = query.getResultList();
		boolean showEditPopup = Current.flash().isShowEditPopup();
		return Actions.renderThis(
				"users", users,
				"showPopup", showEditPopup);
	}
	
	public Action userAddEdit(Integer id) {
		if(id == null) {
			return Actions.renderThis("entity", new UserTestDbo());
		}
		
		UserTestDbo user = Em.get().find(UserTestDbo.class, id);
		return Actions.renderThis("entity", user);
	}

	public Redirect postSaveUser(UserTestDbo entity, String password) {
		if(password.length() < 4) {
			Current.validation().addError("password", "Value is too short");
		} 
		
		if(entity.getFirstName().length() < 3) {
			Current.validation().addError("entity.firstName", "First name must be more than 2 characters");
		}

		//all errors are grouped and now if there are errors redirect AND fill in
		//the form with what the user typed in along with errors
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			Current.flash().setError("Errors in form below");
			Current.flash().setShowEditPopup(true); //ensures we show the edit popup for listUsers on redisplay
			return Actions.redirectFlashAllSecure(AJAX_LIST_USERS, Current.getContext(), "password");
		}
		
		Current.flash().setMessage("User successfully saved");
		Current.flash().keep();
		
		Em.get().merge(entity);
        Em.get().flush();
        
		return Actions.redirect(AjaxCrudTestRouteId.AJAX_LIST_USERS);
	}

	public Render confirmDeleteUser(int id) {
		UserTestDbo user = Em.get().find(UserTestDbo.class, id);
		return Actions.renderThis("entity", user);
	}
	
	public Redirect postDeleteUser(int id) {
		UserTestDbo ref = Em.get().getReference(UserTestDbo.class, id);
		Em.get().remove(ref);
		Em.get().flush();
		Current.flash().setMessage("User deleted");
		Current.flash().keep();
		return Actions.redirect(AjaxCrudTestRouteId.AJAX_LIST_USERS);
	}
}
