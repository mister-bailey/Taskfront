package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsObject;
import org.bailey.taskfront.shared.RecursiveFilterItem.RecursiveInclusionFilter;

import com.google.gwt.user.client.Random;


public abstract class FilteredListItem extends RepresentativeItem {
	
	// Outline of the notification scheme --------------
	// We have two different kinds of directly tracked "notification relationships"
	// 1. Representative ---> Object
	// 2. Parent ---> Child
	// Whenever an item becomes a Parent to a Child, for whatever reason, the direct Child maintains a
	// reference to the Parent. When the Child has an internal update, it calls Parent.updateChild(Child).
	// The Parent will typically notify its own Representatives that one of their candidates has updated,
	// by calling Parent.updateCandidate(Child). The Parent does not, in this instance, test the Child for
	// membership/order---only "Candidates" are tested for membership/order, not "Children".
	// An Object should notify its Representative whenever something significant happens to itself or its
	// children. The Object calls methods like Representative.addCandidate/moveCandidate/updateCandidate,
	// offering its Child in the role of Candidate to the Representative. The Representative will typically
	// test the Candidate for membership as one of its Children, or test to see where in the list it goes.


	//public ArrayList<String> filterList;  // updated whenever we want to iterate, cached for performance???
	
	public FilteredListItem(){super();}
	public FilteredListItem(String objectID){
		super(objectID);
	}
	
	/*public void initialize(String text, String filterID){
		super.initialize(text, filterID);
		populateUIDlist();
	}*/
	
	public InclusionFilter filter;
	
	public Sorter sorter;
	
	
	// ---------------------------
	// --------------------------------
	
	//public abstract boolean include(Item item);
	//public abstract void setInclude(Item item,boolean include);
	//public abstract double childOrder(Item child);
	

	public int childIndexOf(String itemID){
		return directIndexOf(itemID);
	}
	
	/*protected void filterClear(){  // Do I need this?  I never call ListObserver.clearList
		while(children.size()>0){
			for(Representative obs : representatives)obs.removeCandidate(0);
			children.remove(0);
		}
	}*/
	
	// This *does* update any observers
	public void populateUIDlist() {  // UIDlist of filter item should already be populated!
		filterList = getObjectItem().children;
		if(filterList==null) return;
		for(String itemID : filterList){
			filterAdd(/*UIDlist==null ? 0 : */children.size(),itemID);   // TODO lazybones
		}
	}

	// ******* This is an out of date method and should be changed/removed *********
	public void setFilterAndUpdate(String id){
		setFilter(id);
		updateObservers();
		populateUIDlist();
		if(selfSave) this.save();
	}
	// the following *does* repopulate the list, updates observers, and saves if need be
	/*public void replaceFilter(String id){ // DOES repopulate list.
		if(children != null){
			Iterator<String> itr = children.iterator();
			while(itr.hasNext()){
				String childID = itr.next();
				for(Representative obs : representatives) obs.removeCandidate(childID);
				itr.remove();
			}
		}
		getFilterItem().removeObserver(filter);
		setFilter(id);
		populateUIDlist();
		updateObservers();
		if(selfSave)save();
	}*/
	
	public void deserialize(SerializedComponents c){
		this.uid = c.uid;
		if(c.contentString == null) return; // TODO: raise an error?  generate from scratch?
		deserializeContent(c.contentString);
		//loadViewBinsFromContent();
		setFilter(this.getStringProperty("filterID"));
		populateUIDlist();
		updateObservers();
	}
	


	//public void saveMoveChild(int i, String childID){} // Do nothing
	//public void saveRemoveChild(String childID){} // Do nothing
	//public void delete(){} // Do nothing
	//public abstract void updateChild(Item item);
	
	//public abstract void add(int indexTo, String itemID);
	//public abstract String move(int indexTo, int indexFrom);
	
	@Override
	public void addCandidate(int index, String itemID) {
		Item item = Database.getItem(itemID);
		if(!filter.include(this,item))return;
		doCandidateAdd(sorter.targetIndex(this,item),item);
	}
	protected void doCandidateAdd(int targetIndex, Item candidate) {
		for(Representative rep : representatives)rep.addCandidate(targetIndex,candidate.uid);
		children.add(targetIndex,candidate.uid);
		candidate.addParentObserver(this);
		if(selfSave) saveMoveChild(targetIndex,candidate.uid);
	}
	@Override
	public void moveCandidate(int filterIndexFrom, int filterIndexTo) {
		// Do nothing, unless the ordering depends on the filterItem's ordering
	}
	// ************* ADD/MOVE/REMOVE SAVING IN THIS AND RECURSIVE... ***************
	@Override
	public void removeCandidate(String uid){ // override if need be
		int index = childIndexOf(uid);
		if(index >= 0) super.removeChild(index);
	}

	/*protected void candidateRemovePosition(int pos){ // ????? Doesn't seem like a good method!!!!
		removeCandidate(children.get(pos));	
	}*/
	@Override
	public void updateCandidate(Item parent, Item candidate) {
		if(!filter.include(this,candidate)){
			/*if(childIndexOf(candidate.uid) >= 0)*/ removeCandidate(candidate.uid);  // MAJOR CHOKE POINT
		} else {
			int indexFrom = childIndexOf(candidate.uid);
			if(indexFrom<0) doCandidateAdd(sorter.targetIndex(this,candidate), candidate);
			else doMoveChild(sorter.targetIndex(this,candidate, indexFrom),indexFrom);
		}
	}
	public void doMoveChild(int indexTo,int indexFrom){
		if(indexTo==indexFrom || indexTo==indexFrom+1) return;
		for(Representative rep : representatives){
			rep.moveCandidate(indexTo, indexFrom);
			//if(rep instanceof Parent) ((Parent)rep).updateChild(candidate); No need. Candidate already updates reps
		}
		String moveUID = children.remove(indexFrom); // Uses the currently-existing UID, in case wrapping
		children.add(indexTo <= indexFrom ? indexTo : indexTo-1,moveUID);
		if(selfSave) saveMoveChild(indexTo,moveUID);		
	}
	@Override
	public void addChild(int index, String itemID) {
		int indexFrom=childIndexOf(itemID);
		if(indexFrom >= 0){
			moveChild(index,indexFrom);
			return;
		}
		
		Item item = Database.getItem(itemID);
		// if the item already has grounds to be included, then don't change it, but just pass the add command down to filterItem.
		if(!filter.include(this,item))filter.setInclusionProperty(this,item, index);
		
		// **** does it matter what order things get added to the filterItem list?? ****
		Item objectItem = getObjectItem();
		if(objectItem.childIndexOf(itemID)<0){
			//int objectIndexTo = (index >= children.size()) ? objectItem.children.size() : objectItem.UIDindexOf(children.get(index));
			objectItem.addChild(objectItem.children.size(), itemID);
			// **** will the above check UIDindexOf again??? ****
		}
	}
	public String moveChild(int indexTo, int indexFrom) {
		String itemID = children.get(indexFrom);
		filter.setInclusionProperty(this,Database.getItem(itemID),indexTo);
		return itemID;
	}
	public void removeChild(String itemID) {
		filter.setInclude(this,Database.getItem(itemID),false);
	}
	
	public void hardRemove(String itemID){
		getObjectItem().hardRemove(itemID);
	}


	public final double getStartOrder(){return 0;}
	public final double getEndOrder(){return 0;}
	public final void setChildOrder(Item item, double order){}


}
