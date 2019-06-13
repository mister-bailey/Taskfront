package org.bailey.taskfront.client;

import org.bailey.taskfront.client.ItemListWidget.WidgetType;

// Intended for a "top level" list of lists, which shouldn't be edited itself.
// Without collapsing list edge
public class HeaderListWidget extends BareListWidget {
	
	public HeaderListWidget(String uid){
		super();
		this.childWidgetType = WidgetType.HEADER_ITEM;
		initialize(uid);
	}

}
