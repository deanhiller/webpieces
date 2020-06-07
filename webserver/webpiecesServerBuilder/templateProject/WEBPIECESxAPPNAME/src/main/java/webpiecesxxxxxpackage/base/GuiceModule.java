package webpiecesxxxxxpackage.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.plugin.backend.login.BackendLogin;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import webpiecesxxxxxpackage.db.EducationEnum;
import webpiecesxxxxxpackage.db.RoleEnum;
import webpiecesxxxxxpackage.service.RemoteService;
import webpiecesxxxxxpackage.service.RemoteServiceImpl;
import webpiecesxxxxxpackage.service.SimpleStorageImpl;
import webpiecesxxxxxpackage.service.SomeLibrary;
import webpiecesxxxxxpackage.service.SomeLibraryImpl;
import webpiecesxxxxxpackage.web.login.BackendLoginImpl;
import webpiecesxxxxxpackage.web.tags.MyHtmlTagCreator;

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

		Multibinder<HtmlTagCreator> htmlTagCreators = Multibinder.newSetBinder(binder, HtmlTagCreator.class);
		htmlTagCreators.addBinding().to(MyHtmlTagCreator.class);
		
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
