package WEBPIECESxPACKAGE.base;

import org.webpieces.ctx.api.WebConverter;
import org.webpieces.router.api.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import WEBPIECESxPACKAGE.base.libs.Education;
import WEBPIECESxPACKAGE.base.libs.RemoteService;
import WEBPIECESxPACKAGE.base.libs.RemoteServiceImpl;
import WEBPIECESxPACKAGE.base.libs.SomeLibrary;
import WEBPIECESxPACKAGE.base.libs.SomeLibraryImpl;

public class GuiceModule implements Module {

	//This is where you would put the guice bindings you need though generally if done
	//right, you won't have much in this file.
	
	//If you need more Guice Modules as you want to scale, just modify WEBPIECESxCLASSMeta which returns
	//the list of all the Guice Modules in your application
	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Binder binder) {
		//all modules have access to adding their own Startable objects to be run on server startup
		Multibinder<Startable> uriBinder = Multibinder.newSetBinder(binder, Startable.class);
	    uriBinder.addBinding().to(PopulateDatabase.class);

		Multibinder<WebConverter> conversionBinder = Multibinder.newSetBinder(binder, WebConverter.class);
		conversionBinder.addBinding().to(Education.EduConverter.class);
	    
	    binder.bind(SomeLibrary.class).to(SomeLibraryImpl.class);
	    binder.bind(RemoteService.class).to(RemoteServiceImpl.class);
	}

}
