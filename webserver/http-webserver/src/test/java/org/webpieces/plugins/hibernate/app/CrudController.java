package org.webpieces.plugins.hibernate.app;

import static org.webpieces.plugins.hibernate.app.HibernateRouteId.ADD_USER_PAGE;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.EDIT_USER_PAGE;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.UserDbo;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@Singleton
public class CrudController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAsyncController.class);
	
	public Render userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserDbo> users = query.getResultList();
		return Actions.renderThis("users", users);
	}
	
	public Render userAddEdit(Integer id) {
		if(id == null) {
			return Actions.renderThis("user", new UserDbo());
		}
		
		EntityManager mgr = Em.get();
		UserDbo user = mgr.find(UserDbo.class, id);
		return Actions.renderThis("user", user);
	}
	
	public Redirect postSaveUser(UserDbo user, RouteId addEditRoute) {
		if(user.getPassword().length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			Current.flash().setMessage("Errors in form below");
			Actions.redirectFlashAllAddEdit(
					ADD_USER_PAGE, EDIT_USER_PAGE, Current.getContext(), 
					"id", user.getId(), "other", "value", "key3", "value3");
		}
		
		Current.flash().setMessage("User successfully saved");
		Em.get().merge(user);
        Em.get().flush();
        
		return Actions.redirect(HibernateRouteId.LIST_USERS);
	}
	
    public Redirect postDeleteUser(Integer id) {
    	UserDbo user = Em.get().getReference(UserDbo.class, id);
    	Em.get().remove(user);
    	Em.get().flush();
    	
    	Current.flash().setMessage("User "+user.getFirstName()+" "+user.getLastName()+" was deleted");
    	return Actions.redirect(HibernateRouteId.LIST_USERS);
    }
	
}
