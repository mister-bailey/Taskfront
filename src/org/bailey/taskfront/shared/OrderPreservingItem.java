package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsArrays;
import org.bailey.taskfront.client.JsObject;

import com.google.gwt.core.client.JsArrayString;

public abstract class OrderPreservingItem extends FilteredListItem {
	
	public OrderPreservingItem(){super();}
	public OrderPreservingItem(String text, String filterID){
		super(text,filterID);
	}

	// by default takes the order from filteritem, but you can override for a different sort method
	public double childOrder(Item child){
		return filterList.indexOf(child.uid);
	}
	/*public int targetIndex(double targetOrder){
		filterList = getFilterItem().UIDlist;
		return super.targetIndex(targetOrder);
	}
	public int targetIndex(double targetOrder, int startIndex){
		filterList = getFilterItem().UIDlist;
		return super.targetIndex(targetOrder, startIndex);
	}*/
	// override if you want the inclusion property to be set in a special way, depending on the index
	public void setInclusionProperty(Item item, int index){
		setInclude(item,true);
	}
	
	protected void filterAdd(int filterIndex, String itemID) {
		Item item = Database.getItem(itemID);
		if(!include(item))return;
		if(children==null) children=new ArrayList<String>();
		filterList = getObjectItem().children;
		int index = targetIndex(filterIndex);
		for(Representative obs : representatives)obs.candidateObserver().addCandidate(index,itemID);
		children.add(index,itemID);
		//item.parentObservers.add(this);
	}
	protected void filterAdd(String itemID){
		int filterIndex = getObjectItem().children.indexOf(itemID);
		if(filterIndex>=0) filterAdd(filterIndex,itemID);
	}

	protected void filterMove(int filterIndexTo, int filterIndexFrom) {
		filterMove(filterIndexTo,getObjectItem().children.get(filterIndexFrom));
	}
	protected void filterMove(int filterIndexTo, String itemID){
		Item item = Database.getItem(itemID);
		if(!include(item)){
			filterRemove(itemID);
		} else {
			int indexFrom = children.indexOf(itemID);
			int indexTo = targetIndex(filterIndexTo);
			if(indexFrom<0){
				for(Representative obs : representatives)obs.candidateObserver().addCandidate(indexTo,itemID);
				children.add(indexTo,itemID);
			} else {
				for(Representative obs : representatives)obs.candidateObserver().moveCandidate(indexTo, indexFrom);
				children.remove(indexFrom);
				children.add(indexTo <= indexFrom ? indexTo : indexTo-1,itemID);
			}
		}
	}

	public void filterUpdateChild(Item item) {
		if(!include(item)){
			if(children != null && children.contains(item.uid)) filterRemove(item.uid);
		} else {
			if(children==null) children=new ArrayList<String>();
			if(!children.contains(item.uid)) filterAdd(children.size(),item.uid);
			else{
				for(Representative obs : representatives) if(obs instanceof Parent) ((Parent)obs).updateChild(item);
			}
		}
	}

	public String moveChild(int indexTo, int indexFrom){
		if(indexTo==indexFrom || indexTo==indexFrom+1) return children.get(indexFrom);
		Item filterItem = getObjectItem();
		String fromID = children.get(indexFrom);
		
		if(indexTo>=children.size()){
			filterItem.moveChild(filterItem.children.size()-1,fromID);
		} else {		
			String toID = children.get(indexTo);		
			filterItem.moveChild(filterItem.children.indexOf(toID), fromID);
		}
		return fromID;				
	}
	
	public void addChild(int index, String itemID){
		Item filterItem = getObjectItem();
		if(children==null){
			children=new ArrayList<String>();
			if(filterItem.children==null) filterItem.children=new ArrayList<String>();
		}else{
			int indexFrom=children.indexOf(itemID);
			if(indexFrom >= 0){
				moveChild(index,indexFrom);
				return;
			}
		}
		
		Item item = Database.getItem(itemID);
		setInclusionProperty(item, index);
		
		if(filterItem.children==null){
			filterItem.addChild(0, itemID);
			return;
		}
		int filterIndexFrom = filterItem.children.indexOf(itemID);
		int filterIndexTo = (index >= children.size()) ? filterItem.children.size() : filterItem.children.indexOf(children.get(index));
		
		if(filterIndexFrom < 0) filterItem.addChild(filterIndexTo, itemID);
		else filterItem.moveChild(filterIndexTo, filterIndexFrom);
	}

}


