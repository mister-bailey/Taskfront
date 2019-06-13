package org.bailey.taskfront.client;

public class DayItemWidget extends ImmutableItemWidget {

	protected DayItemWidget(){super();}
	
	public DayItemWidget(String itemID){
		super(itemID,true,ListType.SCROLL_LIST);
		//this.setHeight("100%");
	}
	
}
