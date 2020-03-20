package WEBPIECESxPACKAGE.web.tags;

import org.webpieces.templating.api.HtmlTagLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TagLookupOverride implements Module {

	@Override
	public void configure(Binder binder) {
		//NOTE: This is how you install your own Tags into webpieces
		binder.bind(HtmlTagLookup.class).to(MyHtmlTagLookup.class).asEagerSingleton();;
	}

}
