package WEBPIECESxPACKAGE.base;

import org.webpieces.templating.api.HtmlTagLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

import WEBPIECESxPACKAGE.web.tags.MyHtmlTagLookup;
import io.micrometer.core.instrument.MeterRegistry;

public class PlatformOverrides implements Module {

	private MeterRegistry metrics;

	public PlatformOverrides(MeterRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public void configure(Binder binder) {
		//This is a required override to wire in your metrics or if not, a default is installed that
		//just rewrites a snapshot every minute(there is no history saved)
		binder.bind(MeterRegistry.class).toInstance(metrics);
		
		
		//This override is only needed if you want to add your own Html Tags to re-use
		//you can delete this code if you are not adding your own webpieces html tags
		//We graciously added #{mytag}# #{id}# and #{myfield}# as examples that you can
		//tweak so we add that binding here.  This is one example of swapping in pieces
		//of webpieces (pardon the pun)
		binder.bind(HtmlTagLookup.class).to(MyHtmlTagLookup.class).asEagerSingleton();;
	}

}
