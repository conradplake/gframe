package gframe.engine.timing;

import gframe.engine.Camera;

/**
 * Effekt: reinzoomen, wieder rauszoomen durch bewegung des cam origin's in blickrichtung
 * */
public class CamZoom implements Timed{
	
	Camera camera;	
	
	long moveInTimeInMillis;
	long moveOutTimeInMillis;
	float distance;
	float velocity;

	
	public CamZoom(Camera camera, long moveTimeInMillis, float distance){
		this.camera = camera;	
		this.moveInTimeInMillis = moveTimeInMillis;
		this.moveOutTimeInMillis = moveTimeInMillis;
		
		this.distance = distance;
		this.velocity =  distance / moveTimeInMillis;
	}
	
	@Override
	public void timePassedInMillis(long millis) {
		
		float dist = this.velocity * millis;
						
		if(moveInTimeInMillis>0){
			camera.move(dist);
			moveInTimeInMillis -= millis;
		}
		else{
			camera.move(-dist);
			moveOutTimeInMillis -= millis;
		}		
	}

	@Override
	public boolean done() {
		return moveInTimeInMillis<=0 && moveOutTimeInMillis<=0;
	}
	
}