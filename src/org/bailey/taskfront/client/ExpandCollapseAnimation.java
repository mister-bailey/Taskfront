package org.bailey.taskfront.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/*
 *  For the time being, we assume the starting widget is contained in a VerticalPanel,
 *  and we will remove it from its parent and insert the ending widget in its place.
 *  This takes place at the start of the animation if we're expanding, and at the end if
 *  we're contracting.
 */
public class ExpandCollapseAnimation extends Animation {
	Widget expandedWidget, collapsedWidget;
	boolean expand;
	int startHeight, endHeight;

	public ExpandCollapseAnimation(Widget expandedWidget, Widget collapsedWidget, boolean expand){
		this.expandedWidget=expandedWidget;
		this.expand=expand;
		this.collapsedWidget=collapsedWidget;
		if(expand){
			startHeight = collapsedWidget.getOffsetHeight();
			endHeight = expandedWidget.getOffsetHeight();
		} else {
			startHeight = expandedWidget.getOffsetHeight();
			endHeight = collapsedWidget.getOffsetHeight();	
		}
	}
	
	protected void onStart(){
		if(expand){
			VerticalPanel parent = (VerticalPanel) collapsedWidget.getParent();
			parent.insert(expandedWidget, parent.getWidgetIndex(collapsedWidget));
			collapsedWidget.removeFromParent();
			expandedWidget.setVisible(true);
		}
		// onUpdate(0);  // TODO: necessary?
	}
	protected void onUpdate(double progress) {
		if(progress > 0.5){
			progress = progress;
		}
		expandedWidget.setHeight(String.valueOf((int)(startHeight + progress * (endHeight - startHeight))) + "px");
	}
	protected void onComplete(){
		if(expand){
			expandedWidget.setHeight("auto");
		} else {
			VerticalPanel parent = (VerticalPanel) expandedWidget.getParent();
			parent.insert(collapsedWidget, parent.getWidgetIndex(expandedWidget));
			expandedWidget.removeFromParent();
			collapsedWidget.setVisible(true);
		}
	}

}
