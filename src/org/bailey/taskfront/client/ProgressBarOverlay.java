package org.bailey.taskfront.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HTML;

public abstract class ProgressBarOverlay extends HTML {
	//double progress=0;
	//boolean inProgress=false;
	private Animation animation;

	public ProgressBarOverlay(){
		super("&nbsp;");
		addStyleName("actionProgress");
	}

    public void setProgressColor(String color){
        getElement().getStyle().setProperty("backgroundColor", color);
    }
	private void setProgress(double progress){
		//this.progress=progress;
		setWidth(String.valueOf((int)(progress*100)) + "%");
	}

	public void begin(int time){
		animation = new Animation(){
			protected void onUpdate(double progress) {
				ProgressBarOverlay.this.setProgress(progress);
			}
			protected void onComplete(){
				((ComplexPanel)ProgressBarOverlay.this.getParent()).remove(ProgressBarOverlay.this);
				ProgressBarOverlay.this.onComplete();
			}
			protected void onCancel(){
				ProgressBarOverlay.this.onCancel();
			}
			protected double interpolate(double progress){return progress;}
		};
		animation.run(time);
	}
	
	public void cancel(){
		if(animation != null) animation.cancel();
		((ComplexPanel)ProgressBarOverlay.this.getParent()).remove(ProgressBarOverlay.this);
		onCancel();
	}
	
	public void fixProgress(double progress){
		if(animation != null) animation.cancel();
		setProgress(progress);		
	}
	
	public void complete(){
		if(animation != null) animation.cancel();
		ComplexPanel parent = (ComplexPanel)this.getParent(); 
		if(parent != null)parent.remove(ProgressBarOverlay.this);
		onComplete();
	}
	abstract void onComplete();
	void onCancel(){} // override if you wish!
	
	public void onLoad(){ // alternatively, make sure the parent object is already relative position
		this.getParent().getElement().getStyle().setProperty("position","relative");
	}
}
