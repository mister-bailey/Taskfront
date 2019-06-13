package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.bailey.taskfront.shared.CandidateObserver;
import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.NeededActionListItem;
import org.bailey.taskfront.shared.Representative;
import org.bailey.taskfront.shared.UID;
import org.bailey.taskfront.shared.VerbObjectItem;

public class ItemWidget extends FocusPanel implements Representative,
ClickHandler, ValueChangeHandler<String>, KeyDownHandler, KeyUpHandler, DoubleClickHandler, ContextMenuHandler  {
	public static boolean animate=false;
	
	String itemID;
	ItemVerticalPanel panel;
	HorizontalPanel hpanel;
	ProgressBarOverlay pendingAction; // TODO: action framework
	
	HasText itemText;
	protected TextBoxBase getTextBox(){return (TextBoxBase) itemText;}
	
	public interface ListWidget extends CandidateObserver, IsWidget {
		public void focusFromAbove(int cursor);
		public void focusFromBelow(int cursor);
		public ItemWidget getWidget(String itemID);
	}
	ListWidget listWidget;
	boolean listDisplayed = true;
	ListType listType = ListType.ITEM_LIST;
	public CandidateObserver candidateObserver(){
		if(listDisplayed) return getListWidget();  // return previously created listwidget, or create it
		else if(listWidget!=null) return listWidget; // return collapsedlist
		else return Item.dummyCandidateObserver(); // if no collapsedlist, return dummy
	}
	
	public enum ListType{
		ITEM_LIST,
		IMMUTABLE_LIST,
		HEADER_LIST,
		BARE_LIST,
		DISPOSAL_LIST,
		SCROLL_LIST
	}
	
	class ItemVerticalPanel extends VerticalPanel implements ItemPanel {
		public ItemVerticalPanel(){super();}
		public ItemWidget itemWidget() {return (ItemWidget) this.getParent();}
	}
	class ItemDeepHorizontalPanel extends HorizontalPanel implements ItemPanel {
		public ItemDeepHorizontalPanel(){super();}
		public ItemWidget itemWidget() {return (ItemWidget) this.getParent().getParent();}		
	}
	
	public ItemWidget(String itemID){
		super();
		initialize(itemID);
		setDraggable();
	}
	
	public void initialize(String itemID){		
		AutosizeTextArea itemText = (AutosizeTextArea) (this.itemText = new AutosizeTextArea());
		///itemText.addStyleName("bareTextBox");
		this.itemID = itemID;
		Item item = getItem();
		//item.addObserver(this);  This now happens when Widget is attached to document.
		//if(item.hasProperty("completed"))itemText.addStyleName("completed");
		super.add(panel = new ItemVerticalPanel());
		panel.setWidth("100%");
		hpanel = new HorizontalPanel();
		hpanel.setWidth("100%");
		panel.add(hpanel);
		hpanel.addStyleName("absolutePanel");
		this.setStyleName("gwt-Item");
		Button bullet = new Button(BULLET_TEXT,new ClickHandler(){
			public void onClick(ClickEvent event) {
				//ItemWidget.this.setFocus(true);
				/*if(ContentEditPane.currentPane==null || ContentEditPane.currentPane.itemID != ItemWidget.this.itemID)
					ContentEditPane.showNew(ItemWidget.this.itemID,
						ItemWidget.this.getAbsoluteLeft()+ItemWidget.this.getOffsetWidth()+4,
						ItemWidget.this.getAbsoluteTop());*/
			}});
		bullet.addDoubleClickHandler(this);
		bullet.setStyleName("emptyButton");
		bullet.addStyleName("noPadding");
		hpanel.add(bullet);
		//itemText.setStylePrimaryName("gwt-ItemText");
		itemText.addValueChangeHandler(this);
		itemText.addKeyDownHandler(this);
		//itemText.addKeyUpHandler(this); TODO: enable at some point
		//this.addClickHandler(this); TODO: get selection functionality working
		hpanel.add(itemText);
		hpanel.setCellWidth(bullet,"1em");
		hpanel.setCellWidth(itemText,"80%");
		hpanel.add(new HTML());
		//hpanel.setSpacing(0);
		
		update();
		//hpanel.add(new ItemDeepHorizontalPanel());
		if(item.children != null && !item.children.isEmpty())getListWidget();
		itemText.addBlurHandler(new BlurHandler(){
			public void onBlur(BlurEvent event) {
				Item item = ItemWidget.this.getItem();
				String newText = item.getText();
				String oldText = (ItemWidget.this.itemText != null) ? ItemWidget.this.itemText.getText() : null; 
				if(oldText != null && !oldText.equals(newText)){
					item.setText(oldText);
				}
			}			
		});
		// Showing/hiding the editing pane
		// TODO try other triggers than focus
		/*addFocusHandler(new FocusHandler(){
			public void onFocus(FocusEvent event) {
				if(ContentEditPane.currentPane==null || ContentEditPane.currentPane.itemID != ItemWidget.this.itemID)
					ContentEditPane.showNew(ItemWidget.this.itemID,
						ItemWidget.this.getAbsoluteLeft()+ItemWidget.this.getOffsetWidth()+4,
						ItemWidget.this.getAbsoluteTop());
			}
		});*/
		
		addDomHandler(this, ContextMenuEvent.getType());
	}
		
	// Draggable functionality
	public void setDraggable(){
		getElement().setDraggable(Element.DRAGGABLE_TRUE);
		addDragStartHandler(new DragStartHandler() {
			public void onDragStart(DragStartEvent event) {
//			Window.alert("Starting drag...");
			// Required: set data for the event.
			//event.preventDefault();
			event.stopPropagation();
			event.setData("source", "Item");
			draggingUID = ItemWidget.this.itemID;
			draggingFromUID = getParentListWidget().parentItemID;
			// Optional: show a copy of the widget under cursor.
			event.getDataTransfer().setDragImage(ItemWidget.this.getElement(),10, 10);
			}
		});
		
		addDragOverHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent event) {
				if(!draggingUID.equals(ItemWidget.this.itemID)){
					if(GWT.isScript()){
						if(event.getNativeEvent().getClientY() + Window.getScrollTop() <= ItemWidget.this.getAbsoluteTop() + .5 * ItemWidget.this.getOffsetHeight())
								ItemWidget.this.addStyleDependentName("dragOverBefore");
						else ItemWidget.this.addStyleDependentName("dragOverAfter");
					} else ItemWidget.this.addStyleDependentName("dragOverBefore"); // other method too slow in hosted mode
				}
				event.stopPropagation();
				//
				//Window.alert("Dragging...");
			}
		});
		
		addDragLeaveHandler(new DragLeaveHandler(){
			public void onDragLeave(DragLeaveEvent event){
				ItemWidget.this.removeStyleDependentName("dragOverBefore");
				ItemWidget.this.removeStyleDependentName("dragOverAfter");
			}
		}
		);
		
		// Drop target functionality
		addDropHandler(new DropHandler(){
			public void onDrop(DropEvent event){
				ItemWidget.this.removeStyleDependentName("dragOverBefore");
				ItemWidget.this.removeStyleDependentName("dragOverAfter");
				if(event.getData("source").equals("Item")){
					ItemListWidget targetListWidget = getParentListWidget();
					Item draggingTo = targetListWidget.getParentItem();
					
					int i = targetListWidget.getWidgetIndex(ItemWidget.this);
					if(GWT.isScript() && event.getNativeEvent().getClientY() + Window.getScrollTop() > ItemWidget.this.getAbsoluteTop() + .5 * ItemWidget.this.getOffsetHeight()) i += 1;
					if(draggingTo.contains(draggingUID)){
						draggingTo.moveChild(i,draggingUID);
					} else {
						Item draggingFrom = Database.getItem(draggingFromUID);
						if(draggingFrom.getBaseItemID()==draggingTo.getBaseItemID()) draggingFrom.removeCandidate(draggingUID);
						else draggingFrom.hardRemove(draggingUID);
						draggingTo.insert(i,draggingUID);
					}
					
					event.preventDefault();
					event.stopPropagation();
				}
				draggingFromUID=null;
				draggingUID=null;
			}
		});		
	}
	
	protected ItemWidget(){
		super();
	}
	
	// provided only for the BareItemWidget extension.  don't use for ItemWidget!
	// TODO: not good. should separate list header styling from bare configuration
	protected void basicInitialize(String itemID, boolean showText){
		this.itemID = itemID;
		Item item = getItem();
		//item.addObserver(this);  This now happens when widget is attached to document.
		super.add(panel = new ItemVerticalPanel());
		panel.setWidth("100%");
		if(showText){
			TextBox itemText = (TextBox) (this.itemText = new TextBox());
			itemText.setReadOnly(true);
			itemText.setStyleName("listHeader");
			update();
			panel.add(itemText);
		}
		if(item.children != null && !item.children.isEmpty())getListWidget();
	}
	
	protected Item getItem(){
		return Database.getItem(itemID);
	}
	
	private ItemListWidget getParentListWidget(){
		return ((ListPanel) ItemWidget.this.getParent()).listWidget();
	}
	
	public Item getParentItem(){
		if(this.getParent() instanceof ListPanel) return this.getParentListWidget().getParentItemWidget().getItem();
		else return null;
	}
	
	// Does fill in children???
	public ListWidget getListWidget(){
		if(listWidget == null){
			if(getItem().is("collapsed")){
				listDisplayed=false;
				listWidget = new CollapsedList(null);
			} else {
				listWidget = createListWidget();
			}
			panel.add(listWidget);
		}
		return listWidget;
	}
	public ItemListWidget createListWidget(){
		ItemListWidget listWidget=null;
		switch(listType){
		case ITEM_LIST:
			listWidget = new ItemListWidget(itemID/*,childWidgetType*/);
			break;
		case IMMUTABLE_LIST:
			listWidget = new ImmutableListWidget(itemID/*,childWidgetType*/);
			break;
		case BARE_LIST:
			listWidget = new BareListWidget(itemID);
			break;
		case HEADER_LIST:
			listWidget = new HeaderListWidget(itemID);
			break;
		case DISPOSAL_LIST:
			listWidget = new DisposalListWidget(itemID);
			break;
		case SCROLL_LIST:
			listWidget = new BareListWidget(itemID);
			((ItemListWidget)listWidget).addStyleName("scrollList");
		}
		return listWidget;
	}
	
	public void removeList(){
		if(listWidget != null){
			panel.remove(listWidget);
			listWidget=null;
		}
	}
	
	@Override
	public void onClick(ClickEvent event) {
		event.stopPropagation();
		// select/deselect/whatever
		if(!this.selected) this.select();
		else this.deselect();
	}
	
	public void onDoubleClick(DoubleClickEvent event){
		if(Taskfront.mainPanel.itemID != itemID) Taskfront.setMainPanel(
				MainPanel.createFromToken(itemID + Database.getItem(itemID).getDefaultView()));
	}
	
	public void onValueChange(ValueChangeEvent<String> event){
		/*if(updating==true) {
			Window.alert("triggered a redundant value change");
			return;
		}
		updating=true;*/
		Item item = getItem();
		item.setText(event.getValue());
		//updating=false;
	}
	
	public void onKeyDown(KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		switch(keyCode){
		case KeyCodes.KEY_ENTER:
			event.stopPropagation();
			if(event.isControlKeyDown()){
				event.preventDefault();
				actionComplete(5000);
			} else if(!event.isShiftKeyDown()){
				if(listWidget!=null && listDisplayed) {
					event.preventDefault();
					newFirstEntry();
				} else if(itemText.getText().endsWith(":")) {
					event.preventDefault();
					newSublist();
				} else if(this.getParent() instanceof ListPanel){
					event.preventDefault();
					ItemListWidget parent = this.getParentListWidget();
					Item x = new Item("");
					x.save();
					int i = parent.getWidgetIndex(this)+1;
					Item parentItem = parent.getParentItem();
					parentItem.addChild(i, x.uid);
					//parentItem.saveMoveChild(i,x.uid);
					moveDown(0);
				}
			}
			break;
		case KeyCodes.KEY_DOWN:
			if(listWidget!=null && listDisplayed){
				listWidget.focusFromAbove(((TextBoxBase)itemText).getCursorPos());
			}else moveDown(getTextBox().getCursorPos());
			event.preventDefault();
			break;
		case KeyCodes.KEY_UP:
			/*if(listWidget!=null){
				listWidget.focusFromBelow(itemText.getCursorPos());
			}else*/ moveUp(getTextBox().getCursorPos());
			event.preventDefault();
			break;
		case KeyCodes.KEY_BACKSPACE:
			if(itemText.getText().isEmpty() && this.getParent() instanceof ListPanel){
				event.preventDefault();
				ItemListWidget parent = this.getParentListWidget();
				parent.focusUpAt(parent.getWidgetIndex(this)-1, 100);
				getItem().delete();
				/*Item parentItem = parent.getParentItem();
				parentItem.hardRemove(itemID);
				Database.delete(itemID); // !!!!!!!!!!!*/
			}
			break;
		case KeyCodes.KEY_TAB:
			event.preventDefault();
			if(event.isShiftKeyDown()){
				deindent();
			} else {
				indent();
			}
			break;
		case 186:
		case 59:
			if(event.isControlKeyDown()){
				event.preventDefault();
				Item i = this.getItem();
				Item f = i.createFollowUp(null);
				if(!i.is("completed")) actionComplete(5000);
				ItemWidget fw = f.getWidget();
				fw.setFocus(true);
				fw.selectAllText();
			}
			break;
		case 'E':
			if(event.isControlKeyDown()){
				event.preventDefault();
				if(event.isShiftKeyDown()){
					ItemEditPopup.showNew(itemID, this.getAbsoluteLeft() + 10, this.getAbsoluteTop() + 5);
					ItemEditPopup.onReturn = new Command(){
						public void execute() {ItemWidget.this.setFocus(true);}						
					};
				} else {
					PropertyEditPopup.showNew(new ItemPropertyEditPanel(itemID,""), this.getAbsoluteLeft() + 10, this.getAbsoluteTop() + 5);
					PropertyEditPopup.onReturn = new Command(){
						public void execute() {ItemWidget.this.setFocus(true);}						
					};
				}
				ItemEditPopup.currentPopup.setFocus();
			}
			break;
		case 'P':
			if(event.isControlKeyDown()){
				event.preventDefault();
				actionPostpone(5000);
			}
			break;
		case 'Z':
			if(event.isControlKeyDown()){
				event.preventDefault();
				if(pendingAction != null)pendingAction.cancel();
			}
			break;
		case KeyCodes.KEY_DELETE:
			if(event.isControlKeyDown()){
				event.preventDefault();
				actionDelete(5000);
			}
			break;
		/*case 'C':
			if(event.isControlKeyDown()){
				event.preventDefault();
				actionComplete();
			}
			break;*/
			// ctrl t,w,m,f etc for specific postponements
			// ctrl up-down for sending up and down between lists
		}
	}
	public void onKeyUp(KeyUpEvent event) {
		// check the current state of the textbox to see if some action should be triggered,
		// such as recognizing a verb.
		// TODO: scan text with startswith, instead of just matching; defer this workload?
		if(VerbObjectItem.verbs.contains(itemText.getText().toLowerCase())) {
			String verbText = itemText.getText();
			ItemListWidget parentListWidget = this.getParentListWidget();
			Item parent = parentListWidget.getParentItem();
			int i = parent.children.indexOf(itemID);
			getItem().delete();
			parent.addChild(i,VerbObjectItem.createVerbObjectItem(verbText,null).uid);
			parentListWidget.focusDownAt(i,0);
		}
	}
	
	public ItemDeepHorizontalPanel inlineInterface(){return (ItemDeepHorizontalPanel) hpanel.getWidget(2);}
	
	void actionPostpone(int delay){
		ActionPostpone p;
		if(pendingAction instanceof ActionPostpone){
			p = new ActionPostpone(itemID,((ActionPostpone)pendingAction).magnitude+1){
				public void onUnload(){ItemWidget.this.pendingAction=null;}
			};
			pendingAction.cancel();
		} else if(pendingAction != null) {
			pendingAction.cancel();
			p = new ActionPostpone(itemID,1){
				public void onUnload(){ItemWidget.this.pendingAction=null;}
			};
		} else p = new ActionPostpone(itemID,1){
			public void onUnload(){ItemWidget.this.pendingAction=null;}
		};
		pendingAction=p;
		panel.insert(pendingAction,0);
		if(delay>=0) p.begin(delay);
	}
	void actionDelete(int delay){
		if(pendingAction != null) pendingAction.cancel();
		pendingAction = new ProgressBarOverlay(){
			void onComplete() {
				ItemWidget.this.getItem().delete();
			}
			public void onUnload(){ItemWidget.this.pendingAction=null;}
		};
		pendingAction.setProgressColor("LightCoral");
		panel.insert(pendingAction,0);
		if(delay>=0) pendingAction.begin(delay);
	}
	void actionComplete(int delay){
		if(pendingAction!=null)	pendingAction.cancel();
		pendingAction=new ActionComplete(itemID);
		panel.insert(pendingAction,0);
		if(delay>=0) pendingAction.begin(delay);
		/* Alternatively, don't set as new pending action, but just run onComplete. */
	}
	
	void moveDown(int cursor){
		if (this.getParent() instanceof ListPanel){
			ItemListWidget parent = this.getParentListWidget();
			parent.focusDownAt(parent.getWidgetIndex(this)+1, cursor);
		}
	}
	void moveUp(int cursor){
		if (this.getParent() instanceof ListPanel){
			ItemListWidget parent = this.getParentListWidget();
			parent.focusUpAt(parent.getWidgetIndex(this)-1, cursor);
		}
	}
	
	public void focus(int cursor){
		TextBoxBase itemText = getTextBox();
		itemText.setFocus(true);
		int l = itemText.getText().length();
		itemText.setCursorPos(l >= cursor ? cursor : l);		
	}
	public void focusFromAbove(int cursor){
		focus(cursor);
	}
	public void focusFromBelow(int cursor){
		if(listWidget==null || !listDisplayed){
			focus(cursor);
		}
		else {listWidget.focusFromBelow(cursor);}
	}
	public void indent(){ // For partially displayed, or not-displayed lists, this might not work right.
		if(this.getParent() instanceof ListPanel){
			ItemListWidget parentListWidget = this.getParentListWidget();
			int i = parentListWidget.getWidgetIndex(this);
			if(i>0){
				int c = getTextBox().getCursorPos();
				Item parentItem = parentListWidget.getParentItem();
				parentItem.hardRemove(itemID);
				//parentItem.saveRemoveChild(itemID);
				ItemWidget xwidget = parentListWidget.getWidget(i-1); 
				Item x = xwidget.getItem();
				x.add(itemID);
				//x.saveMoveChild(x.UIDlist.size()-1, itemID);
				if(xwidget.listWidget!=null && ((ItemListWidget) xwidget.listWidget).getWidgetCount()>0) xwidget.listWidget.getWidget(itemID).focus(c);
				
			}
		}
	}
	public void deindent(){ // For partially displayed lists, this might not work right.
		if(this.getParent() instanceof ListPanel && this.getParentListWidget().getParentItemWidget().getParent() instanceof ListPanel){
			int c = getTextBox().getCursorPos();
			ItemListWidget currentListWidget = this.getParentListWidget();
			ItemListWidget newListWidget = currentListWidget.getParentItemWidget().getParentListWidget();
			int i = newListWidget.getWidgetIndex(currentListWidget.getParentItemWidget())+1;
			Item currentParentItem = currentListWidget.getParentItem();
			currentParentItem.hardRemove(itemID);
			//currentParentItem.saveRemoveChild(itemID);
			Item newParentItem = newListWidget.getParentItem();
			newParentItem.addChild(i, itemID);
			//newParentItem.saveMoveChild(i,itemID);
			newListWidget.getWidget(itemID).focus(c);
		}
	}
	
	public void update(){ // doesn't update list
		if(textHasFocus()) return;
		Item item = getItem();
		itemText.setText(item.toString());
		((UIObject) itemText).setStyleName("completed",item.is("completed"));
	}
	public void deleteRepresentative(){
		// only called by item.delete()
		// in fact, this instance is probably never called.
		// nonetheless:
		this.removeFromParent();
	}
	
	public void replaceItem(String itemID){
		super.remove(panel);
		initialize(itemID);
	}
	
	protected void onLoad(){
		super.onLoad();
		Item item = getItem();
		if(item.representatives.contains(this)){
			return;
		}
		getItem().addRepresentative(this);
	}
	protected void onUnload(){
		super.onUnload();
		Item i = UID.getItem(itemID);
		if(i != null) i.removeRepresentative(this);
		/*if(textHasFocus()){
			ItemListWidget lw = this.getParentListWidget();
			int index = lw.getWidgetIndex(this);
			if(index < lw.getChildCount()-1 || index==0) moveDown(((TextBoxBase)itemText).getCursorPos());
			else moveUp(((TextBoxBase)itemText).getCursorPos());			
		}*/
		//if(i.getText() != itemText.getText()) i.setText(itemText.getText()); // override for compound items like verb-object
	}
	
	public native static boolean hasFocus(Element element)/*-{
		return element.ownerDocument.activeElement == element;
	}-*/;
	
	public boolean textHasFocus(){
		return hasFocus(((UIObject) itemText).getElement());
	}
	
	public void setFocus(boolean b){
		getTextBox().setFocus(b);
	}
	
	public void selectAllText(){
		getTextBox().selectAll();
	}
	
	// Can I get this into CSS?
	public static final String BULLET_TEXT = "&bull;&nbsp;"; 	
	
	// ************************
	// Sublist-related routines
	// ************************
	
	public void newSublist(){
		newFirstEntry();		
	}
	public void newFirstEntry(){
//		System.out.println("Adding to sublist of " + item);
		Item x = new Item("");
		x.save();
		getItem().addFirst(x.uid);
		//item.saveMoveChild(0,x.uid);
		listWidget.focusFromAbove(0);
	}
	
	// **********************************
	// Selection and drag-n-drop routines
	// **********************************
	
	private static List<ItemWidget> selectedWidgets=new ArrayList<ItemWidget>();
	public boolean selected=false;
	public boolean select(){
		if(this.selected=true) return true;
		selectedWidgets.add(this);
		this.selected=true;
		this.addStyleDependentName("selected");
		return selected;
	}
	public boolean deselect(){
		selectedWidgets.remove(this);
		this.selected=false;
		this.removeStyleDependentName("selected");
		return false;
	}
	
	// ****************************
	// Single-element drag and drop
	// ****************************
	
	public static String draggingUID;
	public static String draggingFromUID;
	
	public void collapseList(){
		if(listWidget==null || !listDisplayed) return;
		listDisplayed=false;
		getItem().setIs("collapsed",true);
		CollapsedList collapsed = new CollapsedList(listWidget);
		ExpandCollapseAnimation animation = new ExpandCollapseAnimation(listWidget.asWidget(), collapsed, false);
		listWidget=collapsed;
		if(animate){
			animation.run(5000);
		} else {
			animation.onStart();
			animation.onComplete();
		}
	}
	
	public void expandList(){
		if(listWidget!=null && listDisplayed) return;
		listDisplayed=true; // TODO: watch out! it may be possible to invoke a contraction while expanding!
		getItem().setIs("collapsed",false);
		CollapsedList collapsed = (CollapsedList) listWidget;
		listWidget=null;
		listWidget = collapsed.fullListWidget==null ? getListWidget() : collapsed.fullListWidget;
		ExpandCollapseAnimation animation = new ExpandCollapseAnimation(listWidget.asWidget(), collapsed,true);
		if(animate){
			animation.run(5000);
		} else {
			animation.onStart();
			animation.onComplete();
		}
	}
	
	public String toString(){
		return "Widget:" + getItem().toString();
	}
	
	public void neededActionPopup(String verb){
		ItemEditPopup pop = new ItemEditPopup(new NeededActionEditPanel(ItemWidget.this.itemID, verb));
		ItemEditPopup.onReturn = new Command(){
			public void execute(){
				ItemWidget.this.setFocus(true);
			}
		};
		pop.setPopupPosition(ItemWidget.this.getAbsoluteLeft() + 10, this.getAbsoluteTop() + 5);
		pop.show();		
	}

	protected static PopupPanel contextPopup;
	private static void hideContextMenu(){
		if(ItemWidget.contextPopup != null){
			ItemWidget.contextPopup.hide();
			ItemWidget.contextPopup=null;
		}		
	}
	public void onContextMenu(ContextMenuEvent event) {
		event.preventDefault();
		event.stopPropagation();
		contextPopup = new PopupPanel(true);
		MenuBar contextMenu = new MenuBar(true);
		contextPopup.setWidget(contextMenu);
		
		contextMenu.addItem("Edit",new Command(){
			public void execute() {
				ItemWidget.hideContextMenu();
				ItemEditPopup.showNew(ItemWidget.this.itemID, ItemWidget.this.getAbsoluteLeft() + 10, ItemWidget.this.getAbsoluteTop() + 5);
				ItemEditPopup.onReturn = new Command(){
					public void execute() {ItemWidget.this.setFocus(true);}						
				};
			}
		});
		/*contextMenu.addItem("Edit property",new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				PropertyEditPopup.showNew(new ItemPropertyEditPanel(itemID,""), ItemWidget.this.getAbsoluteLeft() + 10, ItemWidget.this.getAbsoluteTop() + 5);
				PropertyEditPopup.onReturn = new Command(){
					public void execute() {ItemWidget.this.setFocus(true);}						
				};				
			}
		});*/
		MenuBar neededActionMenu = new MenuBar(true);
		contextMenu.addItem("Needed action",neededActionMenu);
		neededActionMenu.addItem("work on",new Command(){
			public void execute() {
				ItemWidget.hideContextMenu();
				neededActionPopup("work on");
			}			
		});
		neededActionMenu.addItem("talk to",new Command(){
			public void execute() {
				ItemWidget.hideContextMenu();
				neededActionPopup("talk to");
			}			
		});
		neededActionMenu.addItem("plan",new Command(){
			public void execute() {
				ItemWidget.hideContextMenu();
				neededActionPopup("plan");
			}			
		});
		neededActionMenu.addItem("Other...",new Command(){
			public void execute() {
				ItemWidget.hideContextMenu();
				neededActionPopup(null);
			}			
		});

		
		
		contextMenu.addSeparator();
		/*contextMenu.addItem("Delete", new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				if(Window.confirm("Delete?  Are you sure?")) ItemWidget.this.getItem().delete();
			}
		});*/
		contextMenu.addItem("Remove from list", new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				if(ItemWidget.this.getParent() instanceof ListPanel && Window.confirm("Remove from list?  Are you sure?")) Database.getItem(getParentListWidget().parentItemID).hardRemove(ItemWidget.this.itemID);
			}
		});
		
		MenuBar insertMenu = new MenuBar(true);
		insertMenu.addItem("New needed-action list",new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				NeededActionListItem item = new NeededActionListItem("","mainList","do",5*DT.dayLength,10*DT.dayLength);
				item.selfSave=true;
				item.save();
				ItemEditPopup.showNew(item.uid, ItemWidget.this.getAbsoluteLeft() + 10, ItemWidget.this.getAbsoluteTop() + 25);
				/*if(ItemWidget.this.getParent() instanceof ListPanel){
					li
				}else{*/
					Item insertPlace = ItemWidget.this.getItem();
					if(ItemWidget.this.getParent() instanceof ListPanel) Database.getItem(getParentListWidget().parentItemID).addBefore(insertPlace.uid,item.uid);
					else insertPlace.addFirst(item.uid);
				//}
			}
		});
		
		contextMenu.addItem("Insert",insertMenu);
		
		MenuBar views = new MenuBar(true);
		views.addItem("Timespan", new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				MainPanel.deferPanelLoad(new TimesPanel(ItemWidget.this.itemID));
			}
		});
		views.addItem("Repeated action list", new Command(){
			public void execute(){
				Item item = ItemWidget.this.getItem();
				String defaultVerb = item.hasProperty("defaultChildAction") ? item.content.getString("defaultChildAction") : null;
				InputPopup<String> verbInput = new InputPopup<String>("Action to view",new TextBox(),new InputPopup.InputHandler<String>(){
					public void input(String verb){
						MainPanel.deferPanelLoad(new FrequencyPanel(ItemWidget.this.itemID,verb,null));						
					}
				},defaultVerb,true);
				verbInput.show(ItemWidget.contextPopup);
				ItemWidget.hideContextMenu();
			}
		});
		views.addItem("Calendar", new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				MainPanel.deferPanelLoad(new CalendarPanel(ItemWidget.this.itemID));
			}
		});
		views.addItem("Zoom in", new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				MainPanel.deferPanelLoad(new MainPanel(ItemWidget.this.itemID));
			}
		});
		views.addItem("Double column", new Command(){
			public void execute(){
				ItemWidget.hideContextMenu();
				MainPanel.deferPanelLoad(new DoubleColumnPanel(ItemWidget.this.itemID, null));
			}
		});
		contextMenu.addItem("View",views);
		
		contextMenu.addSeparator();
		contextMenu.addItem("Reload", new Command(){
			public void execute(){
				Window.Location.reload();
			}
		});
		
		
		contextPopup.setPopupPosition(event.getNativeEvent().getClientX()+Window.getScrollLeft(), event.getNativeEvent().getClientY()+Window.getScrollTop());
	    contextPopup.show();
	}

}
