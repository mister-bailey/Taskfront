package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.OrderPreservingItem;
import org.bailey.taskfront.shared.Item;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

// exists only in client mode.
/* callbacks are called as deferred commands, so that client
 * can do "hard, upstream" modifications without worrying about
 * concurrent modification of observer lists.
*/

public class ListenerItem extends OrderPreservingItem {
	ItemEventHandler handler;
	public interface ItemEventHandler {
		public void onUpdate();
		public void onAdd(String itemID);
		public void onMove(int indexTo, int indexFrom);
		public void onRemove(String itemID);
	}
	private class OnUpdateCommand implements ScheduledCommand {
		public void execute() {ListenerItem.this.handler.onUpdate();}
	}
	private class OnAddCommand implements ScheduledCommand {
		public String itemID;
		public OnAddCommand(String itemID){this.itemID=itemID;}
		public void execute(){ListenerItem.this.handler.onAdd(itemID);}
	}
	private class OnMoveCommand implements ScheduledCommand {
		public int indexTo,indexFrom;
		public OnMoveCommand(int indexTo, int indexFrom){
			this.indexTo = indexTo;
			this.indexFrom = indexFrom;
		}
		public void execute(){ListenerItem.this.handler.onMove(indexTo,indexFrom);}
	}
	private class OnRemoveCommand implements ScheduledCommand {
		public String itemID;
		public OnRemoveCommand(String itemID){this.itemID=itemID;}
		public void execute(){ListenerItem.this.handler.onRemove(itemID);}
	}
	static Scheduler scheduler = Scheduler.get();
	
	public ListenerItem(String filterID, ItemEventHandler handler){
		super("",filterID);
		selfSave=false;
		this.handler=handler;
	}
	
	public Item getItem(){
		return Database.getItem(getFilter());
	}

	//
	protected void filterAdd(int index, String uid) {scheduler.scheduleDeferred(new OnAddCommand(uid));}
	protected void filterMove(int indexTo, int indexFrom) {scheduler.scheduleDeferred(new OnMoveCommand(indexTo,indexFrom));}
	protected void filterRemove(String uid) {scheduler.scheduleDeferred(new OnRemoveCommand(uid));}
	protected void filterUpdate(){scheduler.scheduleDeferred(new OnUpdateCommand());}
	public void updateChild(Item item) {
		// Do nothing???
	}

	// Do nothing:
	public boolean include(Item item) {return true;}
	public void addChild(int index, String itemID) {}
	public String moveChild(int indexTo, int indexFrom) {return null;}
	public void remove(int index) {}
	public void setInclude(Item item, boolean include) {}

	public double getStartOrder() {
		return 0;
	}

	public double getEndOrder() {
		return 0;
	}

	public void setChildOrder(Item item, double order) {
	}

}
