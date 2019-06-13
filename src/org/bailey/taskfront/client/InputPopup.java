package org.bailey.taskfront.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

public class InputPopup<T> extends PopupPanel {
	HasValue<T> valueBox;
	InputHandler<T> callback;
	public InputPopup(String label, HasValue<T> valueBox, InputHandler<T> callback, T defaultValue, boolean autohide){
		super(autohide);
		this.callback = callback;
		FlowPanel fp = new FlowPanel();
		fp.add(new Label(label));
		fp.add((Widget) (this.valueBox = valueBox));
		
		FlowPanel buttonPanel = new FlowPanel();
		fp.add(buttonPanel);
		Button cancelButton = new Button("Cancel", new ClickHandler(){
			public void onClick(ClickEvent event) {InputPopup.this.hide();}});
		buttonPanel.add(cancelButton);
		cancelButton.addStyleName("floatRight");
		Button okButton = new Button("Ok", new ClickHandler(){
			public void onClick(ClickEvent event) {
				T input = InputPopup.this.valueBox.getValue();
				InputPopup.this.hide();
				InputPopup.this.callback.input(input);
			}			
		});
		buttonPanel.add(okButton);
		okButton.addStyleName("floatRight");
		fp.add(buttonPanel);
		
		if(valueBox instanceof HasKeyDownHandlers){
			((HasKeyDownHandlers)valueBox).addKeyDownHandler(new KeyDownHandler(){
				public void onKeyDown(KeyDownEvent event) {
					int keyCode = event.getNativeKeyCode();
					switch(keyCode){
					case KeyCodes.KEY_ENTER:
						T input = InputPopup.this.valueBox.getValue();
						InputPopup.this.hide();
						InputPopup.this.callback.input(input);						
						break;
					case KeyCodes.KEY_ESCAPE:
						InputPopup.this.hide();
						break;
					}
				}				
			});
		}
		this.setWidget(fp);
		if(defaultValue != null){
			valueBox.setValue(defaultValue);
			if(valueBox instanceof ValueBoxBase) Scheduler.get().scheduleDeferred(new ScheduledCommand(){
				public void execute() {
					((ValueBoxBase) InputPopup.this.valueBox).selectAll();
				}				
			});
		}
	}
	
	public void show(){
		super.show();
		if(valueBox instanceof FocusWidget) ((FocusWidget) valueBox).setFocus(true);
	}
	
	public void show(int x, int y){
		show();
		setPopupPosition(x,y);
	}
	
	public void show(UIObject relative){
		super.showRelativeTo(relative);
		if(valueBox instanceof FocusWidget) ((FocusWidget) valueBox).setFocus(true);
	}
	
	public interface InputHandler<T>{
		public void input(T input);
	}
}
