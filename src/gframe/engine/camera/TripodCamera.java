package gframe.engine.camera;

import gframe.engine.Camera;
import gframe.engine.Point3D;

public class TripodCamera extends Camera{	 
  
  public TripodCamera(){
  	super();
  }
  		
  public TripodCamera(Point3D origin){
  	super(origin);  	
  }
  
  public void rotate(float ax, float ay){
  	super.rotate(ax, 0, 0);
  	super.rotate(getOrigin(), 0, ay, 0);
  }	
    
  
  @Override
  public void rotate(float ax, float ay, float az){
  	rotate(ax, ay);
  }                           
}