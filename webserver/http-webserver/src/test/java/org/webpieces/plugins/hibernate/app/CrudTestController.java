package org.webpieces.plugins.hibernate.app;

import static org.webpieces.plugins.hibernate.app.HibernateRouteId.ADD_USER_PAGE;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.EDIT_USER_PAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;
import org.webpieces.plugins.hibernate.app.dbo.Role;
import org.webpieces.plugins.hibernate.app.dbo.UserRoleDbo;
import org.webpieces.plugins.hibernate.app.dbo.UserTestDbo;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.FlashAndRedirect;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CrudTestController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAsyncController.class);
	
	public Render userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserTestDbo> users = query.getResultList();
		return Actions.renderThis("users", users);
	}
	
	public Render userAddEdit(Integer id) {
		if(id == null) {
			return Actions.renderThis("entity", new UserTestDbo(),
					"levels", LevelEducation.values(),
					"roles", Role.values(),
					"password", null);
		}
		
		UserTestDbo user = UserTestDbo.findWithJoin(Em.get(), id);
		return Actions.renderThis(
				"entity", user,
				"levels", LevelEducation.values(),
				"roles", Role.values(),
				"password", null);
	}
	
	public Redirect postSaveUser(UserTestDbo entity, String password) {
		if(password == null) {
			Current.validation().addError("password", "password is required");
		} else if(password.length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			FlashAndRedirect redirect = new FlashAndRedirect(Current.getContext(), "Errors in form below");
			redirect.setSecureFields("password"); //make sure secure fields are not put in flash cookie!!!
			redirect.setIdFieldAndValue("id", entity.getId());
			return Actions.redirectFlashAll(ADD_USER_PAGE, EDIT_USER_PAGE, redirect);
		}
		
		Current.flash().setMessage("User successfully saved");
		Em.get().merge(entity);
        Em.get().flush();
        
		return Actions.redirect(HibernateRouteId.LIST_USERS);
	}
	
	public Render multiSelect(Integer id) {
		if(id == null) {
			return Actions.renderThis("entity", new UserTestDbo(),
					"levels", LevelEducation.values(),
					"roles", Role.values(),
					"selectedRoles", new ArrayList<>());
		}
		
		UserTestDbo user = UserTestDbo.findWithJoin(Em.get(), id);
		List<UserRoleDbo> roles = user.getRoles();
		List<Role> selectedRoles = roles.stream().map(r -> r.getRole()).collect(Collectors.toList());
		return Actions.renderThis(
				"entity", user,
				"levels", LevelEducation.values(),
				"roles", Role.values(),
				"selectedRoles", selectedRoles,
				"password", null);
	}
	
	public Redirect postSaveUserForMultiSelect(UserTestDbo entity, List<Role> selectedRoles, String password) {
		if(password == null) {
			Current.validation().addError("password", "password is required");
		} else if(password.length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			FlashAndRedirect redirect = new FlashAndRedirect(Current.getContext(), "Errors in form below");
			redirect.setSecureFields("entity.password"); //make sure secure fields are not put in flash cookie!!!
			redirect.setIdFieldAndValue("id", entity.getId());
			return Actions.redirectFlashAll(ADD_USER_PAGE, HibernateRouteId.MULTISELECT, redirect);
		}
		
		Current.flash().setMessage("User successfully saved");
		Em.get().merge(entity);
        Em.get().flush();
        
		return Actions.redirect(HibernateRouteId.LIST_USERS);
	}
	
	public Render confirmDeleteUser(Integer id) {
		UserTestDbo user = Em.get().find(UserTestDbo.class, id);
		return Actions.renderThis("user", user);
	}
	
    public Redirect postDeleteUser(Integer id) {
    	UserTestDbo user = Em.get().getReference(UserTestDbo.class, id);
    	Em.get().remove(user);
    	Em.get().flush();
    	
    	Current.flash().setMessage("User "+user.getFirstName()+" "+user.getLastName()+" was deleted");
    	return Actions.redirect(HibernateRouteId.LIST_USERS);
    }
	
}
