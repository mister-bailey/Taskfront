package org.bailey.taskfront.client;

public class HeaderItemWidget extends BareItemWidget {
	
	public HeaderItemWidget(){
		super();
	}
	
	public HeaderItemWidget(String uid){
		super(uid,true);
		this.setStyleName("headerItem");
	}

}
