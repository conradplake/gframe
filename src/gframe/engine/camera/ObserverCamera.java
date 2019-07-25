package gframe.engine.camera;

import gframe.engine.Camera;
import gframe.engine.MotionEvent;
import gframe.engine.MotionListener;
import gframe.engine.Point3D;

public class ObserverCamera extends Camera implements MotionListener{
   
  public ObserverCamera(){
    super();
  }
  
  
  public ObserverCamera(Point3D origin){
  	super(origin);
  }  
  
  public void processMotionEvent(MotionEvent me){
  	reset();  	
	focus( me.getNewPos() );
  }        
}