package gframe.engine;


/**
 * A vector in 3d space.
 * 
 * */
public class Vector3D {

	public Vector3D() {

	}

	public Vector3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(Point3D to) {
		this.x = to.x;
		this.y = to.y;
		this.z = to.z;
	}

	public Vector3D(Point3D from, Point3D to) {
		this.x = to.x - from.x;
		this.y = to.y - from.y;
		this.z = to.z - from.z;
	}

	public float length() {
		return (float) (Math.sqrt(x * x + y * y + z * z));
	}
	
	public float manhattanLength() {
		return Math.abs(x)+Math.abs(y)+Math.abs(z);
	}

	public float dotProduct(Vector3D anotherVec) {
		return (x * anotherVec.x + y * anotherVec.y + z * anotherVec.z);
	}
	
	public float dotProduct(float x, float y, float z) {
		return (this.x * x + this.y * y + this.z * z);
	}

	public void normalize() {
		float len = length();
		if (len > 0) {
			len = 1/len;
			x *= len;
			y *= len;
			z *= len;
		}
	}

	public void subtract(Vector3D v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	public void add(Vector3D v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public Vector3D scale(float fac) {
		x *= fac;
		y *= fac;
		z *= fac;
		return this;
	}

	public float getZ() {
		return z;
	}

	public float getY() {
		return y;
	}

	public float getX() {
		return x;
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	
	public void lerp(Vector3D target, float lerpFactor){
		float move_dx = (target.x-x)*lerpFactor;
		float move_dy = (target.y-y)*lerpFactor;		
		float move_dz = (target.z-z)*lerpFactor;		
		this.x+=move_dx;
		this.y+=move_dy;
		this.z+=move_dz;
	}
	
	
	public Vector3D getReflectionVector(Vector3D normal){
		float dotProduct = this.dotProduct(normal);
		
		float rx = this.x - 2 * dotProduct * normal.x;
		float ry = this.y - 2 * dotProduct * normal.y;
		float rz = this.z - 2 * dotProduct * normal.z;
		
		return new Vector3D(rx, ry, rz);
	}
	
	
	public Vector3D crossProduct(Vector3D v) {
		float r_x = (v.y*z) - (v.z*y);
		float r_y = (v.z*x) - (v.x*z);
		float r_z = (v.x*y) - (v.y*x);
		return new Vector3D(r_x, r_y, r_z);
	}

	public Vector3D copy() {
		return new Vector3D(x, y, z);
	}
	
	
	@Override
	public String toString() {
		return "[" + x + "," + y + "," + z + "]";
	}
	
	public static float[] normalize(float x, float y, float z){		
		float len = (float) (Math.sqrt(x * x + y * y + z * z));
		if(len>0){
			len = 1/len;
			x*=len;
			y*=len;
			z*=len;
		}
		return new float[]{x,y,z};
	}

	public float x;
	public float y;
	public float z;	
}