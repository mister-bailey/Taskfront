package org.bailey.taskfront.shared;

//includes item creation...
public class SetContentEvent extends ModifyEvent{
	public String contentString;  // "small" serialization, only of "content", not child list
	public String contentCacheString; // serialization of "cached content" for RepItems

	public SetContentEvent(String u, String c){
		super(u);
		this.contentString=c;
		this.contentCacheString=null;
	}
	public SetContentEvent(String uid, String content, String contentCache){
		super(uid);
		this.contentString=content;
		this.contentCacheString=contentCache;
	}
	public SetContentEvent(){}
}
