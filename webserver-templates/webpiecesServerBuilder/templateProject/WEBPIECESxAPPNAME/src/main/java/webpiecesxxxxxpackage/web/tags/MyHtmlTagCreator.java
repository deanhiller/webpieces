package webpiecesxxxxxpackage.web.tags;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.ctx.api.extension.Tag;
import org.webpieces.router.api.PlatformInjector;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.impl.tags.CustomTag;

public class MyHtmlTagCreator implements HtmlTagCreator {

	private ConverterLookup converter;

	@Inject
	public MyHtmlTagCreator(PlatformInjector platformInjector) {
		this.converter = platformInjector.get().getInstance(ConverterLookup.class);
	}

	@Override
	public List<Tag> createTags() {
		List<Tag> tags = new ArrayList<Tag>();
		
		//add any custom tags you like here...
		tags.add(new CustomTag("/webpiecesxxxxxpackage/web/tags/mytag.tag"));
		tags.add(new IdTag(converter, "/webpiecesxxxxxpackage/web/tags/id.tag"));
		
		//you can also override(subclass or whatever) any tag by replacing it in the map
		//This replaces the field tag
		//put(new FieldTag(converter, "/webpiecesxxxxxpackage/base/tags/field.tag"));
		
		//This one subclasses FieldTag to add yet another field tag using #{myfield}# such that
		//we then can use #{field}# and #{myfield}# for different types of fields
		tags.add(new MyFieldTag(converter));
		
		return tags;
	}
	
}
