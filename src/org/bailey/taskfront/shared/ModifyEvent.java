package org.bailey.taskfront.shared;

import org.bailey.taskfront.shared.DatabaseEvent;


public abstract class ModifyEvent extends DatabaseEvent {
	public ModifyEvent(String uid){
		super(uid);
	}
	public ModifyEvent(){}
}
