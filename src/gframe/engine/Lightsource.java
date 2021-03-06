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
	
	
	public float getIntensity(float world_x, float world_y, float world_z) {
		return intensity * (addAttenuation? computeLightAttenuation(world_x, world_y, world_z) : 1f);
	}

	public void setIntensity(float i) {
		intensity = i;
	}

	
	public float getLightAttenuationFalloffFactor() {
		return lightAttenuationFalloffFactor;
	}

	public void setLightAttenuationFalloffFactor(float lightAttenuationFalloffFactor) {
		this.lightAttenuationFalloffFactor = lightAttenuationFalloffFactor;
	}
	
	
	public Color getColor() {
		return col;
	}

	public void setColor(Color c) {
		col = c;
		col.getRGBComponents(rgbComponents);
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

	public void setShadowsEnabled(boolean b){		
		this.setShadowsEnabled = b;
	}
	
	public boolean isShadowsEnabled(){
		return this.setShadowsEnabled;
	}
	
	public Point3D move(float dx, float dy, float dz){
		super.move(dx, dy, dz);
		recomputeDepthMap = true;
		return this;
	}
	
	
	public void rotate(float deg_x, float deg_y, float deg_z) {
		matrix.rotate(deg_x, deg_y, deg_z);
		inverseMatrix = matrix.getInverse();
		light_z = matrix.getZVector();
		recomputeDepthMap = true;
	}
	
	public void setMatrix(Matrix3D m) {
		matrix.setXAxis(m.getXVector());
		matrix.setYAxis(m.getYVector());
		matrix.setZAxis(m.getZVector());
		
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
	
	public void recomputeLightZ(){
		this.light_z= matrix.getZVector();
	}	
	
	public float computeLightAttenuation(float world_x, float world_y, float world_z){
		if(isDirectional==false){
			float d2 = distanceSquared(world_x, world_y, world_z);
			return Toolbox.clamp(1/(lightAttenuationFalloffFactor*d2), 0f, 1f);
		}else{
			return 1f;
		}
	}
	
	
	public boolean isDirectional(){
		return isDirectional;
	}
	
	
	public void setIsDirectional(boolean value){
		isDirectional = value;
	}
	
	public boolean isSpotLight(){
		return isSpotLight;
	}
	
	public void setIsSpotLight(boolean value){
		this.isSpotLight = value;
	}
	
	public boolean isAddAttenuation() {
		return addAttenuation;
	}

	public void setAddAttenuation(boolean addAttenuation) {
		this.addAttenuation = addAttenuation;
	}
	

	public static float AMBIENT_LIGHT_INTENSITY = 0.2f;
	
	public static final float MIN_INTENSITY = 0.01f;
	public static final float NORM_INTENSITY = 0.66f;
	public static final float MAX_INTENSITY = 1f;
	
	private boolean addAttenuation = false;
	private float lightAttenuationFalloffFactor = 0.000001f;
	
	private float intensity;
	private Color col;
	
	protected float[] rgbComponents;
	
	
	private Matrix3D matrix;
	private Matrix3D inverseMatrix;
	private Vector3D light_z;
	
	private ZBuffer depthMap;
	private boolean recomputeDepthMap = true;
	private boolean isSpotLight = true;
	private boolean isDirectional = false;
	
	private boolean setShadowsEnabled = false;
}