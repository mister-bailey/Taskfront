package org.bailey.taskfront.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootPanel;

public class FileSystem {
	public static void save(String data, String name){
		Anchor anchor = createDownloadAnchor(data, name, true);
		anchor.setVisible(false);
		RootPanel.get().add(anchor);
		clickElement(anchor.getElement());
	}
	
	public static Anchor createDownloadAnchor(String data, String name, boolean autoRemove){
		final Anchor anchor = new Anchor(name);
		anchor.setHref(createDownloadURL(data));
		anchor.getElement().setAttribute("download", name);
		
		// Gets rid of the anchor, which was probably invisible anyway
		if(autoRemove){
			anchor.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					revokeURL(anchor.getHref());
					anchor.removeFromParent();
				}				
			});
		}
		
		return anchor;
	}	
	
	
	public static final native String createDownloadURL(String data)/*-{
		blob = new Blob([data],{type:"text/plain;charset=UTF-8"});
		r = URL.createObjectURL(blob);
		return r;
	}-*/;
	
	public static final native void revokeURL(String URL)/*-{
		URL.revokeObjectURL(URL);
	}-*/;
	
	public static final native void clickElement(Element e)/*-{
		e.click();
	}-*/;
}
