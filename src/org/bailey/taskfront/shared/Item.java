package org.bailey.taskfront.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bailey.taskfront.client.AutocompleteOracle;
import org.bailey.taskfront.client.ItemWidget;
import org.bailey.taskfront.client.JsArrays;
import org.bailey.taskfront.client.JsObject;
import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.Database.DataItemBuilder;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Window;

public class Item implements Parent {
	public String uid;
	//public double lastChange;
	public boolean selfSave=true;
	public boolean isVolatile=false;
	public boolean garbageCollect=true;

	public ArrayList<String> children = new ArrayList<String>();
/*	public List<String> references(){
		if(UIDlist==null) return Arrays.asList();
		return UIDlist;
	}*/
	public JsObject content=JsObject.newJsObject();
	public String toString(){
		return getText();
	}
	public void onTextChange(String oldText,String newText){
		if(!selfSave)return; // only save to oracle if we are saving to database also
		if(oldText==null || oldText.equals("")){
			AutocompleteOracle.setText(newText,uid); 
		}else{
			AutocompleteOracle.changeText(oldText,newText,uid);
		}
	}
	// The empty constructor is for Items which will be filled in later (eg., from a server)
	public Item(){}
	
	/*public Item(String ID){
		this.uid = UID.
	}*/
	
	// Does not update observers---probably best
	public Item(String text){
		//System.out.println("Constructing new Item {text = '" + text + "'}");
		//content=JsObject.newJsObject();
		uid = UID.getUID(this);
		content.set("text", text);
		onTextChange(null,text);
	}
	public Item(String text, boolean selfSave){
		this.selfSave=selfSave;
		uid = UID.getUID(this);
		content.set("text", text);
		onTextChange(null,text);
	}
	
	public boolean contains(String uid){return directIndexOf(uid) < 0;}
	public boolean contains(Item item){return directIndexOf(item.uid) < 0;}
	public String getType(){
		if(content.has("type"))return content.getString("type");
		else return "Item";
	}
	
	// merges the data of x into this item
	// in this implementation, x data are preferred
	// does not initiate a save
	// not a deep copy clone!
	/*public void merge(Item x){
		//if (x.lastChange <= lastChange) return; // redundant?
		//lastChange=x.lastChange;
		content=x.content;
		// some unfortunate and goofy stuff with list merging
		if(x.UIDlist != null) {
			if(UIDlist==null)UIDlist=new ArrayList<String>();
			listMerge(x.UIDlist);
		}
		// if(list != null) list.updateObservers();
		updateObservers();
	}*/
	public void merge(SerializedComponents x){
		boolean oldSave = selfSave;
		selfSave=false;
		this.uid = x.uid;
		if(x.contentString != null) deserializeContent(x.contentString);
		if(x.list != null)listMerge(x.list);
		updateObservers();
		selfSave=oldSave;
		onTextChange(null,getText());		
	}
	
	public void listMerge(List<String> newList){  // This doesn't seem very sensible
		for(int n=0; n<newList.size();n++){
			String i = newList.get(n);
			int m = children.indexOf(i); // change?  is this only for base-level lists?
			if(m >= 0){
				if(m != n) this.moveChild(n, i);
			}
			else this.addChild(n, i);
		}
		while(children.size() > newList.size()) this.removeChild(newList.size());
	}
	
	protected String serializeContent(){return content.JSON();}
	public void deserializeContent(String contentString){content = JsObject.eval(contentString);}
	public SerializedComponents serialize(){
		return new SerializedComponents(uid,"Item",serializeContent(),children) ;
	}
	public Item deserialize(SerializedComponents x){ // Doesn't save in any case!
		boolean oldSave = selfSave;
		selfSave=false;
		this.uid = x.uid;
		if(x.contentString != null) deserializeContent(x.contentString);
		if(x.list != null)setUIDList(x.list);
		updateObservers();
		selfSave=oldSave;
		onTextChange(null,getText());
		return this;
	}
	// Doesn't update uid registry
	// Doesn't save!
	/*public static Item fromSerializedComponents(SerializedComponents c){
		JsObject content = JsObject.eval(c.contentString);
		Item x=null;
		if(content.has("type")){
			String type = content.getString("type");
			if(type.equals("VerbObjectItem")) x = new VerbObjectItem();
			else if(type.equals("Item")) x = new Item();
			else if(type.equals("TimeSpanItem")) x = new TimeSpanItem();
			else if(type.equals("HasPropertyItem")) x = new HasPropertyItem();
			else if(type.equals("IsPropertyItem")) x = new IsPropertyItem();
			else if(type.equals("NeededActionListItem")) x = new NeededActionListItem();
			
		} else x = new Item();
		x.selfSave=true;
		x.deserialize(c);
		return x;
	}*/
	{Database.builders.put("Item", new DataItemBuilder(){
			public Item buildItem(SerializedComponents components) {
				return (new Item()).deserialize(components);
			}
		});}
/*	public ArrayList<String> getUIDList(){
		if(list==null)return null;
		ArrayList<String> uidList = new ArrayList<String>();
		for(Item x : list){
			uidList.add(x.uid);
		}
		return uidList;
	}*/

	// Hoping I can leave this out:
	public JsArrayString getUIDJSArray(){
		return JsArrays.fromList(children);
	}
	private static native JsArrayString reinterpretCast(String[] value) /*-{ return value; }-*/;
	
	public void setUIDList(List<String> l){
		for(String itemID : l){
			this.add(itemID);
		}
	}
	public void setUIDArray(String [] l){
		for(String itemID : l){
			this.add(itemID);
		}	
	}

	
	public void save(){
		if(Database.storageMap != null) Database.localSaveItem(this);
		Database.queueServerEvent(new FullSaveEvent(serialize()));
	}
	public void saveContent(){
		if(Database.storageMap != null) Database.localSaveItem(this);
		Database.queueServerEvent(new SetContentEvent(uid,serializeContent()));
	}
	public void saveMoveChild(int i, String childID){
		if(Database.storageMap != null) Database.localSaveItem(this);
		Database.queueServerEvent(new MoveChildEvent(uid,i,childID));		
	}
	public void saveRemoveChild(String childID){
		if(Database.storageMap != null) Database.localSaveItem(this);
		Database.queueServerEvent(new RemoveChildEvent(uid,childID));				
	}
	// Does not remove from either database
	// called before you substitute something else into memory
	public void deleteFromMemory(){ 
		// now we make sure none of our children are minding us
		//if(UIDlist!=null){
			for(String childID : children){
				if(!UID.hasUID(childID)) continue;
				Item child = UID.getItem(childID);  //Database.getItem(childID);
				if(child==null) continue;
				child.removeParent(this);
				if(!child.selfSave) child.delete(); // Ephemeral objects in the hierarchy should also be deleted.
			}
		//}
	}
	public void delete(){
		isVolatile = false;
		// calling parent.removeChild will get rid of our direct child membership
		for(Parent parent : new ArrayList<Parent>(parents))parent.removeChild(uid);
		// now we kill all our representatives
		for(Representative obs : new ArrayList<Representative>(representatives))obs.deleteRepresentative();

		deleteFromMemory();
		
		AutocompleteOracle.removeText(getText());
		
		if(!selfSave)return;
		if(Database.storageMap != null) Database.storageMap.remove(uid);
		Database.queueServerEvent(new DeleteEvent(uid));			
	}
	public void deepSave(int maxDepth){
		if(maxDepth > 0 && children != null){
			for(String itemID : children){
				Database.getItem(itemID).deepSave(maxDepth-1);
			}
		}
		save();
	}
	
	public void setText(String text){
		String oldText = getText();
		setProperty("text",text);
		onTextChange(oldText,text);
	}	
	public String getText(){
		return getStringProperty("text");
	}
	public void setProperty(String key,String value){
		content.set(key, value);
		//updateObservers();
		for(Representative rep : representatives) rep.updateProperty(key, value);
		updateParents();
		if(selfSave) saveContent();
	}
	public void setProperty(String key,boolean value){
		content.set(key, value);
		//updateObservers();
		for(Representative rep : representatives) rep.updateProperty(key, value);
		updateParents();
		if(selfSave) saveContent();
	}
	public void setProperty(String key,double value){
		content.set(key,value);
		//updateObservers();
		for(Representative rep : representatives) rep.updateProperty(key, value);
		updateParents();
		if(selfSave) saveContent();
	}
	public void deleteProperty(String key){
		content.delete(key);
		//updateObservers();
		for(Representative rep : representatives) rep.updateDeleteProperty(key);
		updateParents();
		if(selfSave) saveContent();
	}
	public void setSubProperty(String [] keys, JsObject value){
		content.setSubProperty(keys,value);
		for(Representative rep :  representatives) rep.updateSubProperty(keys, value);
		updateParents();
		// updateObservers(); 
		if(selfSave) saveContent();
	}
	public void deleteSubProperty(String [] keys){
		content.deleteSubProperty(keys);
		updateObservers();
		if(selfSave) saveContent();
	}
	public boolean hasProperty(String key){
		return content.has(key);
	}
	public String getStringProperty(String key){
		return content.getString(key);
	}
	public double getDoubleProperty(String key){
		return content.getDouble(key);
	}
	// Maybe special methods like this are bad:
	public double getTime(){return getDoubleProperty("time");}
	public void setTime(double t){setProperty("time",t);}
	
	// returns true if property exists and has value true
	// returns false if property does not exist or has value false
	public boolean is(String property){
		if(!content.has(property))return false;
		return content.getBoolean(property);
	}
	// This is distinguished from boolean setProperty
	public void setIs(String property,boolean x){
		if(x){
			content.set(property,true);
		} else {
			content.delete(property);
		}
		updateObservers();
		if(selfSave) saveContent();
	}
	
	public double getPriority(){
		if(this.is("completed"))return -1000 * DT.yearLength; // TODO: really, should completed items show up at all?
		if(content.has("time"))return DT.todayStart-getTime();
		return -DT.yearLength;
	}
	public int compareTo(Item item){
		return 0;
	}

	
	/*private void removeList(){  // ***where is this used?***
		UIDlist=null;
		for(Observer obs : observers)obs.removeList();
	}*/
	
	// are observers being updated at the right times?
	public List<Representative> representatives=new ArrayList<Representative>();
	public List<Parent> parents=new ArrayList<Parent>();
	public void addRepresentative(Representative obs){representatives.add(obs);}
	public void removeRepresentative(Representative obs){representatives.remove(obs);}
	public void addParentObserver(Parent obs){if(!parents.contains(obs)) parents.add(obs);}
	public void removeParent(Parent obs){
		parents.remove(obs);
		if(isVolatile && parents.isEmpty()) delete();
	}
	public void updateObservers(){
		for(Representative obs : representatives)obs.update();
		updateParents();
	}
	public void updateParents(){
		for(Parent parent : parents)parent.updateChild(this);
	}
	// ************ Do I need this??? ***************
	public void updateListObservers(){ // complete refresh
		// Removed some nonsense about UIDlist==null
		for(Representative obs : representatives)obs.updateList();
	}
	
	// list operations
	// Any operation that does fine-grained manipulation of lists should update
	// list observers *before* doing its logical operations
	public void addChild(int index, String itemID){
		//if(UIDlist==null)UIDlist=new ArrayList<String>();
		for(Representative obs : representatives)obs.addCandidate(index,itemID);
		int indexFrom=directIndexOf(itemID);
		if(indexFrom >= 0) 	moveChild(index,indexFrom);
		else{
			children.add(index, itemID);
			if(selfSave) saveMoveChild(index,itemID);
			Database.getItem(itemID).addParentObserver(this);
		}
	}
	/*public void insert(int index, String itemID){
		this.addChild(index,itemID);
	}*/
	public void addChild(String itemID){ // stupid little method
		addChild(children.size(),itemID);
	}
	public boolean addAll(Collection<String> c){
		for(String itemID : c)this.addChild(children.size(),itemID);
		return true;
	}
	/*public void addFirst(String itemID){ // stupid little method
		this.addChild(0,itemID);
	}
	public void addLast(String itemID){ // stupid little method
		add(itemID);
	}*/
	/*public void addBefore(String beforeID, String itemID){
		int index = directIndexOf(beforeID);
		if(index >= 0){
			addChild(index,itemID);
			return;
		}
		addLast(itemID);
	}*/
	public String moveChild(int indexTo, int indexFrom){
		if(indexTo==indexFrom || indexTo==indexFrom+1) return children.get(indexFrom);
		// list existence and bound checking??
		for(Representative obs : representatives)obs.moveCandidate(indexTo, indexFrom);
		String itemID = children.remove(indexFrom);
		children.add(indexTo <= indexFrom ? indexTo : indexTo-1,itemID);
		if(selfSave) saveMoveChild(indexTo,itemID);
		return itemID;
	}
	public void moveChild(int indexTo, String itemID){
		moveChild(indexTo, directIndexOf(itemID));
	}
	/*public void clear(){ // not sure if I actually use/need this???
		if(UIDlist==null)return;
		while(UIDlist.size()>0) remove(0);
	}*/
	public void removeChild(int index){ // assumes index is in range
		//if(UIDlist==null)return; // therefore this is redundant???
		removeChild(children.get(index));
	}
	public void removeChild(String itemID){
		for(Representative obs : representatives)obs.removeCandidate(itemID);
		children.remove(itemID);

		// Update Parents?  Only if they care how many item this list has
		// for(Parent obs : parentObservers)obs.
		Database.getItem(itemID).removeParent(this);
		if(selfSave) saveRemoveChild(itemID);
	}
	/*public void removeFirst(){ // TODO get rid of this junk??
		remove(0);
	}
	public void removeLast(){
		remove(children.size()-1);
	}*/
	
	// For perspective items, forces the item to be removed from the source, instead of changing property
	public void hardRemove(String itemID){
		removeChild(itemID);
	}
	public void hardRemove(int index){
		removeChild(index);
	}

	public boolean isEmpty(){
		return /*(UIDlist==null) ||*/ children.isEmpty();
	}
	
	public void updateChild(Item item) {
		for(Representative rep : representatives){
			//if(obs instanceof Promoter)((Promoter)obs)
			rep.updateCandidate(this,item);
		}
	}
	
	public void demoteChild(String uid, int magnitude){
		if(magnitude<=0) return; // really?
		if(hasProperty("demotionTarget")){
			Item i = Database.getItem(getStringProperty("demotionTarget"));
			removeChild(uid);
			i.receiveDemotion(uid,magnitude-1);
		}
	}
	public void receiveDemotion(String uid,int magnitude){
		if(magnitude>0 && hasProperty("demotionTarget")){
			Item i = Database.getItem(getStringProperty("demotionTarget"));
			removeChild(uid);
			i.receiveDemotion(uid,magnitude-1);
		} else addChild(uid);
	}
	
	// Doesn't directly save!
	public Item copy(){
		/*Item x = new Item();
		x.copyFrom(this);
		return x;*/
		SerializedComponents c = this.serialize();
		c.uid = UID.getUID();
		Item x = fromSerializedComponents(c);
		UID.putItem(x.uid,x);
		x.selfSave = this.selfSave;
		return x;
	}
	
	// clones, and adds to parentID, and any parentObservers
	// if parentID is null and no parentObservers, as a last resort adds to mainList
	// doesn't directly save!
	public Item copyAndAdd(String parentID){
		Item x = this.copy();
		boolean addedToParent = false;
		Item parent=null;
		if(parentID != null){
			parent = Database.getItem(parentID);
			parent.add(x.uid);
			addedToParent=true;
		}
		for(Parent obs : parents){
			if(obs instanceof Item && obs != parent){
				((Item)obs).add(x.uid);
				addedToParent=true;
			}
		}
		// as a last resort, adds to mainList
		if(!addedToParent) Database.getItem("mainList").add(x.uid);
		return x;
	}
	
	public void complete(boolean c){
		if(c && !this.is("completed")) this.complete();
		else if (!c && this.is("completed")){
			content.delete("completionTime");
			setIs("completed",false);			
		}
	}
	
	public void complete(){
		double now = (new Date()).getTime();
		if(hasProperty("repeatPeriod")){
			Item x = copyAndAdd(getStringProperty("repeatParent"));
			x.setTime(now + this.getDoubleProperty("repeatPeriod"));
		}
		
		// TODO: is this necessary?
		// if(hasProperty("manager")) Database.getItem(getString("manager")).notifyComplete(this);
		
		content.set("completionTime",now);
		setIs("completed",true); //	updates and saves
	}
	
	// doesn't generate the action!
	// frequency <= 0 means not repeating
	public void setNeededAction(String verb, String repeatParent, double frequency, String candidate){
		if(!hasProperty("neededActions")) content.set("neededActions", JsObject.newJsObject());
		JsObject actions = content.getJsObject("neededActions"); 
		JsObject action;
		if(actions.has(verb)) action = actions.getJsObject(verb);
		else actions.set(verb, action = JsObject.newJsObject());
		
		if(repeatParent != null) action.set("repeatParent",repeatParent);
		else action.delete("repeatParent"); // Really???
		
		if(frequency > 0) action.set("frequency",frequency);
		else action.delete("frequency");
		
		if(candidate != null) action.set("candidate",candidate);
		else action.delete("candidate");
		
		updateObservers();
		if(selfSave) saveContent();
	}
	public void setNeededActionAndEnsureCandidate(String verb, String repeatParent, double frequency){
		if(!hasProperty("neededActions")) content.set("neededActions", JsObject.newJsObject());
		JsObject actions = content.getJsObject("neededActions"); 
		JsObject action;
		if(actions.has(verb)) action = actions.getJsObject(verb);
		else actions.set(verb, action = JsObject.newJsObject());
		
		if(repeatParent != null) action.set("repeatParent",repeatParent);
		else action.delete("repeatParent"); // Really???
		
		if(frequency > 0) action.set("frequency",frequency);
		else action.delete("frequency");
		
		VerbObjectItem candidate;
		double now = (new Date()).getTime();
		if(frequency > 0 && action.has("candidate") && !(candidate = (VerbObjectItem) Database.getItem(action.getString("candidate"))).is("completed")){
			if(candidate.getTime() - now > frequency) candidate.setTime(now + frequency);
			if(repeatParent != null && !action.getString("repeatParent").equals(repeatParent)){ // TODO: if parent==null, remove it???
				action.set("repeatParent", repeatParent);
				Item parentItem = Database.getItem(repeatParent);
				if(!parentItem.children.contains(candidate.uid)) parentItem.add(candidate.uid);
			}
		} else {
			candidate = VerbObjectItem.createVerbObjectItem(verb,this.uid);
			if(frequency > 0) candidate.setTime(now + frequency);
			else if(frequency < 0) candidate.setTime(now - frequency); // negative frequency understood as time remaining for nonrepeating item
			action.set("candidate",candidate.uid);
			if(repeatParent != null) { // TODO: if parent==null, remove it???
				action.set("repeatParent", repeatParent);
				Database.getItem(repeatParent).add(candidate.uid);
			} else {
				Database.getItem("mainList").add(candidate.uid);
			}
		}
		
		updateObservers();
		if(selfSave) saveContent();		
	}
	public void removeNeededAction(String verb){
		if(hasProperty("neededActions")){
			JsObject actions = content.getJsObject("neededActions"); 
			actions.delete(verb);
			if(actions.isEmpty()) content.delete("neededActions");
		}
		updateObservers();
		if(selfSave) saveContent();
	}
	// Probably not to be used:
	public VerbObjectItem setNeededActionAndCreate(String verb, String parent, double frequency, double time){
		VerbObjectItem x = VerbObjectItem.createVerbObjectItem(verb, uid);
		if(time>0) x.setTime(time);
		else if(frequency > 0) x.setTime((new Date()).getTime()+frequency);
		setNeededAction(verb,parent,frequency,x.uid);
		if(parent != null) Database.getItem(parent).add(x.uid);
		return x;
	}
	
	public boolean hasNeededAction(String verb){
		return content.has("neededActions") && content.getJsObject("neededActions").has(verb);
	}
	public JsObject getNeededAction(String verb){
		if(!hasNeededAction(verb)) return null;
		return content.getJsObject("neededActions").getJsObject(verb);
	}
	
	// called by an item when it is completed and wants to notify us
	public void notifyComplete(Item item){
		
	}
	
	public Item createFollowUp(String parentID){
		Item f = copyAndAdd(parentID);
		f.setTime((new Date()).getTime()+f.getTimeScale());
		return f;
	}
	
	public double getTimeScale(){
		if(hasProperty("timeScale")) return content.getDouble("timeScale");
		if(hasProperty("time") && hasProperty("creationTime")) return content.getDouble("time") - content.getDouble("creationTime");
		return DT.dayLength * 2;
	}
	
	public void setTimeScale(double timeScale){
		setProperty("timeScale",timeScale);
	}
	
	public ItemWidget getWidget(){
		for(Representative obs : representatives) if (obs instanceof ItemWidget) return (ItemWidget) obs;
		return null;
	}
	
	public String getBaseItemID(){
		return uid;
	}
	
	public static boolean sameBaseItem(String ID1, String ID2){
		return Database.getItem(ID1).getBaseItemID().equals(Database.getItem(ID2).getBaseItemID());
	}
	
	// maybe I shouldn't just return true/false, but some information about *how* one is the other,
	// which would be null in the false case?
	// returns true if this is an instance of, or view on, "id".  (not symmetric)
	public boolean isItem(String id){
		return this.uid.equals(id);
	}
	public boolean isItem(Item item){
		if(item != null) return isItem(item.uid);
		return false;
	}
	
	// symmetric version.  returns true if these are both instances of, or views on, the same item
	public boolean isSameItem(String id){
		return Database.getItem(id).getBaseItemID().equals(this.getBaseItemID());
	}
	
/*	public int directIndexOf(Item item){
		return directIndexOf(item.uid);
	}*/
	public int directIndexOf(String id){
		return children.indexOf(id);
	}
	// When should this search fall through to base items???
	public int childIndexOf(String id){
		return directIndexOf(id);
	}
	// Checks to see if any of the elements of this list represent itemID
	public int repIndexOf(String itemID){
		for(int i=0; i < children.size(); i++){
			if(children.get(i).startsWith(itemID)) return i;
		}
		return -1;
	}
	
	public String getDefaultView(){
		if(content.has("defaultView")){
			String suffix = content.getString("defaultView").trim();
			if(!suffix.startsWith("/")) suffix = "/" + suffix;
			return suffix;
		} else return "";
	}
	
	/*private static final CandidateObserver dummyCandidateObserver = new CandidateObserver(){
		public void updateList() {}
		//public void addLast(String id) {}
		//public void addFirst(String id) {}
		public void addCandidate(int index, String id) {}
		public void moveCandidate(int indexTo, String id) {}
		public void moveCandidate(int indexTo, int indexFrom) {}
		public void removeCandidate(String id) {}
		public boolean removeCandidate(int index) {return false;}
		//public void removeFirst() {}
		//public void removeLast() {}
		//public void clearList() {}			
	};
	public static final CandidateObserver dummyCandidateObserver(){return dummyCandidateObserver;}
*/
	
}

