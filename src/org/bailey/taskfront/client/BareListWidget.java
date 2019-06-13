package org.bailey.taskfront.client;

import org.bailey.taskfront.client.ItemListWidget.ListVerticalPanel;

// A regular list widget, except without the collapsing list edge.

public class BareListWidget extends ItemListWidget {
	
	public BareListWidget(String uid){
		super();
		initialize(uid);
	}

	public BareListWidget() {
		super();
	}

	public void initialize(String uid){
		this.add(listPanel = new ListVerticalPanel());
		listPanel.setStylePrimaryName("gwt-ListWidget");
		parentItemID=uid;
		fill();
	}
}
