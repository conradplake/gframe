package gframe;

public interface OIS3D{    
  public gframe.engine.Point3D getOrigin();  
  public gframe.engine.Matrix3D getMatrix();
  public gframe.engine.Vector3D getVelocityVector();
}
