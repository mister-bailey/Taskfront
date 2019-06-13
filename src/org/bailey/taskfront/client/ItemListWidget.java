package org.bailey.taskfront.client;

import java.util.Iterator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

import org.bailey.taskfront.client.ItemWidget.ListWidget;
import org.bailey.taskfront.shared.ExposedSubItem;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.VerbObjectItem;

// need to write code for "summarised" lists, i.e., with only a few entries, + ellipsis

// An instance assumes it always has a valid reference parentItem.list

public class ItemListWidget extends HorizontalPanel implements ItemWidget.ListWidget {
	public static boolean animate=false;
	String parentItemID;
	
	WidgetType childWidgetType = WidgetType.ITEM;
	public enum WidgetType{
		ITEM,
		BARE_ITEM,
		DISPOSAL_ITEM,
		HEADER_ITEM
	}

//	protected int maxLength=1000;
	
	// creates an ItemListWidget and fills it from i.list
	// don't use for the first item added to a list!
	/*public ItemListWidget(String uid){
		super();
//		System.out.println("Constructing a ListWidget for " + i.toString());
		this.setStylePrimaryName("gwt-ListWidget");
		parentItemID=uid;
		//parentItem.addListObserver(this);
		fill();
	}*/
	
	ListVerticalPanel listPanel;
	class ListVerticalPanel extends VerticalPanel implements ListPanel {
		public ListVerticalPanel(){super();}
		public ItemListWidget listWidget() {return (ItemListWidget) this.getParent();}
		public WidgetCollection getChildren() {return super.getChildren();}
	}
	
	public ItemListWidget(String uid/*, WidgetType childWidgetType*/){
		super();
		initialize(uid);
	}
	public ItemListWidget(){
		super();
	}
	
	public void initialize(String uid){
		this.setWidth("100%");
		//this.childWidgetType = childWidgetType;
		Button listEdge = new Button();
		listEdge.setStyleName("verticalBar");
		listEdge.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				ItemListWidget.this.getParentItemWidget().collapseList();
			}
		});
		super.add(listEdge);
		super.add(listPanel = new ListVerticalPanel());
		listPanel.setStylePrimaryName("gwt-ListWidget");
		//super.setCellWidth(listEdge,"1em");
		super.setCellWidth(listPanel,"100%");
		//this.setStylePrimaryName("gwt-ListWidget");
		parentItemID=uid;
		fill();
	}
	
	protected Item getParentItem(){
		return Database.getItem(parentItemID);
	}
	
	public ItemWidget getParentItemWidget(){
		return ((ItemPanel) super.getParent()).itemWidget();
	}
	
	public int getWidgetIndex(ItemWidget w){return listPanel.getWidgetIndex(w);}
	
	// doesn't fill the widget or set its "item" field
	/*public ItemListWidget (){
		super();
		this.setStylePrimaryName("gwt-ListWidget");
	}*/
	
	public void focusFromAbove(int cursor){
		((ItemWidget)(listPanel.getWidget(0))).focusFromAbove(cursor);
	}
	public void focusFromBelow(int cursor){
		((ItemWidget)(listPanel.getWidget(listPanel.getWidgetCount()-1))).focusFromBelow(cursor);
	}
	
	// we assume the index will not be negative
	public void focusDownAt(int index, int cursor){
		if(index >= listPanel.getWidgetCount()){
			if(this.getParentItemWidget() instanceof ItemWidget) ((ItemWidget)this.getParentItemWidget()).moveDown(cursor);
		} else {
			((ItemWidget)listPanel.getWidget(index)).focusFromAbove(cursor);
		}
	}
	// We assume the index will not be too large
	public void focusUpAt(int index, int cursor){
		if(index < 0){
			Widget parent = this.getParentItemWidget();
			if(parent instanceof BareItemWidget) ((ItemWidget)parent).moveUp(cursor);
			else if(parent instanceof ItemWidget){
				((ItemWidget)parent).focus(cursor);
			}
		} else {
			((ItemWidget)listPanel.getWidget(index)).focusFromBelow(cursor);
		}		
	}
	
	// ok, not so bad, since this is only used for a complete refresh
	public void updateList() {
		clearList();
		fill();
	}
	protected void fill(){
		Item item = Database.getItem(parentItemID);
		if(item.children==null)return;
		for(String uid : item.children)addLast(uid);
	}
	
	public ItemWidget createChildWidget(String uid){
		switch(childWidgetType){
		case ITEM:
			Item i = Database.getItem(uid);
			if(i instanceof VerbObjectItem) return new VerbObjectWidget(uid);
			else if(i instanceof ExposedSubItem);
			return new ItemWidget(uid);
		case HEADER_ITEM:
			return new HeaderItemWidget(uid);
		case BARE_ITEM:
			return new BareItemWidget(uid, true);
		case DISPOSAL_ITEM:
			return new DisposalItemWidget(uid);
		}
		return null;
	}

	public void addLast(String uid) {
		//System.out.println("Adding to a ListWidget");
		listPanel.add(createChildWidget(uid));	
	}
	public void addFirst(String uid) {
		listPanel.insert(createChildWidget(uid), 0);		
	}
	public void addCandidate(int index, String uid) {
		//if(listPanel.getWidgetCount() > index && ((ItemWidget) listPanel.getWidget(index)).itemID==uid)return; // To deal with create-add bug
		listPanel.insert(createChildWidget(uid), index);		
	}
	public ItemWidget getWidget(int index){return (ItemWidget) listPanel.getWidget(index);}
	public ItemWidget getWidget(String itemID){
		for(Widget w : listPanel){
			if(((ItemWidget)w).itemID == itemID) return (ItemWidget)w;
		}
		return null;
	}
	public void removeCandidate(String uid) {
		ItemWidget w = getWidget(uid);
		if(w!=null)this.remove(w);
	}
	public boolean remove(ItemWidget w){
		((ItemWidget)w).getItem().removeRepresentative((ItemWidget)w);
		return listPanel.remove(w);
	}
	public boolean removeCandidate(int index){
		if(index >= 0 && listPanel.getWidgetCount() > index){
			ItemWidget w = (ItemWidget)listPanel.getWidget(index);
			w.getItem().removeRepresentative(w);
			return listPanel.remove(index);
		}
		return false;		
	}
	public void removeFirst(){
		removeCandidate(0);
	}
	public void removeLast(){
		removeCandidate(listPanel.getWidgetCount()-1);
	}
	
	public void clearList(){
//		System.out.println("Clearing a list widget: " + getParentItem().toString());
		Iterator<Widget> itr = listPanel.getChildren().iterator();
		while(itr.hasNext()){
			ItemWidget w = (ItemWidget)itr.next();
			w.getItem().removeRepresentative(w);
			itr.remove();
		}
	}
	
	@Override
	public void moveCandidate(int indexTo, String uid) {
		ItemWidget w = getWidget(uid);
		if(w==null){
			if(getParentItem().children.contains(uid)) addCandidate(indexTo,uid);
		} else {
			listPanel.insert(w, indexTo);					
		}
	}
	@Override
	public void moveCandidate(int indexTo, int indexFrom) {
		if(indexFrom >= 0 && getParentItem().children.size() > indexFrom && indexTo >= 0 && listPanel.getWidgetCount() >= indexTo){
			if(listPanel.getWidgetCount()>indexFrom){
				listPanel.insert(listPanel.getWidget(indexFrom),indexTo);
			} else {
				Window.alert("Should not be here!");
				listPanel.insert(createChildWidget(getParentItem().children.get(indexFrom)),indexTo);
			}
		}
	}

	// I want to a subclass which shows just a few items
	
	public int getChildCount(){
		return listPanel.getWidgetCount();
	}
	
	public String toString(){
		return "ListWidget:" + getParentItem().toString();
	}
	
}
