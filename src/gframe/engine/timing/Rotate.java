package gframe.engine.timing;

import gframe.engine.Model3D;

/**
 * Effekt: reinzoomen, wieder rauszoomen durch bewegung des cam origin's in blickrichtung
 * */
public class Rotate implements Timed{
	
	Model3D model;	
	
	long rotateTimeInMillis;
		
	float velocity;

	int axis;
	
	public final static int AXIS_X = 0;
	public final static int AXIS_Y = 1;
	public final static int AXIS_Z = 2;
	
	public Rotate(Model3D model, long rotateTimeInMillis, float degreesPerMilli, int axis){
		this.model = model;	
		this.rotateTimeInMillis = rotateTimeInMillis;	
		this.velocity =  degreesPerMilli;
		this.axis = axis;
	}
	
	@Override
	public void timePassedInMillis(long millis) {									
		float degrees = millis * velocity;
		
		if(axis==AXIS_X)
			model.rotate(degrees, 0, 0);	
		if(axis==AXIS_Y)
			model.rotate(0, degrees, 0);
		if(axis==AXIS_Z)
			model.rotate(0, 0, degrees);
	}

	@Override
	public boolean done() {
		return rotateTimeInMillis<=0;
	}
	
}