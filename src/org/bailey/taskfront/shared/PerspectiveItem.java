package org.bailey.taskfront.shared;

import java.util.ArrayList;
import java.util.List;

import org.bailey.taskfront.client.Database;

public abstract class PerspectiveItem extends Item {
	
	public PerspectiveItem(){
		//UIDlist = new ArrayList<String>();
		this.selfSave=false; // when loading a perspective item from the database, set this to true.
	}
	public PerspectiveItem(String text){ // No saving here!
		initialize(text);
	}
	
	public void initialize(String text){
		uid = UID.getUID(this);
		//UIDlist = new ArrayList<String>();
		content.set("text", text);
		this.selfSave=false;		
	}

}
