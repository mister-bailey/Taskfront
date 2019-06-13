package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.Item;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.TextBoxBase;

/*
 *  Item Widgets occurring in a DisposalListWidget, to be disposed of one way or another.
 *  No extensive editing, no cursor, but certain actions (completed, delete, postpone, etc.)
 *  may be performed.
 */

/*
 *  This reinforces my need to have a graphical means of selecting actions, which would appear
 *  by default on these widgets (but only when invoked on other widgets).
 */
public class DisposalItemWidget extends ItemWidget {
	public DisposalItemWidget(String itemID){
		super();
		this.listType = ListType.DISPOSAL_LIST; // Really? do I want sublists to be disposal lists? TODO
		initialize(itemID);
		//disposalInitialize();
	}
	
	public void disposalInitialize(){
		ComplexPanel buttonPanel = inlineInterface();
		buttonPanel.add(new ActionDelete.DeleteButton());
	}
	
	public boolean isDisposed(){return pendingAction != null;}
	
	public void onKeyDown(KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		switch(keyCode){
		case KeyCodes.KEY_ENTER:
			event.stopPropagation();
			if(event.isControlKeyDown()){
				event.preventDefault();
				actionComplete(-1);
				setProgressImminent();
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
		case 'P':
			if(event.isControlKeyDown()){
				event.preventDefault();
				actionPostpone(-1);
				setProgressImminent();
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
				actionDelete(-1);
				setProgressImminent();
			}
			break;
			// ctrl t,w,m,f etc for specific postponements
			// ctrl up-down for sending up and down between lists
		}
	}
	
	public void setProgressImminent(){
		/*Scheduler.get().scheduleDeferred(new ScheduledCommand(){
			public void execute(){
				if(DisposalItemWidget.this.pendingAction != null) DisposalItemWidget.this.pendingAction.fixProgress(1);
			}
		});*/
		if(this.pendingAction != null) this.pendingAction.fixProgress(1);
	}
	
	public void onUnload(){
		super.onUnload();
		if(pendingAction != null) pendingAction.complete();
	}

	
}
