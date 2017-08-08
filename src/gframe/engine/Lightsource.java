package gframe.engine;

import java.awt.Color;

public class Lightsource extends Point3D {
	
	public Lightsource(float x, float y, float z, Color col, float intensity) {
		super(x, y, z);
		setColor(col);
		setIntensity(intensity);
		rgbComponents = col.getRGBComponents(new float[4]);
		
		this.matrix = new Matrix3D();
		this.inverseMatrix = matrix.getInverse();
		this.light_z = matrix.getZVector();
	}

	public Lightsource(Point3D origin, Color col, float intensity) {
		super(origin.x, origin.y, origin.z);
		setColor(col);
		setIntensity(intensity);
		
		rgbComponents = col.getRGBComponents(new float[4]);
		
		this.matrix = new Matrix3D();
		this.inverseMatrix = matrix.getInverse();
		this.light_z = matrix.getZVector();
	}


	public float getIntensity() {
		return intensity;
	}

	public void setIntensity(float i) {
		intensity = i;
	}

	public Color getColor() {
		return col;
	}

	public void setColor(Color c) {
		col = c;
		col.getRGBComponents(rgbComponents);
	}
	
//	public Lightsource copy(){
//		return new Lightsource(super.copy(), col, intensity);
//	}
	
	
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

	public void setShadowsEnabled(boolean b){		
		this.setShadowsEnabled = b;
	}
	
	public boolean isShadowsEnabled(){
		return this.setShadowsEnabled;
	}
	
	public void move(float dx, float dy, float dz){
		super.move(dx, dy, dz);
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
	

	public static final float AMBIENT_LIGHT_INTENSITY = 0.2f;
	
	public static final float MIN_INTENSITY = 0.01f;
	public static final float NORM_INTENSITY = 0.66f;
	public static final float MAX_INTENSITY = 1f;

	float intensity;
	Color col;
	float[] rgbComponents;
	
	
	private Matrix3D matrix;
	private Matrix3D inverseMatrix;
	private Vector3D light_z;
	
	private ZBuffer depthMap;
	private boolean recomputeDepthMap = true;
	private boolean isSpotLight = true;
	
	private boolean setShadowsEnabled = false;
}