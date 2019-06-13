package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.client.ItemWidget;
import org.bailey.taskfront.client.ItemWidget.ListWidget;

import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

// the very existence of this class offends me.
public class BareItemWidget extends ItemWidget {
	public void addFocusStyle(){
		if(itemText != null) ((Widget)itemText).addStyleDependentName("dragOver");
		else addStyleDependentName("dragOver");
	}
	public void removeFocusStyle(){
		if(itemText != null) ((Widget)itemText).removeStyleDependentName("dragOver");
		else removeStyleDependentName("dragOver");
	}

	public BareItemWidget(){super();}
	public BareItemWidget(String itemID, boolean showText) {
		super();
		listType=ListType.BARE_LIST;
		basicInitialize(itemID, showText);
		bareInitialize();
	}
	
	public BareItemWidget(String itemID, boolean showText, ListType listType/*, WidgetType childWidgetType*/) {
		super();
		this.listType = listType;
		//this.childWidgetType = childWidgetType;
		basicInitialize(itemID, showText);
		bareInitialize();
	}
	
	public void bareInitialize(){//String itemID, boolean showText){

		this.setStyleName("gwt-BareItem");
		
		addKeyDownHandler(this);
		
		addDragOverHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent event) {
				if(!draggingUID.equals(BareItemWidget.this.itemID)) BareItemWidget.this.addFocusStyle();
				event.stopPropagation();
				//
				//Window.alert("Dragging...");
			}
		});
		
		addDragLeaveHandler(new DragLeaveHandler(){
			public void onDragLeave(DragLeaveEvent event){
				BareItemWidget.this.removeFocusStyle();
			}
		}
		);
		
		// Drop target functionality
		addDropHandler(new DropHandler(){
			public void onDrop(DropEvent event){
				BareItemWidget.this.removeFocusStyle();
				//Window.alert("..." + event.getData("source") + "...");
				if(event.getData("source").equals("Item")){
					//Window.alert("Dropping " + Database.getItem(draggingFromUID).getText());
					//ListWidget targetListWidget = BareItemWidget.this.getListWidget();
					Item draggingTo = BareItemWidget.this.getItem();
					// I should find out whether we are toward the top or bottom of the target
					// For now, I'll just add in front of the target
					if(draggingTo.uid==draggingFromUID){
						draggingTo.moveChild(0,draggingUID);
						//draggingTo.saveMoveChild(0,draggingUID);
					} else {
						Item draggingFrom = Database.getItem(draggingFromUID);
						draggingFrom.removeCandidate(draggingUID);
						//draggingFrom.saveRemoveChild(draggingUID);
						draggingTo.insert(0,draggingUID);
						//draggingTo.saveMoveChild(0,draggingUID);
					}
					draggingFromUID=null;
					draggingUID=null;
					event.preventDefault();
					event.stopPropagation();
				}
			}
		});

	}
	
	public ListWidget getListWidget(){
		if(listWidget != null) return listWidget;
		listWidget = createListWidget();
		panel.add(listWidget);
		((UIObject) listWidget).setStyleName("gwt-TopList");
		return listWidget;
	}
	
	//public void update(){}
	
	public void onKeyDown(KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		switch(keyCode){
			case KeyCodes.KEY_ENTER:
				event.preventDefault();
				event.stopPropagation();
				newFirstEntry();
		}
	}
	
	// when control is passed in and out, go directly to the list
	public void focusFromAbove(int cursor){
		if(listWidget!=null){
			listWidget.focusFromAbove(((TextBoxBase)itemText).getCursorPos());
		}else moveDown(cursor);
	}
	public void focusFromBelow(int cursor){
		if(listWidget==null){moveUp(cursor);}
		else {listWidget.focusFromBelow(cursor);}
	}
	public void updateList(){}
	
}
