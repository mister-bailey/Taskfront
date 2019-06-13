package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.List;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsObject;

public class HasPropertyItem extends OrderPreservingItem {
	private String [] subProperty;
	
	// Consider implementing these as direct variables???  For speed?
	//private String property;
	//private boolean has;
	protected String property(){return getStringProperty("property");}
	protected boolean has(){return content.getBoolean("has");}
	
	public HasPropertyItem(String text, String filterID, String property, boolean has){
		super(text,filterID);
		content.set("type","HasPropertyItem");
		setCriterionProperty(property);
		setHas(has);
		populateUIDlist();
	}
	public HasPropertyItem(){super();}
	
	private void setCriterionProperty(String p){ // no observer updates
		//property=p;
		content.set("property",p);
		if(p.contains(".")) subProperty = p.split("\\.");
		else subProperty=null;
	}
	private void setHas(boolean h){
		//has=h;
		content.set("has",h);
	}
	public void deserializeContent(String contentString){
		super.deserializeContent(contentString);
		String p = content.getString("property"); 
		if(p.contains(".")) subProperty = p.split("."); 
	}
	
	// Only needed if I have to get special member fields out of the content
/*	public void deserialize(SerializedComponents c){
		this.uid = c.uid;
		if(c.contentString == null) return; // raise an error?  generate from scratch?
		this.content = JsObject.eval(c.contentString);
		setFilter(this.getString("filterID"));
		setCriterionProperty(this.getString("property"));
		setHas(this.getBoolean("has"));
		populateUIDlist();
		updateObservers();
	}*/

/*	protected void filterAdd(int filterIndex, String uid) {
		Item item = Database.getItem(uid);
		if(!include(item))return;
		int index = UIDlist.size(); // alternatively, I could start index at max(filterIndex,UIDlist.size())
		List<String> filterList = Database.getItem(getFilter()).UIDlist;
		//int indexOf = filterList.indexOf(uid);  // THIS SHOULD EQUAL 
		while(index>0 && filterIndex<filterList.indexOf(UIDlist.get(index-1))) index--;
		for(Observer obs : observers)obs.listObserver().add(index,uid);
		UIDlist.add(index,uid);
	}*/

/*	@Override
	protected void filterMove(int indexTo, int indexFrom) {  // TODO sort this method out
		List<String> filterList = Database.getItem(getFilter()).UIDlist;
		Item item = Database.getItem(filterList.get(indexFrom));
		int thisIndexFrom = UIDlist.indexOf(item.uid);
		if(thisIndexFrom == -1)return;
		
		int size = UIDlist.size()-1;  // Do we remove the entry first, and if not, count the index right!
		if(size==0)return;
		
		int thisIndexTo = thisIndexFrom;
		UIDlist.remove(thisIndexFrom);  // From here.  (Do the right comparison, add thises to indices
		if(thisIndexTo == 0 || indexTo > filterList.indexOf(UIDlist.get(thisIndexTo))){
			while(thisIndexTo<size && indexTo > filterList.indexOf(UIDlist.get(thisIndexTo))) thisIndexTo++;
		} else {
			while(thisIndexTo>0 && indexTo < filterList.indexOf(UIDlist.get(thisIndexTo))) thisIndexTo--;
		}
		UIDlist.add(thisIndexTo,item.uid); // TODO: before or after observer update?
		if(indexTo>indexFrom)indexTo++;
		for(Observer obs : observers){
            obs.listObserver().move(thisIndexTo,thisIndexFrom);
            //if(obs instanceof ParentObserver) ((ParentObserver)obs).updateChild(item);
        }
	}*/

	@Override
	public boolean include(Item item) {
		if (subProperty==null) return(has()==item.content.has(property()));
		else return(has()==item.content.hasSubProperty(subProperty));
	}
	public void setInclude(Item item,boolean include){
		if (subProperty==null){
			if(has()==include) item.setProperty(property(),null);
			else item.deleteProperty(property());
		} else {
			if(has()==include) item.setSubProperty(subProperty,null);
			else item.deleteSubProperty(subProperty);
		}
	}

	protected void filterUpdate() {
		// TODO: if this item's content.properties depend on filterItems, update
	}
	public double getStartOrder() {
		return 0;
	}
	public double getEndOrder() {
		return 0;
	}
	public void setChildOrder(Item item, double order) {
	}

	/*public HasPropertyItem copy() {
		HasPropertyItem item = new HasPropertyItem();
		
		return null;
	}*/

}
