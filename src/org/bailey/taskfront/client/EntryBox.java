package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.Item;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;

// ************ NOT USED! **************************

// how should this function in the midst of the list, for insertion?
// how should we deal with editing existing entries?
// perhaps ItemWidget should also function as a TextBox

// also, perhaps this should derive from SuggestBox
public class EntryBox extends TextBox implements KeyUpHandler{
	private Item target;
	public int targetIndex=-1;
	
	public EntryBox(Item target){
		super();
		this.target=target;
		this.addKeyUpHandler(this);
	}
	public void setTarget(Item target){
		this.target=target;
	}
	public void setTargetIndex(int targetIndex){
		this.targetIndex=targetIndex;
	}
	public void onKeyUp(KeyUpEvent event) {
		System.out.println("EntryBox KeyUpEvent");
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			System.out.println("  KEY_ENTER");
			Item x = new Item(getText());
			x.save();
			if(targetIndex >= 0 && targetIndex <= target.children.size()){
				System.out.println("  Why am I here???");
				target.addChild(targetIndex,x.uid);
				targetIndex++;
			} else {
				//System.out.println("EntryBox trying to add '" + getText() + "' to " + target.toString());
				target.add(x.uid);
			}
			target.saveMoveChild(targetIndex,x.uid);
			setText("");
		}
	}
}
