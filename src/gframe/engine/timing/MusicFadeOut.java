package gframe.engine.timing;

import javafx.scene.media.MediaPlayer;

public class MusicFadeOut implements Timed{

	private MediaPlayer mediaPlayer;		
	private double fadeOutDelta;		
	private boolean stopPlayerWhenDone;
	private boolean done; 

	
	public MusicFadeOut(MediaPlayer mediaPlayer, long fadeOutTimeInMillis) {
		this(mediaPlayer, fadeOutTimeInMillis, true);
	}
	
	public MusicFadeOut(MediaPlayer mediaPlayer, long fadeOutTimeInMillis, boolean stopPlayerWhenDone) {
		this.mediaPlayer = mediaPlayer;		
		this.fadeOutDelta = mediaPlayer.getVolume() / fadeOutTimeInMillis;
		this.stopPlayerWhenDone = stopPlayerWhenDone;
	}
	
	@Override
	public void timePassedInMillis(long millis) {
		
		if(done)
			return;
						
		double intensityFaded = fadeOutDelta * millis;	
		double newIntensity = Math.max(0, mediaPlayer.getVolume() - intensityFaded);
		mediaPlayer.setVolume(newIntensity);
		
		if(newIntensity<=0){
			if(stopPlayerWhenDone){
				mediaPlayer.stop();	
			}			
			done = true; 					
		}		
	}

	@Override
	public boolean done() {
		return this.done;
	}
	
}