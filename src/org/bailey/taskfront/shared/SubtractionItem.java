package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsArrays;
import org.bailey.taskfront.client.JsObject;

public class SubtractionItem extends OrderPreservingItem {
	
	public ArrayList<SubtractionObserver> subtractionObservers;
	
	public List<String> references(){
		List<String> r = super.references();
		if(subtractionObservers != null){
			for(SubtractionObserver obs : subtractionObservers){
				r.add(obs.itemID);
			}
		}
		return r;
	}
	
	public SubtractionItem(String text, String filterID, List<String> subtractionFilters){
		super(text, filterID);
		content.set("type","SubtractionItem");
		setSubtractionFilters(subtractionFilters);
		populateUIDlist();
	}
	
	// doesn't (de)populate list!
	public void setSubtractionFilters(List<String> subtractionFilters){
		content.set("subtractionFilters",JsArrays.fromList(subtractionFilters));
		if(subtractionObservers != null){
			for(SubtractionObserver obs : subtractionObservers){
				obs.getItem().removeRepresentative(obs);
			}
		}
		subtractionObservers = new ArrayList<SubtractionObserver>();
		if(subtractionFilters != null){
			for(String filterID : subtractionFilters){
				subtractionObservers.add(new SubtractionObserver(filterID));
			}
		}
	}
	
	// assumes the UIDlist is currently empty!
	public void populateUIDlist(){
		ArrayList<String> filterList = getObjectItem().children;
		if(filterList != null) children = new ArrayList<String>(filterList);
		else {
			children = new ArrayList<String>();
			return;
		}
		if(subtractionObservers != null){
			for(SubtractionObserver obs : subtractionObservers){
				ArrayList<String> removeList = obs.getItem().children; 
				if(removeList != null) children.removeAll(removeList);
			}
		}
		for(String itemID : children){
			for(Representative obs : representatives)obs.candidateObserver().addLast(itemID);
		}
	}
	
	public void deserializeContent(String contentString){
		JsObject newContent = JsObject.eval(contentString);
		List<String> newSubtractionFilters = Arrays.asList(JsArrays.toArray(newContent.getJsArrayString("subtractionFilters")));
		if(content == null || !content.has("subtractionFilters") || content.getJsArrayString("subtractionFilters").length()==0){
			setSubtractionFilters(newSubtractionFilters);			
		} else if(!newSubtractionFilters.equals(
				Arrays.asList(JsArrays.toArray(content.getJsArrayString("subtractionFilters"))))){
			// TODO reset subtraction filters and repopulate list!
		}
		content = newContent;
	}
	
	public void filterUpdateChild(Item item){
		if(children.contains(item.uid)){
			for(Representative obs : representatives) {
				if(obs instanceof Parent) ((Parent)obs).updateChild(item);
			}
		}
	}
	
	protected void unsubtract(String itemID){
		filterList = getObjectItem().children;
		int filterIndex = -1;
		if(filterList != null) filterIndex = filterList.indexOf(itemID);
		if(filterIndex < 0) return;
		if(children==null) children=new ArrayList<String>();
		if(children.contains(itemID)) return;
		int index = targetIndex(filterIndex);
		for(Representative obs : representatives)obs.candidateObserver().addCandidate(index,itemID);
		children.add(index,itemID);
	}

	public boolean include(Item item) {
		if(subtractionObservers==null)return true;
		for(SubtractionObserver obs : subtractionObservers){
			Item subtractItem = obs.getItem();
			if(subtractItem.children != null && subtractItem.children.contains(item.uid)) return false;
		}
		return true;
	}
	
	// WARNING:
	// don't rely on the case include==false !   (we don't know what to do then, so we do nothing.)
	public void setInclude(Item item, boolean include) {
		if(!include)return;  // Don't rely on this
		for(SubtractionObserver obs : subtractionObservers){
			Item subtractItem = obs.getItem();
			if(subtractItem.children != null && subtractItem.children.contains(item.uid)) subtractItem.removeCandidate(item.uid);
		}
	}
	
	protected void filterUpdate() {
		updateObservers();
	}
	
	public double getStartOrder() {
		return 0;
	}

	public double getEndOrder() {
		return 0;
	}

	public void setChildOrder(Item item, double order) {
	}

	public class SubtractionObserver extends FilterObserver {
		public SubtractionObserver(String itemID) {
			super(itemID);
		}
		public void addCandidate(int index, String id){super.removeCandidate(id);}
		public void moveCandidate(int indexTo, int indexFrom){}
		public void removeCandidate(String id){SubtractionItem.this.unsubtract(id);}
		
	}

}
