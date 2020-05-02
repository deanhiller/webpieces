package org.webpieces.router.impl.routebldr;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class FilterComparator implements Comparator<FilterInfo> {

	@Override
	public int compare(FilterInfo o1, FilterInfo o2) {
		if(o1.getFilterApplyLevel() > o2.getFilterApplyLevel())
			return -1;
		else if(o1.getFilterApplyLevel() < o2.getFilterApplyLevel())
			return 1;
		
		return 0;
	}

}
