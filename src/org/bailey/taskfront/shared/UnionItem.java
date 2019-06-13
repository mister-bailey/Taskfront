package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.List;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsArrays;
import org.bailey.taskfront.client.JsObject;

import com.google.gwt.core.client.JsArrayString;

public class UnionItem extends PerspectiveItem {
	
	List<UnionBin> bins;
	
	public UnionItem(List<String> binIDs, String text){
		uid = UID.getUID(this);
		setText(text);
		children = new ArrayList<String>();
		setBins(binIDs);
		populateUIDlist();
		save();
	}
	public void setBins(List<String> binIDs){
		//content.set("bins",JsArrays.fromList(binIDs));
		bins = new ArrayList<UnionBin>();
		for(String binID : binIDs){
			bins.add(new UnionBin(this,binID));
		}
	}
	public ArrayList<String> getBins(){
		ArrayList<String> result = new ArrayList<String>();
		for(UnionBin bin : bins)result.add(bin.itemID);
		return result;
	}
	public SerializedComponents serialize(){
		return new SerializedComponents(uid,content.JSON(),getBins());
	}
	public void addBin(String uid){
		if(bins==null) bins = new ArrayList<UnionBin>();
		bins.add(new UnionBin(this,uid));
		populateUIDlist();
	}
	public void removeBin(String uid){
		for(UnionBin bin : bins){
			if(bin.itemID.equals(uid)){
				removeBin(bin);
				break;
			}
		}
	}
	public void removeBin(UnionBin bin){
		bins.remove(bin);
		populateUIDlist();
	}
	public void deserialize(SerializedComponents c){
		this.uid = c.uid;
		//if(c.contentString == null) return; // raise an error?  generate from scratch?
		this.content = JsObject.eval(c.contentString);
		//loadViewBinsFromContent();
		setBins(c.list);
		populateUIDlist();
		updateObservers();
	}
	private int getAbsoluteIndex(UnionBin bin){
		int index=0;
		for(UnionBin b : bins){
			if(b==bin)break;
			index += b.size();
		}
		return index;
	}
	public void populateUIDlist(UnionBin bin){ // does not act on observers
		int a = getAbsoluteIndex(bin), s = bin.size();
		Item item = Database.getItem(bin.itemID);
		for(int i=0;i<s;i++)children.add(a+i,item.children.get(i));
		//for(Observer obs : observers)obs.listObserver().updateList();
	}
	public void populateUIDlist(){
		children = new ArrayList<String>();
		for(UnionBin bin : bins) populateUIDlist(bin);	
	}

	
	// THESE are the methods that should update the UIDlist and call observers.
	// (The classical Item list operations should just propagate downward.)
	protected void binAdd(UnionBin bin, int index, String uid){
		if(children==null)children = new ArrayList<String>();
		index = index + getAbsoluteIndex(bin);
		for(Representative obs : representatives)obs.candidateObserver().addCandidate(index,uid);
		children.add(index,uid);
	}
	protected void binMove(UnionBin bin, int indexTo, int indexFrom){
		int absIndex = getAbsoluteIndex(bin);
		for(Representative obs : representatives)obs.candidateObserver().moveCandidate(indexTo + absIndex, indexFrom + absIndex);
		String itemID = children.remove(indexFrom + absIndex);
		children.add(indexTo + absIndex,itemID);
	}
	protected void binRemove(UnionBin bin, String uid){
/*		if(UIDlist.size()==1){
			for(Observer obs : observers)obs.removeList();
			UIDlist=null;
		} else{*/
		for(Representative obs : representatives)obs.candidateObserver().removeCandidate(uid);
		children.remove(uid);
		//}
	}
	protected void binClear(UnionBin bin){
		int n = bin.size(), a = getAbsoluteIndex(bin);
		for(int i=a;i<a+n;i++){
			for(Representative obs : representatives)obs.candidateObserver().removeCandidate(i);
			children.remove(i);
		}
	}
	protected void binUpdateList(UnionBin bin){
		int n = bin.size(), a = getAbsoluteIndex(bin);
		for(int i=a;i<a+n;i++){
			children.remove(i);
		}
		populateUIDlist(bin);
		for(Representative obs : representatives)obs.candidateObserver().updateList();
	}

	public class UnionBin implements Representative, CandidateObserver, Parent {
		UnionItem parent;
		String itemID;
		public UnionBin(UnionItem parent, String itemID){
			this.parent=parent;
			this.itemID=itemID;
			Item item = Database.getItem(itemID);
			item.addRepresentative(this);
			if(item.children==null)return;
			for(String uid : item.children)Database.getItem(uid).parents.add(this);			
		}
		public int size(){
			Item item = Database.getItem(itemID);
			if(item.children==null)return 0;
			return item.children.size();
		}
		public void addLast(String id) {  // TODO get rid of superfluous methods?
			Item item = Database.getItem(itemID); 
			if(item.children != null)addCandidate(item.children.size(), id);
		}
		public void addFirst(String id) {
			addCandidate(0,id);			
		}
		public void addCandidate(int index, String id){parent.binAdd(this,index,id);}
		
		public void moveCandidate(int indexTo, String id) {
			Item item = Database.getItem(itemID); 
			if(item.children != null){
				int indexFrom = item.children.indexOf(id);
				if(indexFrom >= 0) moveCandidate(indexTo, indexFrom);
			}
		}
		public void moveCandidate(int indexTo, int indexFrom){parent.binMove(this,indexTo,indexFrom);}
		public void removeCandidate(String id){parent.binRemove(this,id);}
		public boolean removeCandidate(int index){
			removeCandidate(Database.getItem(itemID).children.get(index));
			return true;
		}
		public void removeFirst() { // TODO get rid of stupid little methods like this?
			removeCandidate(0);			
		}
		public void removeLast() {
			Item item = Database.getItem(itemID); 
			if(item.children != null)removeCandidate(item.children.size()-1);			
		}
		public void clearList(){parent.binClear(this);}  //  Clears only the list!! ?
		
		public CandidateObserver candidateObserver() {
			return this;
		}
		public void updateList() { // item.UIDlist is already filled
			parent.binUpdateList(this);
			/*clearList();
			Item item = Database.getItem(itemID); 
			if(item.UIDlist != null)for(String id : item.UIDlist) addLast(id);*/
		}
		public void removeList() { // probably don't do anything here.
		}
		public void updateChild(Item item) {parent.updateChild(item);}
		public void update() {}//parent.binUpdate(this);}
		public void deleteRepresentative() {parent.removeBin(this);}
	}

	public void hardRemove(String itemID) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateChild(Item item) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addChild(int indexTo, String itemID) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String moveChild(int indexTo, int indexFrom) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void removeCandidate(String itemID) {
		// TODO Auto-generated method stub
		
	}
	
	// I need to write the add/move/remove methods for this class!

}
