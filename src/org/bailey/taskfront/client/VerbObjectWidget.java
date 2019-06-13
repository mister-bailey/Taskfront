package org.bailey.taskfront.client;

import org.bailey.taskfront.client.AutocompleteOracle.ItemSuggestion;
import org.bailey.taskfront.client.ItemWidget.ItemDeepHorizontalPanel;
import org.bailey.taskfront.client.ItemWidget.ItemVerticalPanel;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.VerbObjectItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class VerbObjectWidget extends ItemWidget {
	
	public HasText verbText;
	public boolean hasObject;
	
	protected TextBoxBase getTextBox(){
		if(hasObject) return (TextBoxBase) itemText; // itemtext is a regular textbox referring to postscript
		else return (TextBoxBase) ((SuggestBox)itemText).getValueBox(); // itemtext is still waiting for an object suggestion
	}
	protected VerbObjectItem getItem(){return (VerbObjectItem) super.getItem();}

	public VerbObjectWidget(String itemID){
		super();
		verbObjectInitialize(itemID);
		setDraggable();
	}

	// Do this right!
	// need to insert the verb text beforehand
	// but things in hpanel are indexed
	private void verbObjectInitialize(String itemID) {
		this.itemID = itemID;
		VerbObjectItem item = getItem();
		item.addRepresentative(this);

		super.add(panel = new ItemVerticalPanel());
		hpanel = new HorizontalPanel();
		panel.add(hpanel);
		this.setStyleName("gwt-Item");
		Button bullet = new Button(BULLET_TEXT,new ClickHandler(){
			public void onClick(ClickEvent event) {
				//VerbObjectWidget.this.setFocus(true);
			}});
		bullet.setStyleName("emptyButton");
		bullet.addStyleName("noPadding");
		hpanel.add(bullet);
		hpanel.setCellWidth(bullet,"1em");
		
		// three-part item text stuff
		HTML verbText = (HTML)(this.verbText = new HTML());
		hpanel.add((Widget) verbText);
		verbText.setStyleName("bareTextBox prewrap");
		
		// itemText plays the dual role of a box containing the postscript,
		// and a suggestbox for choosing the object, if there isn't one yet
		if(!item.hasObject()){
			hasObject=false;
			itemText = new SuggestBox(new AutocompleteOracle());
			((SuggestBox)itemText).addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
				public void onSelection(SelectionEvent<Suggestion> event) {
					ItemSuggestion s = (ItemSuggestion) event.getSelectedItem();
					VerbObjectItem item = VerbObjectWidget.this.getItem();
					item.setFilterAndUpdate(s.getUID());
				}				
			});
		} else {
			hasObject=true;
			itemText = new TextBox();
		}
		hpanel.add((Widget) itemText);

		//((UIObject) itemText).setStylePrimaryName("gwt-ItemText");
		((UIObject) itemText).setStyleName("bareTextBox");
		
		setTextHandlers();
		setDraggable();
		addDomHandler(this, ContextMenuEvent.getType());
		
		//itemText.addKeyUpHandler(this); Don't look for verbs!
		//this.addClickHandler(this); TODO: selection functionality
		
		update();
		hpanel.add(new HTML());
		if(item.children != null && !item.children.isEmpty()) getListWidget();
	}
	
	protected void setTextHandlers(){
		((HasValueChangeHandlers<String>) itemText).addValueChangeHandler(this);
		((HasKeyDownHandlers) itemText).addKeyDownHandler(this);		
	}
	
	/*public void focus(int cursor){
		
	}*/
	
	public void update(){
		VerbObjectItem item = getItem();
		if(item.hasObject()){
			if(!hasObject){
				hasObject=true;
				int i = hpanel.getWidgetIndex((Widget) itemText);
				hpanel.removeCandidate(i);
				hpanel.insert((Widget)(itemText=new TextBox()), i);
				((UIObject) itemText).setStyleName("bareTextBox");
				setTextHandlers();
			}
			verbText.setText(item.getVerbObjectText());
		} else {
			verbText.setText(item.getPrefaceText() + " ");
		}
		itemText.setText(item.getPostscript());
		((UIObject) verbText).setStyleName("completed",item.is("completed"));
		((UIObject) itemText).setStyleName("completed",item.is("completed"));
	}
	
	public void onValueChange(ValueChangeEvent<String> event){
		/*if(updating==true) {
			Window.alert("triggered a redundant value change");
			return;
		}
		updating=true;*/
		VerbObjectItem item = getItem();
		item.setPostscript(event.getValue());
		//updating=false;
	}

	// override up/down so that suggestbox works
	public void onKeyDown(KeyDownEvent event){
		int keyCode = event.getNativeKeyCode();
		if(!hasObject && (keyCode==KeyCodes.KEY_ENTER || keyCode==KeyCodes.KEY_DOWN || keyCode==KeyCodes.KEY_UP) 
				&& ((DefaultSuggestionDisplay)((SuggestBox)itemText).getSuggestionDisplay()).isSuggestionListShowing());
		else super.onKeyDown(event);
	}
}
