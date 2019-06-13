package org.bailey.taskfront.client;

import com.google.gwt.user.client.ui.Widget;

/*
 *  Leads the user through a list so that they can dispose of things in it,
 *  eg., by marking complete, deleting, postponing...
 *  Doesn't permit the creation of new items, or of editing items (by default).
 *  Usually lives within a DisposalWidget (extends ItemWidget), which will be signalled
 *  in some way once every item is dealt with, to allow "exiting" the disposal activity.
 */

/*
 * I want some kind of menu system for
 */

public class DisposalListWidget extends ItemListWidget {

	public DisposalListWidget(String itemID) {
		super();
		childWidgetType = WidgetType.DISPOSAL_ITEM;
		initialize(itemID);
	}
	
	public boolean checkIfDisposalFinished(){
		for(Widget w : listPanel){
			if(!((DisposalItemWidget)w).isDisposed()) return false;
		}
		disposalIsFinished();
		return true;
	}
	
	private void disposalIsFinished(){
		
	}

}
