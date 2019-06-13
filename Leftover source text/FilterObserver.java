	public class FilterObserver implements Item.Representative, Item.CandidateObserver /*, Parent*/ {
		final String itemID;
		public FilterObserver(/*ItemViewItem itemViewItem,*/ String itemID){
			//this.parent=itemViewItem;
			this.itemID=itemID;
			Item item = Database.getItem(itemID);
			item.addObserver(this);
		}
		public Item getItem(){
			return Database.getItem(itemID);
		}
		public void addLast(String id) {  // TODO get rid of superfluous methods?
			Item item = Database.getItem(itemID); 
			if(item.children != null)addCandidate(item.children.size(), id);
			else addCandidate(0,id);
		}
		public void addFirst(String id) {
			addCandidate(0,id);			
		}
		public void addCandidate(int index, String id){RepresentativeItem.this.filterAdd(index,id);}
		
		public void moveCandidate(int indexTo, String id){RepresentativeItem.this.filterMove(indexTo,id);}
		public void moveCandidate(int indexTo, int indexFrom){RepresentativeItem.this.filterMove(indexTo,indexFrom);}
		public void removeCandidate(String id){RepresentativeItem.this.filterRemove(id);}
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
		//public void clearList(){parent.filterClear();}  //  Clears only the list!! ?
		
		public CandidateObserver candidateObserver() {
			return this;
		}
		public void updateList() { // item.UIDlist is already filled
			RepresentativeItem.this.filterClear(); // Clears only the list
			Item item = Database.getItem(itemID); 
			if(item.children != null)for(String id : item.children) addLast(id);
		}
		public void removeList() { // probably don't do anything here.
		}
		public void updateChild(Item item) {RepresentativeItem.this.filterUpdateChild(item);}
		public void update() {RepresentativeItem.this.filterUpdate();}	
		public void deleteRepresentative() {RepresentativeItem.this.filterDelete();}
	}
