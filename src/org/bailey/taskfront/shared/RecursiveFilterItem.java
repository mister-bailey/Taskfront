package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.ListIterator;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.shared.PropertyRangeFilter;
import org.bailey.taskfront.shared.PropertySorter;
import org.bailey.taskfront.shared.RecursiveFilterItem.RecursiveInclusionFilter;
import java.lang.Double;

public abstract class RecursiveFilterItem extends FilteredListItem {
	 
	// more constructors please!
	public RecursiveFilterItem(){super();}
	public RecursiveFilterItem(String objectID){
		super(objectID);
	}

	
	// main work here is in addCandidate, moveCandidate, etc.
	// The convention will be that, if an ObjectItem (i.e., a lower level) asks us to add something, we
	// first see if it's already a member; if it's not, we check for inclusion; if inclusion, then we
	// create an abstraction around the item, propagating the filter downward through the hierarchy
	// (with attenuation).
	
	// In what cases would the filterItem add something, but it would already be included?
	// Until I have a likely answer that to question, I'll assume it doesn't happen.
	
	// Policy regarding the client adding items ---------------
	// I will assume that the item the client wants to add should be added directly (with no wrapping
	// or unwrapping) to my objectItem. Then when my objectItem notifies me of an added child
	// I will wrap it myself.
	// This means that if my wrapped children are dragndropped elsewhere, I am responsible for
	// unwrapping them first.
	
	// Creates a brand new RecursiveFilterItem to wrap a child of this item
	public Item createRecursiveFilterWrapper(Item item){
		RecursiveFilterItem recursiveItem = createBareRecursiveFilterWrapper(); // Initialized somehow
		recursiveItem.setNewWrapperUID(item.uid);
		recursiveItem.filter = ((RecursiveInclusionFilter)filter).createRecursiveFilter(this,item);
		recursiveItem.filter.storeFilterData(recursiveItem);
		recursiveItem.sorter = ((RecursiveSorter)sorter).createRecursiveSorter(this,item);
		recursiveItem.sorter.storeSorterData(recursiveItem);
		recursiveItem.initializeRepresentation(item.uid);
		// If initializeRepresentation populates the list, then there's nothing more to do here
		return recursiveItem;
	}
	
	// Should do nothing more than create an uninitialized instance of the appropriate subclass
	// For the moment, it only uses the ambient class as input
	protected abstract RecursiveFilterItem createBareRecursiveFilterWrapper();
	
	// Since we expect pretty much all of our children to be wrapped by representatives, if we're looking
	// to see if an item is a child of ours, we need to see if its representative is a child
	@Override
	public int childIndexOf(String id){
		return repIndexOf(id);
	}
		
	// In this case, filter and sorter will receive the (unwrapped) candidates
	public interface RecursiveInclusionFilter extends InclusionFilter {
		public RecursiveInclusionFilter createRecursiveFilter(Item parent,Item objectItem);
	}
	public interface RecursiveSorter extends Sorter{
		public abstract RecursiveSorter createRecursiveSorter(Item parent,Item objectItem);
	}

	
	// addCandidate inherits precisely from FilteredListItem. It just calls a different doCandidateAdd.
	// public void addCandidate(int candidateIndex, String itemID);
	protected void doCandidateAdd(int targetIndex, Item candidate) {
		// make a wrapper:
		Item item = createRecursiveFilterWrapper(candidate);
		// Currently, I'm comparing the unwrapped child to my wrapped children. See if this makes sense?
		for(Representative obs : representatives)obs.addCandidate(targetIndex,item.uid);
		children.add(targetIndex,item.uid);
		item.addParentObserver(this);
	}



}
