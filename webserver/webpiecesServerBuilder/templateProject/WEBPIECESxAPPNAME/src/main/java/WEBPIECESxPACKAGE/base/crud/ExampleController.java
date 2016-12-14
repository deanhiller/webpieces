package WEBPIECESxPACKAGE.base.crud;

import static WEBPIECESxPACKAGE.base.routes.ExampleRouteId.ADD_USER_FORM;
import static WEBPIECESxPACKAGE.base.routes.ExampleRouteId.EDIT_USER_FORM;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import WEBPIECESxPACKAGE.base.routes.ExampleRouteId; 

@Singleton
public class ExampleController {

	private static Logger log = LoggerFactory.getLogger(ExampleController.class);
	
	public Action userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserDbo> users = query.getResultList();
		return Actions.renderThis("users", users);
	}
	
	public Action userAddEdit(Integer id) {
		if(id == null) {
			return Actions.renderThis("user", new UserDbo());
		}
		
		EntityManager mgr = Em.get();
		UserDbo user = mgr.find(UserDbo.class, id);
		return Actions.renderThis("user", user);
	}

	public Redirect postSaveUser(UserDbo user) {
		if(user.getPassword().length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			Current.flash().setMessage("Errors in form below");
			Actions.redirectFlashAllAddEdit(
					ADD_USER_FORM, EDIT_USER_FORM, Current.getContext(), 
					"id", user.getId(), "other", "value", "key3", "value3");
		}
		
		Current.flash().setMessage("User successfully saved");
		Em.get().merge(user);
        Em.get().flush();
        
		return Actions.redirect(ExampleRouteId.LIST_USERS);
	}
	
	public Redirect postDeleteUser(int id) {
		UserDbo ref = Em.get().getReference(UserDbo.class, id);
		Em.get().remove(ref);
		return Actions.redirect(ExampleRouteId.LIST_USERS);
	}
}
