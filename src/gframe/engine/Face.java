package gframe.engine;

import java.awt.Color;

public class Face {
	
	public Face(Point3D p1, Point3D p2, Point3D p3, Color col) {		
		this(new Point3D[]{p1,p2,p3}, col);		
	}
	
	public Face(Point3D p1, Point3D p2, Point3D p3, Point3D p4, Color col) {		
		this(new Point3D[]{p1,p2,p3,p4}, col);		
	}			
	
	public Face(Point3D[] vertices, Color col) {
		this(vertices, vertices.length, col);
	}
	
	public Face(Point3D[] vertices, int length, Color col) {
		this.vertices = new Point3D[length];						
		for (int i=0;i<length;i++) {
			this.vertices[i] = vertices[i]; 			
		}
		
		recompute();
		
		this.col = col;
	}
	
	
	public Face(Point3D[] vertices, int length, Point3D centroid, float normal_x, float normal_y, float normal_z, Color col) {
		this.vertices = new Point3D[length];						
		for (int i=0;i<length;i++) {
			this.vertices[i] = vertices[i]; 			
		}
		
//		recompute();
		this.centroid = centroid;
		
		this.normal_x = normal_x;
		this.normal_y = normal_y;
		this.normal_z = normal_z;
		
		this.col = col;
	}
	
	protected Face(Point3D[] vertices, int length, Point3D centroid, Color col, float normal_x, float normal_y, float normal_z) {
		this.vertices = new Point3D[length];					
		for (int i=0;i<length;i++) {
			this.vertices[i] = vertices[i]; 			
		}
		this.centroid = centroid;
		this.col = col;
		this.normal_x = normal_x;
		this.normal_y = normal_y;
		this.normal_z = normal_z;
	}
	
	public void transform(Matrix3D matrix) {		
		for(int i=0;i<vertices.length;i++){
			matrix.transform(vertices[i]);
		}
		matrix.transform(centroid);
		
		float[] newNormal = matrix.transform(normal_x, normal_y, normal_z);
		normal_x = newNormal[0];
		normal_y = newNormal[1];
		normal_z = newNormal[2];
	}
	
	/**
	 * Does the transform but also moves all vertices by p
	 * */
	public void transform(Matrix3D matrix, Point3D p) {		
		for(int i=0;i<vertices.length;i++){
			matrix.transform(vertices[i]);
			vertices[i].add(p);
		}
		matrix.transform(centroid);
		centroid.add(p);
		
		float[] newNormal = matrix.transform(normal_x, normal_y, normal_z);
		normal_x = newNormal[0];
		normal_y = newNormal[1];
		normal_z = newNormal[2];
	}
	

	public float minX() {		
		float minx = vertices[0].x;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].x < minx){
				minx = vertices[i].x;
			}
		}							
		return minx;
	}

	public float maxX() {
		float maxx = vertices[0].x;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].x > maxx){
				maxx = vertices[i].x;
			}
		}							
		return maxx;
	}
	
	public float minY() {		
		float miny = vertices[0].y;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].y < miny){
				miny = vertices[i].y;
			}
		}							
		return miny;
	}

	public float maxY() {
		float maxy = vertices[0].y;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].y > maxy){
				maxy = vertices[i].y;
			}
		}							
		return maxy;
	}

	public float minZ() {
		float minz = vertices[0].z;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].z < minz){
				minz = vertices[i].z;
			}
		}							
		return minz;
	}

	public float maxZ() {
		float maxz = vertices[0].z;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].z > maxz){
				maxz = vertices[i].z;
			}
		}							
		return maxz;
	}
	
	public Point3D getMaxZVertex(){
		Point3D result = vertices[0];
		float maxz = vertices[0].z;
		for(int i=1;i<vertices.length;i++){
			if(vertices[i].z > maxz){
				maxz = vertices[i].z;
				result = vertices[i];
			}
		}							
		return result;
	}
	
	
	/**
	 * Adds position of p to all vertices and centroid. 
	 * */
	public void add(Point3D p) {		
		for(int i=0;i<vertices.length;i++){
			vertices[i].add(p);		
		}
		centroid.add(p);
	}	

	public void move(float x, float y, float z) {
		for(int i=0;i<vertices.length;i++){
			vertices[i].move(x, y, z);
		}
		centroid.move(x, y, z);
				
	}

	
	/**
	 * Recompute centroid and normal vector
	 * */
	public void recompute(){
		this.centroid = computeCentroid();
		
		Vector3D nvec = new Vector3D(0, 0, 0);
		Vector3D vec12 = new Vector3D(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
		Vector3D vec13 = new Vector3D(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
		nvec.x = vec12.y * vec13.z - vec13.y * vec12.z;
		nvec.y = vec12.z * vec13.x - vec13.z * vec12.x;
		nvec.z = vec12.x * vec13.y - vec13.x * vec12.y;
		nvec.normalize();	
				
		this.normal_x = nvec.x;
		this.normal_y = nvec.y;
		this.normal_z = nvec.z;
	}
	
	
	private Point3D computeCentroid() {		
		float mx = minX();
		float my = minY();
		float mz = minZ();
		return new Point3D(mx + (maxX() - mx) / 2f, my + (maxY() - my) / 2f, mz + (maxZ() - mz) / 2f);			
	}

	
	
	public RenderFace createRenderFace(){
		return createRenderFace(Material.DEFAULT);
	}
	
	/**
	 * 
	 * Creates a representative of this face used for rendering (see Engine3D.fillPolyBuffers).
	 * @param shininess 
	 * @param diffuseCoefficient 
	 * @param abientCoefficient 
	 * */
	public RenderFace createRenderFace(Material material){
		
		if(renderFace==null){		
			Point3D[] copyVertices = new Point3D[vertices.length];
			for(int i=0;i<vertices.length;i++){
				copyVertices[i]=vertices[i].copy();
			}		
			renderFace = new RenderFace(copyVertices, copyVertices.length, centroid.copy(), col, normal_x, normal_y, normal_z);			
			renderFace.material = material;
		}
		else{
			renderFace.reset(this);
		}
		
		return renderFace;
	}
	
	
	public Point3D[] getVertices() {
		return vertices;
	}
	
	
	public Vector3D getNormal() {
		return new Vector3D(normal_x, normal_y, normal_z);
	}
	
	
	public Point3D getCentroid() {
		return centroid;
	}
	
	
	public Color getColor(){
		return col;
	}
	
	public void setColor(Color c){
		this.col = c;
	}
	

	Color col;

	Point3D[] vertices;
	
	Point3D centroid;
	
	public float normal_x, normal_y, normal_z;
	
	RenderFace renderFace;
	
}