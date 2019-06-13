package org.bailey.taskfront.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public abstract class DelayedAction extends FlowPanel {
	Animation animation;
	HTML progressBar;
	boolean countingDown=false;
	double progress=0;
	protected HTML label;
	
	public DelayedAction(String text){
		this.addStyleName("delayedAction");
		progressBar = new HTML("&nbsp;");
		progressBar.addStyleName("actionProgress");
		this.add(progressBar);
		HorizontalPanel hpanel = new HorizontalPanel();
		hpanel.addStyleName("absolutePanel");
		label = new HTML(text);
		hpanel.add(label);
		Button xButton = new Button("X", new ClickHandler(){
			public void onClick(ClickEvent event){
				DelayedAction.this.cancel();
			}
		});
		xButton.setStyleName("emptyButton");
		hpanel.add(xButton);
		this.add(hpanel);
		
		animation = new Animation(){
			protected void onUpdate(double progress) {
				DelayedAction.this.setProgressBar(progress);
				DelayedAction.this.progress=progress;
			}
			protected void onComplete(){
				((ComplexPanel)DelayedAction.this.getParent()).remove(DelayedAction.this);
				DelayedAction.this.execute();
			}
			protected void onCancel(){}
			protected double interpolate(double progress){return progress;}
		};
	}
    
    public DelayedAction(String text, String color){
        this(text);
        setProgressColor(color);
    }
    
    private void initializeWidget(){
    	
    }
    
    
    public abstract void execute();
	
	public void cancel(){
		animation.cancel();
        ((ComplexPanel)this.getParent()).remove(this);
	}
	
	public void pause(){
		
	}
	public void resume(){
		
	}
	
	public void countdown(double time){
		countingDown=true;
		animation.run((int) time);
	}
	
    public void setProgressColor(String color){
        progressBar.getElement().getStyle().setProperty("backgroundColor", color);
    }
	private void setProgressBar(double progress){
		progressBar.setWidth(String.valueOf((int)(progress*100)) + "%");
	}
	
	public abstract static class ActionButton extends Button implements ClickHandler {
		ActionButton(){
			this.addClickHandler(this);
			this.setStyleName("actionButton");
		}
		ActionButton(String html){
			this();
			this.setHTML(html);
		}
		public void onClick(ClickEvent event) {
			ItemWidget w = ((ItemPanel) this.getParent()).itemWidget();
			actOn(w);
		}
		abstract void actOn(ItemWidget w);
	}

}
