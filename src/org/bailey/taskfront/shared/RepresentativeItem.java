package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsObject;

// Perspective items which provide a view on a particular other item
// (contrast with unionitem, etc.)

public abstract class RepresentativeItem extends PerspectiveItem implements Representative {

	public List<String> references(){  // This looks weird.  In fact it returns a singleton list.
		if(hasObject()) return Arrays.asList(objectItemID);
		else return new ArrayList<String>();
	} // What is this????
	
	protected String objectItemID;
	
	public JsObject contentCache = JsObject.newJsObject();

	// There is a primary way of constructing a RepresentativeItem, which requires that it know who
	// it represents at the start, and this fact will be immutable.
	// It must then adopt a "recursive" UID extending the represented UID
	
	/*public RepresentativeItem(String text, String objectID){
		super();
		initialize(text,objectID);
	}*/
	
	public RepresentativeItem(String objectID){
		super();
		setNewWrapperUID(objectID);
		initializeRepresentation(objectID);
		isVolatile = true;
	}
	
	protected void setNewWrapperUID(String objectID){
		uid=UID.getUIDextension(this, objectID);
	}
	
	public RepresentativeItem(){
		super();
		isVolatile = true;
	}
	
	public void initializeRepresentation(String objectID){
		objectItemID = objectID;
		Database.getItem(objectID).addRepresentative(this);
		// ************* This should probably also populate the list, depending on loading settings? ********
	}
	
	protected String serializeContentCache(){return contentCache.JSON();}
	public void deserializeContentCache(String contentCacheString){contentCache = JsObject.eval(contentCacheString);}
	public void saveContentCache(){
		if(Database.storageMap != null) Database.localSaveItem(this);
		Database.queueServerEvent(new SetContentEvent(uid,serializeContent(),serializeContentCache()));
	}
	public SerializedComponents serialize(){
		return new SerializedComponents(uid,serializeContent(),serializeContentCache(),children);
	}
	
	
	// THESE are the methods that should update the UIDlist and call observers.
	// (The classical Item list operations should just propagate downward.)
		
	/* *********************** PARENT is not the right terminology here. DOCUMENT!!!
	 * Interpretations---
	 * 
	 * parent.add(index, childID) is a request to parent to add child to its list prior to the entry at index.
	 * parent.move(indexTo, indexFrom) is a request to parent to move the item *currently* at indexTo, to be
	 * 			immediately prior to the item which is *currently* at indexTo (so, in fact, it may end up
	 * 			at location indexTo-1), or to the end of the list if indexTo==UIDlist.size().
	 * 
	 * parent.filterAdd(index, childID) is an announcement to an observer that the observed object will, as soon
	 * 			as the call is over, perform an action to the effect of observed.add(index, childID).
	 * parent.filterMove(indexTo, indexFrom) is an announcement that observed will, after the call, perform
	 * 			observed.move(indexTo, indexFrom).
	 */
	
	
	protected /*abstract*/ final void filterAdd(int filterIndex, String itemID){};
	protected /*abstract*/ final void filterMove(int filterIndexTo, int filterIndexFrom){};
	protected /*abstract*/ final void filterMove(int filterIndexTo, String id){};
	protected /*abstract*/ final void filterRemove(String uid){};
	protected /*abstract*/ final void filterClear(){};
	protected /*abstract*/ final void filterUpdateChild(Item item){};

	/* Only do something here if you want the list heading to depend on the filter item
	 * heading, or something along those lines. */
	protected abstract void filterUpdate();
	
	// override if you want deletion of the filtered item to do something other than just
	// delete this item (such as reproduce a copy)
	public void deleteRepresentative(){this.delete();}
	
	//public abstract void updateChild(Item item); 
	
	public void deepSave(int maxDepth){ // deprecate, probably
		if(maxDepth > 0 && objectItemID != null){
			getObjectItem().deepSave(maxDepth-1);
		}
		save();
	}

	public boolean hasObject(){return objectItemID != null;}
	//public String getFilter(){return filter.itemID;}
	public Item getObjectItem(){return Database.getItem(objectItemID);}
	public void setFilter(String id){ // Does not populate list.  You must do this!
		objectItemID = id;//new FilterObserver(/*this,*/id);
		this.content.set("filterID",id);
	}
	
	public void deleteFromMemory(){
		super.deleteFromMemory();
		if(hasObject()) getObjectItem().representatives.remove(objectItemID);
	}
	
	public boolean removeCandidate(int index){
		removeCandidate(getObjectItem().children.get(index));
		return true;
	}

	
	public String getBaseItemID(){
		if(hasObject())return getObjectItem().getBaseItemID();
		else return uid;
	}
	
	public boolean isItem(String id){
		return uid.startsWith(id);
		/*if(id.equals(this.uid)) return true;
		if(hasObject()) return getObjectItem().isItem(id);
		return false;*/
	}
	
	// Returns the index of the first item in the UIDlist which "looks down on" baseID
	public int UIDindexOfBaseID(String baseID){
		ListIterator<String> iter = children.listIterator();
		while(iter.hasNext()){
			if(Database.getItem(iter.next()).isItem(baseID)) return iter.previousIndex();
		}
		return -1;		
	}
	// ***** For the time being, UIDindexOf will behave just as UIDlist.indexOf, and
	// ***** not call UIDindexOfBaseID, which must be called explicitly
	
	// may need overriden versions of these in Item
	public String moreConcreteID(){
		if(objectItemID != null) return objectItemID;
		else return this.uid;
	}
	public Item moreConcreteItem(){
		if(hasObject()) return getObjectItem();
		else return this;
	}
		
	// ----- Property setters and getters -----
	// By default, setters pass property down to Object. (Override properties don't.)
	// ---Eventually, have a deferred property passing mechanism, and also set cache immediately
	// Getters will
	// (1) check override (local) properties
	// (2) check cache of inherited properties
	// (3) ask Object for property (then update cache)
	
	@Override
	public void setProperty(String key, String value){
		getObjectItem().setProperty(key, value);
	}
	@Override
	public void setProperty(String key, boolean value){
		getObjectItem().setProperty(key, value);
	}
	@Override
	public void setProperty(String key, double value){
		getObjectItem().setProperty(key, value);
	}
	@Override
	public void deleteProperty(String key){
		getObjectItem().deleteProperty(key);
	}
	@Override
	public void setSubProperty(String [] keys, JsObject value){
		getObjectItem().setSubProperty(keys, value);
	}
	
	public void setOverrideProperty(String key, String value){
		super.setProperty(key, value);
	}
	public void setOverridProperty(String key, boolean value){
		super.setProperty(key, value);
	}
	public void setOverrideProperty(String key, double value){
		super.setProperty(key, value);
	}
	public void deleteOverrideProperty(String key){
		super.deleteProperty(key);
	}
	public void setOverrideSubProperty(String [] keys, JsObject value){
		super.setSubProperty(keys, value);
	}
	
	@Override
	public boolean hasProperty(String key){
		if(content.has(key)) return true;
		else if(contentCache.has(key)) return true;
		else return getObjectItem().hasProperty(key);
		// Doesn't update object cache ---- too dangerous, and maybe not necessary
	}
	@Override
	public String getStringProperty(String key){
		if(content.has(key)){
			return content.getString(key);
		} else if (contentCache.has(key)) {
			return contentCache.getString(key);
		} else{
			String value = getObjectItem().getStringProperty(key);
			contentCache.set(key, value);
			if(selfSave)saveContentCache();
			return value;
		}
	}
	@Override
	public double getDoubleProperty(String key){
		if(content.has(key)){
			return content.getDouble(key);
		} else if (contentCache.has(key)) {
			return contentCache.getDouble(key);
		} else{
			Double value = getObjectItem().getDoubleProperty(key);
			contentCache.set(key, value);
			if(selfSave)saveContentCache();
			return value;	
		}
	}
	@Override
	public boolean is(String key){
		if(content.has(key)){
			return content.getBoolean(key);
		} else if (contentCache.has(key)) {
			return contentCache.getBoolean(key);
		} else{
			Boolean value = getObjectItem().is(key);
			contentCache.set(key, value);
			if(selfSave)saveContentCache();
			return value;
		}
	}
	// getSubProperty ????
	
	// ---- Property-specific object updaters
	public void updateProperty(String key,String value){
		if(!content.has(key)){
			contentCache.set(key, value);
			for(Representative rep : representatives) rep.updateProperty(key, value);
			updateParents();
			if(selfSave) saveContent();
		}
	}
	public void updateProperty(String key,double value){
		if(!content.has(key)){
			contentCache.set(key, value);
			for(Representative rep : representatives) rep.updateProperty(key, value);
			updateParents();
			if(selfSave) saveContent();
		}
	}
	public void updateProperty(String key,boolean value){
		if(!content.has(key)){
			contentCache.set(key, value);
			for(Representative rep : representatives) rep.updateProperty(key, value);
			updateParents();
			if(selfSave) saveContent();
		}
	}
	public void updateSubProperty(String [] keys, JsObject value){
		if(!content.hasSubProperty(keys)){
			contentCache.setSubProperty(keys, value);
			for(Representative rep : representatives) rep.updateSubProperty(keys, value);
			updateParents();
			if(selfSave) saveContent();
		}
	}
	public void updateDeleteProperty(String key){
		if(!content.has(key)){
			contentCache.delete(key);
			for(Representative rep : representatives) rep.updateDeleteProperty(key);
			updateParents();
			if(selfSave) saveContent();
		}
	}

}

