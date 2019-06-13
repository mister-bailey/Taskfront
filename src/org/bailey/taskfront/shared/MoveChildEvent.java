package org.bailey.taskfront.shared;

// includes AddEvent
public class MoveChildEvent extends ModifyEvent {
	public String childUID;
	public int targetIndex;
	
	public MoveChildEvent(String uid, int targetIndex, String childUID){
		super(uid);
		this.childUID = childUID;
		this.targetIndex = targetIndex;
	}
	public MoveChildEvent(){}
}
