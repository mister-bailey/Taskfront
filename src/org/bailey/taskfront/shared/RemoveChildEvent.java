package org.bailey.taskfront.shared;

public class RemoveChildEvent extends ModifyEvent {
	public String childUID;
	
	public RemoveChildEvent(String uid, String childUID){
		super(uid);
		this.childUID = childUID;
	}
	public RemoveChildEvent(){}
}
