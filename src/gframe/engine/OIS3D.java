package gframe.engine;


/**
 * An object in 3D space.
 * */
public interface OIS3D{
  public Point3D getOrigin();  
  public Matrix3D getMatrix();
  public Vector3D getVelocityVector();
}
