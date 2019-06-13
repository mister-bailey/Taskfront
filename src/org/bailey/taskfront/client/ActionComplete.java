package org.bailey.taskfront.client;

import java.util.Date;

import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.Item;

/*
 * Some issues:
 * I want completed items to show up crossed out sometimes,
 * and other times go away entirely (either because they're excluded by a filter,
 * or they're moved elsewhere.
 */
public class ActionComplete extends ProgressBarOverlay {
	String itemID;
	//String parentID;
	public ActionComplete(String itemID/*,String parentID*/){
		super();
		setProgressColor("LightGreen");
		this.itemID=itemID;
		//this.parentID=parentID;
	}
	
	@Override
	public void onComplete() {
		Item item = Database.getItem(itemID);
		/*Item parent = Database.getItem(parentID);
		String targetID = parent.completionTarget();
		if(targetID != null){
			parent.hardRemove(itemID);
			Database.getItem(targetID).add(itemID);
		}*/
		if(!item.is("completed")){
			item.complete(true);
		} else {
			item.complete(false);
		}
	}
}
