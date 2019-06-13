package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.Item;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;

public class CollapsedList extends Button implements ClickHandler, ItemWidget.ListWidget {
	public static final String ELLIPSES = "&bull;&nbsp;&bull;&nbsp;&bull;&nbsp;&bull;&nbsp;&bull;&nbsp;&bull;";
	public ItemWidget.ListWidget fullListWidget;
	
	public CollapsedList(ItemWidget.ListWidget fullListWidget){
		super(ELLIPSES);
		this.fullListWidget = fullListWidget;
		this.setStyleName("emptyButton");
		this.addClickHandler(this);
	}

	public void onClick(ClickEvent event) {
		((ItemPanel)this.getParent()).itemWidget().expandList();
	}
	
	// Dummy listobserver methods
	// or: pass through to fullListWidget
	public void updateList() {if(fullListWidget!=null)fullListWidget.updateList();}
	//public void addLast(String id) {if(fullListWidget!=null)fullListWidget.addLast(id);}
	//public void addFirst(String id) {if(fullListWidget!=null)fullListWidget.addFirst(id);}
	public void addCandidate(int index, String id) {if(fullListWidget!=null)fullListWidget.addCandidate(index,id);}
	public void moveCandidate(int indexTo, String id) {if(fullListWidget!=null)fullListWidget.moveCandidate(indexTo,id);}
	public void moveCandidate(int indexTo, int indexFrom) {if(fullListWidget!=null)fullListWidget.moveCandidate(indexTo,indexFrom);}
	public void removeCandidate(String id) {if(fullListWidget!=null)fullListWidget.removeCandidate(id);}
	public boolean removeCandidate(int index) {
		if(fullListWidget!=null)return fullListWidget.removeCandidate(index);
		else return false;
	}
	//public void removeFirst() {if(fullListWidget!=null)fullListWidget.removeFirst();}
	//public void removeLast() {if(fullListWidget!=null)fullListWidget.removeLast();}

	@Override
	public void focusFromAbove(int cursor) {
		((ItemPanel)this.getParent()).itemWidget().moveDown(cursor);
	}

	@Override
	public void focusFromBelow(int cursor) {
		((ItemPanel)this.getParent()).itemWidget().moveUp(cursor);
	}

	@Override
	public ItemWidget getWidget(String itemID) {
		// TODO Auto-generated method stub
		return null;
	}

}
