package gframe.engine;

public class MotionEvent{

  public MotionEvent(Object3D obj, Point3D oldPos){
  	this.obj 	= obj;
    this.oldPos = oldPos;	
  }
  
  public Point3D getOldPos(){
    return oldPos;
  }
  
  public Object3D getObject(){
    return obj;
  }
  
  public Point3D getNewPos(){
  	return obj.getOrigin();
  }
    
  
  private Point3D oldPos;  
  private Object3D obj;
}