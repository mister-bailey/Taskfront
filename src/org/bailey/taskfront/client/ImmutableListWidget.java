package org.bailey.taskfront.client;

public class ImmutableListWidget extends ItemListWidget {
	
	public ImmutableListWidget(String uid){
		super();
		this.childWidgetType = WidgetType.BARE_ITEM; 
		initialize(uid);
	}
	
}
