package org.bailey.taskfront.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasFocus;

public abstract class EditPanel extends FlowPanel /*implements Focusable*/ {
	public String itemID;
	
	public EditPanel(String itemID){
		super();
		this.itemID=itemID;
	}
	
	public abstract void update();
	public abstract void save();
	
	public abstract void setFocus();

}
