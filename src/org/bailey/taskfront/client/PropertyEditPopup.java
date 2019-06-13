package org.bailey.taskfront.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PropertyEditPopup extends ItemEditPopup {

	public static ItemEditPopup newPopup(EditPanel ep){
		if(currentPopup != null) /*((HasWidgets)*/ currentPopup.hide();//getParent()).remove(currentPane);
		return currentPopup = new PropertyEditPopup(ep);
	}
	public static void showNew(EditPanel ep, int x, int y){
		newPopup(ep);
		currentPopup.setPopupPosition(x,y);
		currentPopup.show();
		//currentPane.center();
	}

	protected PropertyEditPopup(EditPanel ep){
		super();
		this.itemID=ep.itemID;
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(this.editPanel = ep);
		
		Button cancelButton = new Button("Cancel", new ClickHandler(){
			public void onClick(ClickEvent event) {
				PropertyEditPopup.this.hide();
				returnFocus();
			}});
		panel.add(cancelButton);
		//cancelButton.addStyleName("floatRight");
		Button okButton = new Button("Ok", new ClickHandler(){
			public void onClick(ClickEvent event) {
				editPanel.save();
				PropertyEditPopup.this.hide();
				returnFocus();
			}			
		});
		panel.add(okButton);
		///okButton.addStyleName("floatRight");
		this.setWidget(panel);

	}
}
