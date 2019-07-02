package org.webpieces.plugins.backend.menu;

import java.util.Comparator;

public class HeadingCompare implements Comparator<SingleMenu> {

	@Override
	public int compare(SingleMenu o1, SingleMenu o2) {
		return o1.getMenuCategory().getMenuTitle().compareTo(o2.getMenuCategory().getMenuTitle());
	}

}
