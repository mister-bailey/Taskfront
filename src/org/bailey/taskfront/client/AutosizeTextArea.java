package org.bailey.taskfront.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.TextArea;

public class AutosizeTextArea extends TextArea {
	static int verticalPadding=0;
	
	public AutosizeTextArea(){
		super();
		//setStyleName("gwt-TextBox");
		setStyleName("bareTextBox");
		addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event) {
				AutosizeTextArea.this.autosize();
			}			
		});
		//autosize();
	}
	
	public void onLoad(){autosize();}
	public void setText(String text){
		super.setText(text);
		autosize();
	}
	
	public void autosize(){
		if(verticalPadding<0){
			Style s = getElement().getStyle();
			String pt = s.getPaddingTop();
			String pb = s.getPaddingBottom();
			if(pt.length()<2 || pb.length()<2)return;
			verticalPadding = Integer.parseInt(pt.substring(0,pt.length()-2)) + Integer.parseInt(pb.substring(0,pb.length()-2));
		}
		setVisibleLines(1);
		Element e = getElement();
		int sh = e.getScrollHeight();
		if(sh>verticalPadding)setHeight(e.getScrollHeight() - verticalPadding + "px");
	}

}
