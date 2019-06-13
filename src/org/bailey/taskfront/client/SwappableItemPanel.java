package org.bailey.taskfront.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.History;

public class SwappableItemPanel extends MainPanel {
	ItemSelectBox selectBox = new ItemSelectBox(){
		protected void onValueChange(){
			SwappableItemPanel.this.changeItem(this.getUID());
		}
	};

	public SwappableItemPanel(){
		super();
	}
	
	public SwappableItemPanel(String itemID){
		this();
		this.add(selectBox);
		selectBox.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
		changeItem(itemID);
	}
	
	public void setItem(String itemID){
		this.itemID = itemID;
		selectBox.setUID(itemID);
		if(this.getWidgetCount()>1)this.remove(1);
		if(itemID != null) {
			ItemWidget itemWidget = new ImmutableItemWidget(itemID,false);
			this.add(itemWidget);
			itemWidget.setWidth("100%");
		}		
	}
	
	public void changeItem(String itemID){
		setItem(itemID);
		History.newItem(getGlobalToken(),false);
	}
	
	public String getToken(){
		return itemID + "/Swappable";
	}
}
