package gframe;

import gframe.engine.Matrix3D;
import gframe.engine.Vector3D;

public class Space3D{
  
  
  public Space3D(){
    this(0);
  }
  
  public Space3D(float gravity){    
	setGravity(gravity);	
  }    
      
  public float getGravity(){
    return gravity;
  }
  
  public Vector3D getGravityVector(){
    return gravityVec;
  }
  
  public void setGravity(float gravity){
  	this.gravity = gravity;
	gravityVec   = new Vector3D(0, -gravity, 0);
  }
  
  
  public float altitudeOf(OIS3D ois){
    return ois.getOrigin().y;	
  }      
 
  public void simulate(OIS3D ois, float secPassed){
	  this.simulate(ois, gravityVec, secPassed);
  }
  
  public void simulate(OIS3D ois, Vector3D accVec, float secPassed){
	Matrix3D mat = ois.getMatrix();	
	Matrix3D imat = mat.getInverse();
	Vector3D v = mat.transform( ois.getVelocityVector() );  // v is relative to object space! transform to world space
	float tmpf = 0.5f * secPassed * secPassed;	// temp
	float dv_x = accVec.x * secPassed;
	float dv_y = accVec.y * secPassed;
	float dv_z = accVec.z * secPassed;
	float ds_x = v.x * secPassed + accVec.x * tmpf;
	float ds_y = v.y * secPassed + accVec.y * tmpf;
	float ds_z = v.z * secPassed + accVec.z * tmpf;
	
//	if(ds_x==Float.NEGATIVE_INFINITY || ds_x==Float.POSITIVE_INFINITY || ds_x==Float.NaN){
//		System.out.println("DEBUG");
//	}	
//	System.out.println("sim deltas: "+ds_x+", "+ds_y+", "+ds_z);
	
	v.x	+= dv_x;
	v.y += dv_y;
	v.z += dv_z;					
	imat.transform(v);
	ois.getOrigin().move(ds_x*ONE_METER, ds_y*ONE_METER, ds_z*ONE_METER);	
  }      
  
  
//  public void simulate(OIS3D ois, float secPassed){
//	Matrix3D mat = ois.getMatrix();		
//	Vector3D v = mat.transform( ois.getVelocityVector() );	
//	float ds_x = v.x * secPassed;
//	float ds_y = v.y * secPassed;
//	float ds_z = v.z * secPassed;	
//    mat.getInverse().transform(v);	
//	ois.getOrigin().move(ds_x*ONE_METER, ds_y*ONE_METER, ds_z*ONE_METER);	
//  }
  
    
  private float gravity;  
  private Vector3D gravityVec;

  public static int ONE_METER = 100;
  public static final float EARTH_G = 9.80665f;
  public static final float MOON_G  = EARTH_G / 6f;
  public static final float MARS_G  = EARTH_G / 3f;
  
}
