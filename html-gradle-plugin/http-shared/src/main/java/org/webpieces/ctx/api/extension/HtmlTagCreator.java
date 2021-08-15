package org.webpieces.ctx.api.extension;

import java.util.List;


public interface HtmlTagCreator {

	/**
	 * It's really hard to keep router and template service separate so they do NOT depend on each other so
	 * we accept a Tag which has to be case to HtmlTag as only template service knows about HtmlTag
	 * @return
	 */
	List<Tag> createTags();

}
