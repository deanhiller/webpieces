package WEBPIECESxPACKAGE.base.tags;

import javax.inject.Inject;

import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.templating.impl.tags.CustomTag;

public class MyHtmlTagLookup extends HtmlTagLookup {

	@Inject
	public MyHtmlTagLookup(TemplateConfig config, RouterLookup lookup, ConverterLookup converter) {
		super(config, lookup, converter);
		//add any custom tags you like here...
		put(new CustomTag("/WEBPIECESxPACKAGE/base/tags/mytag.tag"));
		put(new IdTag(converter, "/WEBPIECESxPACKAGE/base/tags/id.tag"));
		
		//you can also override(subclass or whatever) any tag by replacing it in the map
		//This replaces the field tag
		//put(new FieldTag(converter, "/WEBPIECESxPACKAGE/base/tags/field.tag"));
		
		//This one subclasses FieldTag to add yet another field tag using #{myfield}# such that
		//we then can use #{field}# and #{myfield}# for different types of fields
		put(new MyFieldTag(converter));
	}
	
}
