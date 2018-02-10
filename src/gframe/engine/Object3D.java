package gframe.engine;

import java.util.LinkedList;
import java.util.List;

public class Object3D {

	public Object3D() {
		origin = new Point3D(0, 0, 0);
		matrix = new Matrix3D();
		children = new LinkedList();
	}

	public Object3D(Point3D origin) {
		this.origin = origin;
		matrix = new Matrix3D();
		children = new LinkedList();
	}

	public void setOrigin(Point3D origin) {
		this.origin = origin;
	}

	public Point3D getOrigin() {
		return origin;
	}

	public void setMatrix(Matrix3D matrix) {
		this.matrix = matrix;
	}

	public Matrix3D getMatrix() {
		return matrix;
	}

	public void reset() {
		matrix.reset();
	}

	public void move(Point3D dest) {
		origin.move(dest.x - origin.x, dest.y - origin.y, dest.z - origin.z);
	}

	public void move(float units) {
		Vector3D z = matrix.getZVector(); // move along z axis
		origin.move(z.x * units, z.y * units, z.z * units);
	}
	
	
	/**
	 * Only moves the origin along x and z direction relative to object space.
	 * Useful for FPS-Walking mechanics :)
	 * */
	public void moveXZ(float units) {
		Vector3D z = getMatrix().getZVector(); // move along z axis
		getOrigin().move(z.x * units, 0, z.z * units);
	}

	public void move(float dx, float dy, float dz) {
		Point3D ttarget = matrix.transform(new Point3D(dx, dy, dz));
		origin.move(ttarget.x, ttarget.y, ttarget.z);
	}

	public void focus(Point3D attpoint) {
		Vector3D worldvec = new Vector3D(origin, attpoint);
		Vector3D dir = matrix.getInverse().transform(worldvec.copy());
		if (dir.getZ() < 0) {
			rotate(0, 180, 0);
			dir = matrix.getInverse().transform(worldvec);
		}
		float dir_x = dir.getX();
		float dir_z = dir.getZ();
		if (dir_x != 0) {
			float dist = (float) Math.sqrt(dir_x * dir_x + dir_z * dir_z);
			float phi = (float) Math.acos(dir_z / dist);
			phi = Toolbox.radiantToDegree(phi);
			if (dir_x > 0) {
				phi = -phi;
			}
			rotate(0, (int) phi, 0);
			dir.setZ(dist); // update direction vector
			dir.setX(0);
		}

		float dir_y = dir.getY();
		dir_z = dir.getZ();
		if (dir_y != 0) {
			float phi = (float) Math.acos(dir_z / (float) Math.sqrt(dir_y * dir_y + dir_z * dir_z));
			phi = Toolbox.radiantToDegree(phi);
			if (dir_y < 0) {
				phi = -phi;
			}
			rotate((int) phi, 0, 0);
		}
	}

	public void rotate(float deg_x, float deg_y, float deg_z) {
		matrix.rotate(deg_x, deg_y, deg_z);
	}

	public void rotate(Point3D rp, float deg_x, float deg_y, float deg_z) {
		Point3D newOrigin = origin.copy();
		if (deg_x != 0) {
			Matrix3D xmat = Toolbox.getXrotMatrix(deg_x);
			newOrigin.x -= rp.x;
			newOrigin.y -= rp.y;
			newOrigin.z -= rp.z;
			xmat.transform(newOrigin);
			newOrigin.add(rp);
			xmat.transform(matrix);
			matrix = xmat;
		}
		if (deg_y != 0) {
			Matrix3D ymat = Toolbox.getYrotMatrix(deg_y);
			newOrigin.x -= rp.x;
			newOrigin.y -= rp.y;
			newOrigin.z -= rp.z;
			ymat.transform(newOrigin);
			newOrigin.add(rp);
			ymat.transform(matrix);
			matrix = ymat;
		}
		if (deg_z != 0) {
			Matrix3D zmat = Toolbox.getZrotMatrix(deg_z);
			newOrigin.x -= rp.x;
			newOrigin.y -= rp.y;
			newOrigin.z -= rp.z;
			zmat.transform(newOrigin);
			newOrigin.add(rp);
			zmat.transform(matrix);
			matrix = zmat;
		}
		move(newOrigin);
	}

	public Vector3D getXVector() {
		return matrix.getXVector();
	}

	public Vector3D getYVector() {
		return matrix.getYVector();
	}

	public Vector3D getZVector() {
		return matrix.getZVector();
	}

	public boolean hasChildren() {
		return (children.size() > 0);
	}

	public boolean hasParent() {
		return (parent != null);
	}

	public void setParent(Object3D parent) {
		this.parent = parent;
	}

	public Object3D getParent() {
		return parent;
	}

	public void addChild(Object3D obj) {
		children.add(obj);
		obj.setParent(this);
	}

	public void removeChild(Object3D obj) {
		if (children.remove(obj)) {
			obj.setParent(null);
		}
	}

	public List getChildren() {
		return children;
	}

	private Point3D origin;
	private Matrix3D matrix;

	private List<Object3D> children;

	private Object3D parent;
}
