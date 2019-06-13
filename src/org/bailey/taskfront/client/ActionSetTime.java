package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.DT;

public class ActionSetTime extends ProgressBarOverlay {
	public double time;
	public String itemID;

	public ActionSetTime(String itemID, double time){
		super();
		this.time=time;
		this.itemID=itemID;
	}
	
	void onComplete() {
		Database.getItem(itemID).setTime(time);
	}
}
