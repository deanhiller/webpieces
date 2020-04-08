package WEBPIECESxPACKAGE.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.plugins.backend.login.BackendLogin;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import WEBPIECESxPACKAGE.GlobalAppContext;
import WEBPIECESxPACKAGE.db.EducationEnum;
import WEBPIECESxPACKAGE.db.RoleEnum;
import WEBPIECESxPACKAGE.service.RemoteService;
import WEBPIECESxPACKAGE.service.RemoteServiceImpl;
import WEBPIECESxPACKAGE.service.SimpleStorageImpl;
import WEBPIECESxPACKAGE.service.SomeLibrary;
import WEBPIECESxPACKAGE.service.SomeLibraryImpl;
import WEBPIECESxPACKAGE.web.login.BackendLoginImpl;

public class GuiceModule implements Module {

	private static final Logger log = LoggerFactory.getLogger(GuiceModule.class);
	
	//This is where you would put the guice bindings you need though generally if done
	//right, you won't have much in this file.
	
	//If you need more Guice Modules as you want to scale, just modify ServerMeta which returns
	//the list of all the Guice Modules in your application
	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Binder binder) {
		
		log.info("running module");
		
		//all modules have access to adding their own Startable objects to be run on server startup
		Multibinder<Startable> uriBinder = Multibinder.newSetBinder(binder, Startable.class);
	    uriBinder.addBinding().to(PopulateDatabase.class);

		Multibinder<ObjectStringConverter> conversionBinder = Multibinder.newSetBinder(binder, ObjectStringConverter.class);
		conversionBinder.addBinding().to(EducationEnum.WebConverter.class);
		conversionBinder.addBinding().to(RoleEnum.WebConverter.class);
	    
	    binder.bind(SomeLibrary.class).to(SomeLibraryImpl.class);
	    binder.bind(RemoteService.class).to(RemoteServiceImpl.class).asEagerSingleton();

	    //Must bind a SimpleStorage for plugins to read/save data and render their html pages
	    binder.bind(SimpleStorage.class).to(SimpleStorageImpl.class).asEagerSingleton();
	    
	    //Must bind a BackendLogin for the backend plugin(or remove the backend plugin)
	    binder.bind(BackendLogin.class).to(BackendLoginImpl.class).asEagerSingleton();
	    
	    GlobalAppContext ctx = new GlobalAppContext();
	    //MUST be bound to ApplicationContext to be available in webpages..
		binder.bind(ApplicationContext.class).toInstance(ctx);
		binder.bind(GlobalAppContext.class).toInstance(ctx);
	}

}
