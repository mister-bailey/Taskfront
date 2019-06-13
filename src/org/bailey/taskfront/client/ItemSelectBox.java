package org.bailey.taskfront.client;

import org.bailey.taskfront.client.AutocompleteOracle.ItemSuggestion;
import org.bailey.taskfront.shared.VerbObjectItem;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class ItemSelectBox extends SuggestBox {
	String uid;
	
	public ItemSelectBox(){
		this(new AutocompleteOracle());
	}
		
	public ItemSelectBox(AutocompleteOracle oracle){
		super(oracle);
		//
		this.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
			public void onSelection(SelectionEvent<Suggestion> event) {
				ItemSuggestion s = (ItemSuggestion) event.getSelectedItem();
				ItemSelectBox.this.setUID(s.getUID());
				ItemSelectBox.this.onValueChange();
			}				
		});
	}

	public ItemSelectBox(AutocompleteOracle oracle, String uid){
		this(oracle);
		setUID(uid);
	}
	public ItemSelectBox(String uid){
		this(new AutocompleteOracle(), uid);
	}
	
	// null means no item selected
	public String getUID(){
		return uid;
	}
	public void setUID(String uid){
		this.uid=uid;
		if(uid != null) super.setValue(Database.getItem(uid).getText());
		else super.setValue("<none>");
	}
	public String getValue(){
		return getUID();
	}
	public void setValue(String uid){
		setUID(uid);
	}

	protected void onValueChange() {} // override this, or implement even/handler stuff

	// TODO: implement event/handler stuff
	
	// drag and drop function
}
