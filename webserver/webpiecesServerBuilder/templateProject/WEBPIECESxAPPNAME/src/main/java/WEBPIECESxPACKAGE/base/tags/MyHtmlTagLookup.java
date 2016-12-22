package WEBPIECESxPACKAGE.base.tags;

import javax.inject.Inject;

import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.CustomTag;
import org.webpieces.templating.impl.tags.FieldTag;

public class MyHtmlTagLookup extends HtmlTagLookup {

	@Inject
	public MyHtmlTagLookup(TemplateConfig config) {
		super(config);
		//add any custom tags you like here...
		put(new CustomTag("/WEBPIECESxPACKAGE/base/tags/mytag.tag"));
	}

	/**
	 * Override the Field Tag
	 */
	@Override
	protected void addFieldTag(TemplateConfig config) {
		put(new FieldTag("/WEBPIECESxPACKAGE/base/tags/field.tag", "error"));		
	}
	
}
