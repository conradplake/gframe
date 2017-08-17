package gframe.engine;

/**
 * A point in 3d space.
 * 
 * A point also has a normal vector attached which itself can point in any direction.
 * This is used for computing light over a polygon.
 *  
 * */
public class Point3D {

	public Point3D() {
		this(0, 0, 0);
	}

	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(float x, float y, float z, float normal_x, float normal_y, float normal_z) {
		this(x, y, z, normal_x, normal_y, normal_z, 0f, 0f);
	}
	
	public Point3D(float x, float y, float z, float normal_x, float normal_y, float normal_z, float u, float v) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.normal_x = normal_x;
		this.normal_y = normal_y;
		this.normal_z = normal_z;
		
		this.u = u;
		this.v = v;
	}

	public float distance(Point3D p) {
		return (float) Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z));
	}

	public float distanceSquared(Point3D p) {
		return ((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y) + (p.z - z) * (p.z - z));
	}

	public float distance(float px, float py, float pz) {
		return (float) Math.sqrt((px - x) * (px - x) + (py - y) * (py - y) + (pz - z) * (pz - z));
	}

	public void move(float dx, float dy, float dz) {
		x += dx;
		y += dy;
		z += dz;
	}
	
	
	public Point3D subtract(Point3D p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
		return this;
	}

	public Point3D add(Point3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
		return this;
	}

	public void setCoordinates(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setNormalVector(Vector3D normalVector){
		this.normal_x = normalVector.x;
		this.normal_y = normalVector.y;
		this.normal_z = normalVector.z;
	}
	
	public void setNormalVector(float nx, float ny, float nz){
		this.normal_x = nx;
		this.normal_y = ny;
		this.normal_z = nz;
	}

	
	public void lerp(Point3D targetPosition, float lerpFactor) {		
		float move_dx = (targetPosition.x-this.x)*lerpFactor;
		float move_dy = (targetPosition.y-this.y)*lerpFactor;		
		float move_dz = (targetPosition.z-this.z)*lerpFactor;		
		this.move(move_dx, move_dy, move_dz);
		
		// Also lerp normals and u,v-coordinates
		this.normal_x += (targetPosition.normal_x-this.normal_x)*lerpFactor;
		this.normal_y += (targetPosition.normal_y-this.normal_y)*lerpFactor;
		this.normal_z += (targetPosition.normal_z-this.normal_z)*lerpFactor;

		this.u += (targetPosition.u-this.u)*lerpFactor;
		this.v += (targetPosition.v-this.v)*lerpFactor;
	}
	
	public Point3D copy() {
		return (new Point3D(x, y, z, normal_x, normal_y, normal_z, u, v));
	}
		

	@Override
	public String toString() {
		return "[" + x + "," + y + "," + z + "], vn: ["+normal_x+","+normal_y+","+normal_z+"], uv: ["+u+","+v+"]";
	}

	
	public static float distance(float px1, float py1, float pz1, float px2, float py2, float pz2) {
		return (float) Math.sqrt((px2 - px1) * (px2 - px1) + (py2 - py1) * (py2 - py1) + (pz2 - pz1) * (pz2 - pz1));
	}
	

	public float x, y, z;

	public float normal_x, normal_y, normal_z;
	
	// texture map coordinates in [0..1]
	float u, v;

}