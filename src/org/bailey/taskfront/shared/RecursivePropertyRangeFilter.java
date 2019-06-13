package org.bailey.taskfront.shared;

import org.bailey.taskfront.shared.RecursiveFilterItem.RecursiveInclusionFilter;

public class RecursivePropertyRangeFilter extends PropertyRangeFilter implements RecursiveInclusionFilter {
	public RecursivePropertyRangeFilter(String key,double start, double end) {super(key,start, end);}
	public RecursiveInclusionFilter createRecursiveFilter(Item parent, Item objectItem) {return this;}		
}
