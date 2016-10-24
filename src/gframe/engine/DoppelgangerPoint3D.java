package gframe.engine;

public class DoppelgangerPoint3D extends Point3D{
  
  public DoppelgangerPoint3D(float x, float y, float z){
    super(x,y,z);
	setDoppelganger( new Point3D(x,y,z) );
  }
  
  public DoppelgangerPoint3D(float x1, float y1, float z1, float x2, float y2, float z2){
  	super(x1,y1,z1);  
	setDoppelganger( new Point3D(x2,y2,z2) );
  }
  
  public void setDoppelganger(Point3D doppelganger){
    this.doppelganger  = doppelganger;
  }
  
  public Point3D getDoppelganger(){
    return doppelganger;
  }
  
  @Override
  public void move(float dx, float dy, float dz){
    super.move(dx,dy,dz);
    doppelganger.move(dx,dy,dz);
  }
  
  @Override
  public void add(Point3D p){
    super.add(p);
	doppelganger.add(p);
  }

  
  private Point3D doppelganger;  
}