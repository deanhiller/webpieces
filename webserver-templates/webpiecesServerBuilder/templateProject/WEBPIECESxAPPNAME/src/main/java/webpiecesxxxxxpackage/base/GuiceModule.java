package webpiecesxxxxxpackage.base;

import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.plugin.backend.login.BackendLogin;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import webpiecesxxxxxpackage.db.EducationEnum;
import webpiecesxxxxxpackage.db.RoleEnum;
import webpiecesxxxxxpackage.service.*;
import webpiecesxxxxxpackage.web.login.BackendLoginImpl;
import webpiecesxxxxxpackage.web.tags.MyHtmlTagCreator;

import javax.inject.Inject;
import javax.inject.Singleton;

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

	    //since GlobalAppContext is a singleton, ApplicationContext will be to and will be the same
		binder.bind(ApplicationContext.class).to(GlobalAppContext.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	public RemoteService create(RESTClientCreator creator, YourServiceDirectory directory) {
		return creator.createClient(RemoteService.class, directory.getRemoteSvc());
	}
}
