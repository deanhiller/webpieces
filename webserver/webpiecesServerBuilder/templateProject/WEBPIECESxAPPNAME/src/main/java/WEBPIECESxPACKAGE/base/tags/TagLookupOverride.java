package WEBPIECESxPACKAGE.base.tags;

import org.webpieces.templating.api.HtmlTagLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TagLookupOverride implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(HtmlTagLookup.class).to(MyHtmlTagLookup.class).asEagerSingleton();;
	}

}
