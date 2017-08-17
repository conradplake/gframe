package gframe.engine.timing;

import gframe.engine.Lightsource;

public class FadeOutFadeIn implements Timed{

	Lightsource lightsource;
	
	float originalIntensity;
	
	float fadeOutDelta;
	float fadeInDelta;
	
	boolean fadeOut;			
	boolean done; 

	public FadeOutFadeIn(Lightsource lightsource, long fadeOutTimeInMillis, long fadeInTimeInMillis) {
		this(lightsource, lightsource.getIntensity(), fadeOutTimeInMillis, fadeInTimeInMillis);
	}
	
	
	public FadeOutFadeIn(Lightsource lightsource, float intensity, long fadeOutTimeInMillis, long fadeInTimeInMillis) {
		this.lightsource = lightsource;
		this.originalIntensity = intensity;
		
		this.fadeOutDelta = originalIntensity / fadeOutTimeInMillis;
		this.fadeInDelta = originalIntensity / fadeInTimeInMillis;	
		
		this.fadeOut = fadeOutTimeInMillis > 0;
	}
	
	@Override
	public void timePassedInMillis(long millis) {
		
		if(done)
			return;
		
		if(fadeOut){				
			float intensityFaded = fadeOutDelta * millis;	
			float newIntensity = Math.max(0, lightsource.getIntensity() - intensityFaded);
			lightsource.setIntensity(newIntensity);
			if(newIntensity<=0){
				fadeOut = false;
			}
		}
		else{
			float intensityFaded = fadeInDelta * millis;
			float newIntensity = Math.min(originalIntensity, lightsource.getIntensity() + intensityFaded);
			lightsource.setIntensity(newIntensity);
			
			if(lightsource.getIntensity()>=originalIntensity){
				done = true;
			}
		}
		
	}

	@Override
	public boolean done() {
		return this.done;
	}
	
}