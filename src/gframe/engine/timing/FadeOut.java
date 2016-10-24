package gframe.engine.timing;

import gframe.engine.Lightsource;

public class FadeOut implements Timed{

	Lightsource lightsource;
	
	float originalIntensity;
	
	float fadeOutDelta;
		
	boolean done; 

	
	public FadeOut(Lightsource lightsource, long fadeOutTimeInMillis) {
		this.lightsource = lightsource;
		this.originalIntensity = lightsource.getIntensity();
		
		this.fadeOutDelta = originalIntensity / fadeOutTimeInMillis;
	}
	
	@Override
	public void timePassedInMillis(long millis) {
		
		if(done)
			return;
		
					
		float intensityFaded = fadeOutDelta * millis;	
		float newIntensity = Math.max(0, lightsource.getIntensity() - intensityFaded);
		lightsource.setIntensity(newIntensity);
		if(newIntensity<=0){
			done = true; 			
		}		
	}

	@Override
	public boolean done() {
		return this.done;
	}
	
}