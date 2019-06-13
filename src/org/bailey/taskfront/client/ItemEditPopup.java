package org.bailey.taskfront.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.client.ui.HasWidgets;

import org.bailey.taskfront.shared.CandidateObserver;
import org.bailey.taskfront.shared.Item;

public class ItemEditPopup extends PopupPanel {
	String itemID;
	EditPanel editPanel;
	static ItemEditPopup currentPopup;
	public static Command onReturn;
	
	public static ItemEditPopup newPopup(String itemID){
		if(currentPopup != null) /*((HasWidgets)*/ currentPopup.hide();//getParent()).remove(currentPane);
		return currentPopup = new ItemEditPopup(itemID);
	}
	public static void showNew(String itemID, int x, int y){
		newPopup(itemID);
		currentPopup.setPopupPosition(x,y);
		currentPopup.show();
		//currentPane.center();
	}
	
	protected ItemEditPopup(){
		super(true);
	}
	
	protected ItemEditPopup(String itemID){
		super(true);
		this.itemID=itemID;
		VerticalPanel panel = new VerticalPanel();
		panel.add(editPanel = ItemEditPanel.createPanel(itemID));
		FlowPanel buttonPanel = new FlowPanel();
		panel.add(buttonPanel);
		
		Button cancelButton = new Button("Cancel", new ClickHandler(){
			public void onClick(ClickEvent event) {
				ItemEditPopup.this.hide();
				returnFocus();
			}});
		buttonPanel.add(cancelButton);
		cancelButton.addStyleName("floatRight");
		Button okButton = new Button("Ok", new ClickHandler(){
			public void onClick(ClickEvent event) {
				editPanel.save();
				ItemEditPopup.this.hide();
				returnFocus();
			}			
		});
		buttonPanel.add(okButton);
		okButton.addStyleName("floatRight");
		this.setWidget(panel);
	}
	
	// a compact edit popup, perhaps for a single property:
	public ItemEditPopup(EditPanel editPanel){
		this.editPanel = editPanel;
		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.add(editPanel);
		Button okButton = new Button("Ok", new ClickHandler(){
			public void onClick(ClickEvent event) {
				ItemEditPopup.this.editPanel.save();
				ItemEditPopup.this.hide();
				returnFocus();
			}			
		});
		hpanel.add(okButton);
		Button cancelButton = new Button("Cancel", new ClickHandler(){
			public void onClick(ClickEvent event) {
				ItemEditPopup.this.hide();
				returnFocus();
			}});
		hpanel.add(cancelButton);
		this.setWidget(hpanel);		
	}
	
	public CandidateObserver listObserver() {return Item.dummyCandidateObserver();}
	public void update() {

	}
	public void deleteObserver(){hide();}
	public void removeList() {}
	
	public void setFocus(){
		editPanel.setFocus();
	}
	
	public void returnFocus(){
		if(onReturn != null){
			Command c = onReturn;
			onReturn = null;
			c.execute();
		}
	}

}
