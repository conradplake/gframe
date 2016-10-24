package gframe.engine;


/**
 * This class represents an origin (lightsource) and a direction (matrix).
 * It's actually called a spotlight :/
 * */
public class DirectionalLight{
	
	private Lightsource lightsource;
	private Matrix3D matrix;
	private Matrix3D inverseMatrix;
	private Vector3D light_z;
	
	private ZBuffer depthMap;
	private boolean recomputeDepthMap = true;
	private boolean isSpotLight = true;
	
	public DirectionalLight(Lightsource lightsource){
		this.lightsource = lightsource;
		this.matrix = new Matrix3D();
		this.inverseMatrix = matrix.getInverse();
		this.light_z = matrix.getZVector();
	}

	public Lightsource getLightsource(){
		return lightsource;
	}

	public Point3D getOrigin() {
		return lightsource;
	}
	
	public Matrix3D getInverseMatrix(){
		return inverseMatrix;
	}
	
	public void recomputeInverse(){
		this.inverseMatrix = this.matrix.getInverse();
	}
	
	public Matrix3D getMatrix(){
		return matrix;
	}

	public Vector3D getZVector() {
		return light_z;
	}

	
	public void move(float dx, float dy, float dz){
		lightsource.move(dx, dy, dz);
		recomputeDepthMap = true;
	}
	
	
	public void rotate(float deg_x, float deg_y, float deg_z) {
		matrix.rotate(deg_x, deg_y, deg_z);
		inverseMatrix = matrix.getInverse();
		light_z = matrix.getZVector();
		recomputeDepthMap = true;
	}
	
	public boolean isRecomputeDepthMap(){
		return this.recomputeDepthMap;
	}
	
	public void setDepthMap(ZBuffer depthMap){
		this.depthMap = depthMap;
		this.recomputeDepthMap = false;
	}
	
	public ZBuffer getDepthMap(){
		return this.depthMap;
	}

	public void recomputeDepthMap() {
		recomputeDepthMap = true;
	}
	
	public boolean isSpotLight(){
		return isSpotLight;
	}
	
	public void setIsSpotLight(boolean value){
		this.isSpotLight = value;
	}
}
