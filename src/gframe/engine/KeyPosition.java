package gframe.engine;


/**
 * 
 * This class represents a position and orientation in 3D space at a specific point in time.
 * 
 * E.g. one can use objects of this class to store positions for animated camera movement (fly-bys).
 * */
public class KeyPosition {

	long timestamp;
	Object3D position; // origin and orientation
	
	public KeyPosition(long timestamp, Object3D position) {
		this.timestamp = timestamp;
		this.position = position;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public Object3D getPosition() {
		return position;
	}
	public void setPosition(Object3D position) {
		this.position = position;
	}
	
	
	/**
	 * Interpolate between specified position and this target position to find current position
	 * */
	public void updatePosition(long lastTime, long currentTime, Object3D aPosition){					
		long totalTime = timestamp - lastTime;
		if(totalTime<0)
			totalTime = 0;
		
		float lerpFactor;
		if(totalTime==0){
			lerpFactor = 1;
		}else{
			long timeElapsed = currentTime - lastTime;		
			lerpFactor = Math.min(1f, timeElapsed / (float)totalTime);
		}
			
		aPosition.getOrigin().lerp(this.position.getOrigin(), lerpFactor);
		aPosition.getMatrix().lerp(this.position.getMatrix(), lerpFactor);		
	}
	
	
	public void updatePosition(long lastTime, long currentTime, Matrix3D aPosition){					
		long totalTime = timestamp - lastTime;
		if(totalTime<0)
			totalTime = 0;
		
		float lerpFactor;
		if(totalTime==0){
			lerpFactor = 1;
		}else{
			long timeElapsed = currentTime - lastTime;		
			lerpFactor = Math.min(1f, timeElapsed / (float)totalTime);
		}
					
		aPosition.lerp(this.position.getMatrix(), lerpFactor);		
	}
	
	
	public void updatePosition(long lastTime, long currentTime, Point3D aPosition){		
		long totalTime = timestamp - lastTime;
		if(totalTime<0)
			totalTime = 0;
		
		float lerpFactor;
		if(totalTime==0){
			lerpFactor = 1;
		}else{
			long timeElapsed = currentTime - lastTime;		
			lerpFactor = Math.min(1f, timeElapsed / (float)totalTime);
		}
		
		aPosition.lerp(this.position.getOrigin(), lerpFactor);			
	}
	
}
