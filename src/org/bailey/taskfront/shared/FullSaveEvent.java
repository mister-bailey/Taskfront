package org.bailey.taskfront.shared;

import java.util.List;

public class FullSaveEvent extends ModifyEvent {
	public String type;
	public String contentString;
	public String contentCacheString;
	public List<String> uidList;
	public FullSaveEvent(SerializedComponents c){
		super(c.uid);
		type = c.type;
		contentString=c.contentString;
		contentCacheString= c.contentCacheString;
		uidList=c.list;
	}
	/*public FullSaveEvent(Item x){
		super(x.uid);
		contentString=x.serializeContent();
		uidList=(List<String>) (x.UIDlist != null ? x.UIDlist.clone() : null);
	}*/
	public FullSaveEvent(){}

}
