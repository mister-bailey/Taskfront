package org.bailey.taskfront.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;

// TODO: parentID is currently not used!
public class ActionDelete extends DelayedAction {
	String itemID;
	String parentID;
	public ActionDelete(String itemID, String parentID){
		super("Delete","LightCoral");
		this.itemID=itemID;
		this.parentID=parentID;
	}

	@Override
	public void execute() {
		Database.getItem(itemID).delete(); // !!!!!!!!!!!
	}
	
	public static class DeleteButton extends ActionButton {
		public DeleteButton(){
			super("X");
			this.addStyleDependentName("delete");
		}
		void actOn(ItemWidget w) {w.actionDelete(-1);}
	}

}
