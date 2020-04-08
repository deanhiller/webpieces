package WEBPIECESxPACKAGE.base;

import org.webpieces.templating.api.HtmlTagLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

import WEBPIECESxPACKAGE.web.tags.MyHtmlTagLookup;

public class PlatformOverrides implements Module {

	@Override
	public void configure(Binder binder) {		
		//This override is only needed if you want to add your own Html Tags to re-use
		//you can delete this code if you are not adding your own webpieces html tags
		//We graciously added #{mytag}# #{id}# and #{myfield}# as examples that you can
		//tweak so we add that binding here.  This is one example of swapping in pieces
		//of webpieces (pardon the pun)
		binder.bind(HtmlTagLookup.class).to(MyHtmlTagLookup.class).asEagerSingleton();;
	}

}
