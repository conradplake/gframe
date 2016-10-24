package gframe.engine;


/**
 * A matrix of 3x3 vectors in 3d space.
 * 
 * */
public class Matrix3D {

	public Matrix3D() {
		array = new float[3][3];
		reset();
	}

	public void apply(float[][] mat) {
		array[0][0] = mat[0][0];
		array[0][1] = mat[0][1];
		array[0][2] = mat[0][2];
		array[1][0] = mat[1][0];
		array[1][1] = mat[1][1];
		array[1][2] = mat[1][2];
		array[2][0] = mat[2][0];
		array[2][1] = mat[2][1];
		array[2][2] = mat[2][2];
	}

	public float[][] getArray() {
		return array;
	}

	public void rotate(float deg_x, float deg_y, float deg_z) {
		if (deg_x != 0) {
			transform(Toolbox.getXrotMatrixArray(deg_x));
		}
		if (deg_y != 0) {
			transform(Toolbox.getYrotMatrixArray(deg_y));
		}
		if (deg_z != 0) {
			transform(Toolbox.getZrotMatrixArray(deg_z));
		}
	}

	public void transform(Matrix3D tmat) {
		transform(tmat.getArray());
	}

	public void transform(float[][] tmat) {
		float[][] tmpArr = new float[3][3];

		tmpArr[0][0] = tmat[0][0] * array[0][0] + tmat[0][1] * array[1][0] + tmat[0][2] * array[2][0];
		tmpArr[0][1] = tmat[0][0] * array[0][1] + tmat[0][1] * array[1][1] + tmat[0][2] * array[2][1];
		tmpArr[0][2] = tmat[0][0] * array[0][2] + tmat[0][1] * array[1][2] + tmat[0][2] * array[2][2];

		tmpArr[1][0] = tmat[1][0] * array[0][0] + tmat[1][1] * array[1][0] + tmat[1][2] * array[2][0];
		tmpArr[1][1] = tmat[1][0] * array[0][1] + tmat[1][1] * array[1][1] + tmat[1][2] * array[2][1];
		tmpArr[1][2] = tmat[1][0] * array[0][2] + tmat[1][1] * array[1][2] + tmat[1][2] * array[2][2];

		tmpArr[2][0] = tmat[2][0] * array[0][0] + tmat[2][1] * array[1][0] + tmat[2][2] * array[2][0];
		tmpArr[2][1] = tmat[2][0] * array[0][1] + tmat[2][1] * array[1][1] + tmat[2][2] * array[2][1];
		tmpArr[2][2] = tmat[2][0] * array[0][2] + tmat[2][1] * array[1][2] + tmat[2][2] * array[2][2];

		array = tmpArr;
	}

	public Matrix3D getTransformed(Matrix3D tmat) {
		return getTransformed(tmat.getArray());
	}

	public Matrix3D getTransformed(float[][] tmat) {
		Matrix3D resultMat = new Matrix3D();
		float[][] tmpArr = new float[3][3];

		tmpArr[0][0] = tmat[0][0] * array[0][0] + tmat[0][1] * array[1][0] + tmat[0][2] * array[2][0];
		tmpArr[0][1] = tmat[0][0] * array[0][1] + tmat[0][1] * array[1][1] + tmat[0][2] * array[2][1];
		tmpArr[0][2] = tmat[0][0] * array[0][2] + tmat[0][1] * array[1][2] + tmat[0][2] * array[2][2];

		tmpArr[1][0] = tmat[1][0] * array[0][0] + tmat[1][1] * array[1][0] + tmat[1][2] * array[2][0];
		tmpArr[1][1] = tmat[1][0] * array[0][1] + tmat[1][1] * array[1][1] + tmat[1][2] * array[2][1];
		tmpArr[1][2] = tmat[1][0] * array[0][2] + tmat[1][1] * array[1][2] + tmat[1][2] * array[2][2];

		tmpArr[2][0] = tmat[2][0] * array[0][0] + tmat[2][1] * array[1][0] + tmat[2][2] * array[2][0];
		tmpArr[2][1] = tmat[2][0] * array[0][1] + tmat[2][1] * array[1][1] + tmat[2][2] * array[2][1];
		tmpArr[2][2] = tmat[2][0] * array[0][2] + tmat[2][1] * array[1][2] + tmat[2][2] * array[2][2];

		resultMat.apply(tmpArr);
		return resultMat;
	}

	public Point3D transform(Point3D p) {
		float x = p.x * array[0][0] + p.y * array[1][0] + p.z * array[2][0];
		float y = p.x * array[0][1] + p.y * array[1][1] + p.z * array[2][1];
		float z = p.x * array[0][2] + p.y * array[1][2] + p.z * array[2][2];
		p.x = x;
		p.y = y;
		p.z = z;
		
		// same for p's normal vector..
		float nx = p.normal_x * array[0][0] + p.normal_y * array[1][0] + p.normal_z * array[2][0];
		float ny = p.normal_x * array[0][1] + p.normal_y * array[1][1] + p.normal_z * array[2][1];
		float nz = p.normal_x * array[0][2] + p.normal_y * array[1][2] + p.normal_z * array[2][2];
		p.normal_x = nx;
		p.normal_y = ny;
		p.normal_z = nz;
		return p;
	}

	public float[] transform(float vec_x, float vec_y, float vec_z) {
		float new_x = vec_x * array[0][0] + vec_y * array[1][0] + vec_z * array[2][0];
		float new_y = vec_x * array[0][1] + vec_y * array[1][1] + vec_z * array[2][1];
		float new_z = vec_x * array[0][2] + vec_y * array[1][2] + vec_z * array[2][2];		
		return new float[]{new_x, new_y, new_z};
	}
	
	/**
	 * 
	 * Returns only the transform of z-component (vec_z). Useful if only the distance is of concern (e.g. in backface culling).
	 * **/
	public float transformZ(float vec_x, float vec_y, float vec_z){
		return vec_x * array[0][2] + vec_y * array[1][2] + vec_z * array[2][2];
	}

	public Vector3D transform(Vector3D vec) {
		float x = vec.x * array[0][0] + vec.y * array[1][0] + vec.z * array[2][0];
		float y = vec.x * array[0][1] + vec.y * array[1][1] + vec.z * array[2][1];
		float z = vec.x * array[0][2] + vec.y * array[1][2] + vec.z * array[2][2];
		vec.x = x;
		vec.y = y;
		vec.z = z;
		return vec;
	}

	public Matrix3D getInverse() {
		float[][] iarr = new float[3][3];
		iarr[0][0] = array[0][0];
		iarr[0][1] = array[1][0];
		iarr[0][2] = array[2][0];
		iarr[1][0] = array[0][1];
		iarr[1][1] = array[1][1];
		iarr[1][2] = array[2][1];
		iarr[2][0] = array[0][2];
		iarr[2][1] = array[1][2];
		iarr[2][2] = array[2][2];
		Matrix3D imat = new Matrix3D();
		imat.apply(iarr);
		return imat;
	}

	public void reset() {
		array[0][0] = 1;
		array[0][1] = 0;
		array[0][2] = 0;
		array[1][0] = 0;
		array[1][1] = 1;
		array[1][2] = 0;
		array[2][0] = 0;
		array[2][1] = 0;
		array[2][2] = 1;
	}

	public Vector3D getXVector() {
		return new Vector3D(array[0][0], array[0][1], array[0][2]);
	}

	public Vector3D getYVector() {
		return new Vector3D(array[1][0], array[1][1], array[1][2]);
	}

	public Vector3D getZVector() {
		return new Vector3D(array[2][0], array[2][1], array[2][2]);
	}

	public void setXAxis(float x, float y, float z) {
		array[0][0] = x;
		array[0][1] = y;
		array[0][2] = z;
	}

	public void setXAxis(Vector3D x) {
		array[0][0] = x.x;
		array[0][1] = x.y;
		array[0][2] = x.z;
	}

	public void setYAxis(float x, float y, float z) {
		array[1][0] = x;
		array[1][1] = y;
		array[1][2] = z;
	}

	public void setYAxis(Vector3D y) {
		array[1][0] = y.x;
		array[1][1] = y.y;
		array[1][2] = y.z;
	}

	public void setZAxis(float x, float y, float z) {
		array[2][0] = x;
		array[2][1] = y;
		array[2][2] = z;
	}

	public void setZAxis(Vector3D z) {
		array[2][0] = z.x;
		array[2][1] = z.y;
		array[2][2] = z.z;
	}

	public void lerp(Matrix3D targetMatrix, float lerpFactor) {
		// drei vektor lerps: x, y, z axis
		Vector3D xVector = getXVector();
		xVector.lerp(targetMatrix.getXVector(), lerpFactor);		
		setXAxis(xVector);
		
		Vector3D yVector = getYVector();
		yVector.lerp(targetMatrix.getYVector(), lerpFactor);		
		setYAxis(yVector);
		
		Vector3D zVector = getZVector();
		zVector.lerp(targetMatrix.getZVector(), lerpFactor);		
		setZAxis(zVector);			
	}
	
	@Override
	public String toString(){
		//return "[xx="+array[0][0]+",xy="+array[0][1]+",xz="+array[0][2]+", yx="+array[1][0]+",yy="+array[1][1]+",yz="+array[1][2]+", zx="+array[2][0]+",zy="+array[2][1]+",zz="+array[2][2]+"]";
		return "["+array[0][0]+"\t"+array[0][1]+"\t"+array[0][2]+"\t"+array[1][0]+"\t"+array[1][1]+"\t"+array[1][2]+"\t"+array[2][0]+"\t"+array[2][1]+"\t"+array[2][2]+"]";
	}
	
	private float[][] array;
	
}
