package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.Database;

public class ExposedSubItem extends ModifiedItem {
	
	// For the time being, I will operate under the assumption that
	// these items are not saved, and are in fact volatile.
	// If I'm caching perspective lists, I will have to change that.

	public ExposedSubItem(){
		super();
	}
		
	public ExposedSubItem(String parentItemID, String subitemID){
		super(Database.getItem(parentItemID).toString(),subitemID);
		content.set("type","ExposedSubItem");
		content.set("parentItemID",parentItemID);
	}
	
	
}
