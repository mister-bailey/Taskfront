package org.bailey.taskfront.shared;

public interface InclusionFilter {
	// Should we include this item?
	public boolean include(FilteredListItem parent,Item candidate);
	
	// given Item child, which has not yet been placed at index, set child's properties so that it "ought to" be there
	// (and be included).
	// IMPORTANT CAVEAT: if child's properties are already adequate for it to be there, it should not be changed.
	public void setInclusionProperty(FilteredListItem parent,Item candidate, int index);
	public void setInclude(FilteredListItem parent,Item candidate,boolean include);
	public void storeFilterData(Item item);
}

