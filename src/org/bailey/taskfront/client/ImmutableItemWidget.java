package org.bailey.taskfront.client;

import org.bailey.taskfront.client.ItemWidget.ListType;

public class ImmutableItemWidget extends BareItemWidget {

	public ImmutableItemWidget(){super();}
	public ImmutableItemWidget(String itemID, boolean showText) {
		super();
		listType=ListType.BARE_LIST;
		basicInitialize(itemID, showText);
		immutableInitialize();
	}
	
	public ImmutableItemWidget(String itemID, boolean showText, ListType listType/*, WidgetType childWidgetType*/) {
		super();
		this.listType = listType;
		//this.childWidgetType = childWidgetType;
		basicInitialize(itemID, showText);
		immutableInitialize();
	}
	
	public void immutableInitialize(){
		this.setStyleName("gwt-BareItem");
	}

}
