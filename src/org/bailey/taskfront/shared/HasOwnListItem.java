package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.JsObject;

public abstract class HasOwnListItem extends RepresentativeItem {
	
	public HasOwnListItem(){super();}
	public HasOwnListItem(String text, String objectID){
		super(text,objectID);
	}
	
	public void setFilterAndUpdate(String filterID){
		setFilter(filterID);
		updateObservers();
		if(selfSave) this.save();
	}
	protected void filterUpdate() {
		updateObservers();
	}
	protected void filterAdd(int filterIndex, String itemID) {}
	protected void filterMove(int filterIndexTo, int filterIndexFrom) {}
	protected void filterRemove(String uid) {}
	protected void filterClear() {}
	protected void filterUpdateChild(Item item) {}
	
	public void deserialize(SerializedComponents x){
		boolean oldSave = selfSave;
		selfSave=false;
		this.uid = x.uid;
		if(x.contentString != null) this.content = JsObject.eval(x.contentString);
		if(x.list != null)setUIDList(x.list);
		setFilter(this.getStringProperty("filterID"));
		updateObservers();
		selfSave=oldSave;
		onTextChange(null,getText());
	}


}
