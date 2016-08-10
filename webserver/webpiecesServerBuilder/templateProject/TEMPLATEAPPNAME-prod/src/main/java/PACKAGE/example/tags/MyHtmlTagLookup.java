package PACKAGE.example.tags;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.CustomTag;

public class MyHtmlTagLookup extends HtmlTagLookup {

	@Inject
	public MyHtmlTagLookup(TemplateConfig config) {
		super(config);
		//add any custom tags you like here...
		put(new CustomTag("/PACKAGE/example/tags/mytag.tag"));
	}

}
