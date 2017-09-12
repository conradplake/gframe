package gframe.engine;

import java.awt.Color;

/**
 * The representative of a face with all information at hand needed for
 * rendering. An instance of this class is created from a face object via method
 * Face.createRenderFace
 * 
 * TODO inheritance from face is necessary here?
 */
public class RenderFace extends Face implements Comparable<RenderFace> {

	public float[] cam_X;
	public float[] cam_Y;
	public float[] cam_Z;

	// perspektivische weltkoordinaten; beim rendern wieder durch zFactor
	// dividieren um die korrigierten koordinaten zu erhalten; z.B. f�r shadow
	// mapping ben�tigt
	float[] pcorrectedWorld_X;
	float[] pcorrectedWorld_Y;
	float[] pcorrectedWorld_Z;

	float[] zFactors;

	float min_x;
	float min_y;
	float min_z;
	float max_x;
	float max_y;
	float max_z;

	int screen_bbox_minx;
	int screen_bbox_maxx;
	int screen_bbox_miny;
	int screen_bbox_maxy;

	// relative texture coordinates
	float[] texel_U;
	float[] texel_V;

	Material material;

	Matrix3D inverseTangentSpace;
	Matrix3D tangentSpace;

	Shader shader;

	Point3D cameraPosition;

	protected RenderFace(Point3D[] vertices, int length, Point3D centroid, Color col, float normal_x, float normal_y,
			float normal_z) {
		super(vertices, length, centroid, col, normal_x, normal_y, normal_z);

		cam_X = new float[length];
		cam_Y = new float[length];
		cam_Z = new float[length];

		pcorrectedWorld_X = new float[length];
		pcorrectedWorld_Y = new float[length];
		pcorrectedWorld_Z = new float[length];

		texel_U = new float[length];
		texel_V = new float[length];

		zFactors = new float[length];
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}

	public Shader getShader() {
		return shader;
	}

	/**
	 * 
	 * Setup cam-space according to the specified camera position
	 */
	public void transformToCamSpace(Point3D camOrigin, Matrix3D icammat) {
		this.transformToCamSpace(camOrigin, icammat, true);
	}

	/**
	 * 
	 * Setup cam-space according to the specified camera position
	 */
	public void transformToCamSpace(Point3D camOrigin, Matrix3D icammat, boolean perspectiveCorrect) {

		min_x = Integer.MAX_VALUE;
		min_y = Integer.MAX_VALUE;
		min_z = Integer.MAX_VALUE;
		max_x = -Integer.MAX_VALUE;
		max_y = -Integer.MAX_VALUE;
		max_z = -Integer.MAX_VALUE;

		screen_bbox_minx = -1;
		screen_bbox_maxx = -1;
		screen_bbox_miny = -1;
		screen_bbox_maxy = -1;

		this.cameraPosition = camOrigin;

		for (int i = 0; i < vertices.length; i++) {
			float[] camCoords = icammat.transform(vertices[i].x - camOrigin.x, vertices[i].y - camOrigin.y,
					vertices[i].z - camOrigin.z);

			cam_Z[i] = camCoords[2];

			float zf = 1f;
			if (perspectiveCorrect) {
				zf = Engine3D.zFactor(cam_Z[i]);
				// zf = (float)Toolbox.map(zf, 0, 10000, 0, 800);
			}

			zFactors[i] = zf;
			cam_X[i] = camCoords[0] * zf;
			cam_Y[i] = camCoords[1] * zf;

			pcorrectedWorld_X[i] = vertices[i].x * zf;
			pcorrectedWorld_Y[i] = vertices[i].y * zf;
			pcorrectedWorld_Z[i] = vertices[i].z * zf;

			// vertices[i].normal_x *= zf;
			// vertices[i].normal_y *= zf;
			// vertices[i].normal_z *= zf;

			texel_U[i] = vertices[i].u * zf;
			texel_V[i] = vertices[i].v * zf;

			if (cam_Z[i] < min_z) {
				min_z = cam_Z[i];
			}
			if (cam_Z[i] > max_z) {
				max_z = cam_Z[i];
			}
			if (cam_X[i] < min_x) {
				min_x = cam_X[i];
			}
			if (cam_X[i] > max_x) {
				max_x = cam_X[i];
			}
			if (cam_Y[i] < min_y) {
				min_y = cam_Y[i];
			}
			if (cam_Y[i] > max_y) {
				max_y = cam_Y[i];
			}
		}
	}

	@Override
	public float minZ() {
		return min_z;
	}

	@Override
	public float minX() {
		return min_x;
	}

	@Override
	public float minY() {
		return min_y;
	}

	@Override
	public float maxX() {
		return max_x;
	}

	@Override
	public float maxY() {
		return max_y;
	}

	@Override
	public float maxZ() {
		return max_z;
	}

	public Matrix3D getInverseTangentSpace() {
		if (inverseTangentSpace == null) {
			computeTangentSpace();
		}
		return inverseTangentSpace;
	}

	public Matrix3D getTangentSpace() {
		if (tangentSpace == null) {
			computeTangentSpace();
		}
		return tangentSpace;
	}

	public void clearTangentSpace() {
		tangentSpace = null;
		inverseTangentSpace = null;
	}

//	public void computeTangentSpace() {
//		// see:
//		// - literature/26-BumpMap+ProcTex.pdf
//		// - http://www.terathon.com/code/tangent.html
//		// -
//		// http://www.gamasutra.com/view/feature/129939/messing_with_tangent_space.php
//
////		int q2_index = vertices.length > 3 ? 3 : 2;
//		int q2_index = vertices.length-1;
//
//		float q1_x = vertices[1].x - vertices[0].x;
//		float q1_y = vertices[1].y - vertices[0].y;
//		float q1_z = vertices[1].z - vertices[0].z;
//
//		float q2_x = vertices[q2_index].x - vertices[0].x;
//		float q2_y = vertices[q2_index].y - vertices[0].y;
//		float q2_z = vertices[q2_index].z - vertices[0].z;
//
//		float s1 = 0 - 0;
//		float s2 = texel_U[q2_index] - 0;
//		float t1 = texel_V[1] - 0;
//		float t2 = texel_V[q2_index] - 0;
//
//		float norm = 1f / (s1 * t2 - s2 * t1);
//
//		float tangent_x = norm * (-s2 * q1_x + s1 * q2_x);
//		float tangent_y = norm * (-s2 * q1_y + s1 * q2_y);
//		float tangent_z = norm * (-s2 * q1_z + s1 * q2_z);
//
//		float bitangent_x = norm * (t2 * q1_x - t1 * q2_x);
//		float bitangent_y = norm * (t2 * q1_y - t1 * q2_y);
//		float bitangent_z = norm * (t2 * q1_z - t1 * q2_z);
//
//		// normalisieren
//		float[] tangentNormalized = Vector3D.normalize(tangent_x, tangent_y, tangent_z);
//		float[] bitangentNormalized = Vector3D.normalize(bitangent_x, bitangent_y, bitangent_z);
//
//		tangentSpace = new Matrix3D();
//		tangentSpace.setYAxis(tangentNormalized[0], tangentNormalized[1], tangentNormalized[2]);
//		tangentSpace.setXAxis(bitangentNormalized[0], bitangentNormalized[1], bitangentNormalized[2]);
//
//		// tangentSpace.setZAxis(vertices[0].normal_x, vertices[0].normal_y,
//		// vertices[0].normal_z);
//		tangentSpace.setZAxis(normal_x, normal_y, normal_z);
//
//		// noch orthogonalisieren?!
//
//		/*
//		 * Anderer Weg: Generate the Bi-Normal vector from the v channel first.
//		 * As we leaned from upside down mapping, our normal will be inverted.
//		 * One simple way of doing this is to just avoid the inverted vector.
//		 * Generate the Tangent vector from the cross product of the Normal and
//		 * the Bi-Normal.
//		 * 
//		 * Detecting inverted texture coordinates between two adjacent faces can
//		 * be fairly simple.
//		 * 
//		 * Once the Tangent and Bi-Normal vectors have been generated�
//		 * 
//		 * Check if the Tangent vectors face the same direction. This can be
//		 * done quickly using a dot product and checking if the answer is
//		 * positive. Then get the face normals for the two faces. Check if they
//		 * face the same direction. If results from step 1 and 2 both result in
//		 * positive or both negative answers, then your texture coordinates
//		 * along the u channel is fine. Now do the same for the Bi-Normal vector
//		 * to check if the v channels are inverted.
//		 * 
//		 * 
//		 */
//
//		inverseTangentSpace = tangentSpace.getInverse();
//	}
	
	
	public void computeTangentSpace() {
		// see:
		// - literature/26-BumpMap+ProcTex.pdf
		// - http://www.terathon.com/code/tangent.html
		// -
		// http://www.gamasutra.com/view/feature/129939/messing_with_tangent_space.php

//		int q2_index = vertices.length > 3 ? 3 : 2;
		int q2_index = vertices.length-1;

		float q1_x = vertices[1].x - vertices[0].x;
		float q1_y = vertices[1].y - vertices[0].y;
		float q1_z = vertices[1].z - vertices[0].z;

		float q2_x = vertices[q2_index].x - vertices[0].x;
		float q2_y = vertices[q2_index].y - vertices[0].y;
		float q2_z = vertices[q2_index].z - vertices[0].z;

		float s1 = vertices[1].u - vertices[0].u;
		float s2 = vertices[q2_index].u - vertices[0].u;
		float t1 = vertices[1].v - vertices[0].v;
		float t2 = vertices[q2_index].v - vertices[0].v;

		float q = 1f / (s1 * t2 - s2 * t1);

		float tangent_x = q * (-s2 * q1_x + s1 * q2_x);
		float tangent_y = q * (-s2 * q1_y + s1 * q2_y);
		float tangent_z = q * (-s2 * q1_z + s1 * q2_z);

		float bitangent_x = q * (t2 * q1_x - t1 * q2_x);
		float bitangent_y = q * (t2 * q1_y - t1 * q2_y);
		float bitangent_z = q * (t2 * q1_z - t1 * q2_z);

		
		Vector3D normal = getNormal().copy();
		Vector3D tangent = new Vector3D(tangent_x, tangent_y, tangent_z);		
		
		// orthogonalisieren nach Gram-Schmidt
		float normalTangentDot = normal.dotProduct(tangent);		
		normal.multiply(normalTangentDot);
		tangent = tangent.subtract(normal).normalize();
							
		Vector3D bitangent = getNormal().crossProduct(tangent);		
		
		float handedness = bitangent.dotProduct(bitangent_x, bitangent_y, bitangent_z) < 0 ? -1 : 1;	
		bitangent.multiply(handedness);

		tangentSpace = new Matrix3D();
		tangentSpace.setYAxis(tangent);				
		tangentSpace.setXAxis(bitangent);
		tangentSpace.setZAxis(getNormal());
		
		inverseTangentSpace = tangentSpace.getInverse();
	}

	@Override
	public int compareTo(RenderFace rf) {
		if (this.min_z > rf.min_z) {
			return 1;
		} else if (this.min_z < rf.min_z) {
			return -1;
		}
		return 0;
	}

	public int getScreen_bbox_minx() {
		return screen_bbox_minx;
	}

	public void setScreen_bbox_minx(int screen_bbox_minx) {
		this.screen_bbox_minx = screen_bbox_minx;
	}

	public int getScreen_bbox_maxx() {
		return screen_bbox_maxx;
	}

	public void setScreen_bbox_maxx(int screen_bbox_maxx) {
		this.screen_bbox_maxx = screen_bbox_maxx;
	}

	public int getScreen_bbox_miny() {
		return screen_bbox_miny;
	}

	public void setScreen_bbox_miny(int screen_bbox_miny) {
		this.screen_bbox_miny = screen_bbox_miny;
	}

	public int getScreen_bbox_maxy() {
		return screen_bbox_maxy;
	}

	public void setScreen_bbox_maxy(int screen_bbox_maxy) {
		this.screen_bbox_maxy = screen_bbox_maxy;
	}

	/**
	 * reset coordinates as in original face reset render state
	 */
	protected void reset(Face face) {
		for (int i = 0; i < face.vertices.length; i++) {
			Point3D faceVertex = face.vertices[i];
			Point3D vertex = this.vertices[i];
			vertex.setCoordinates(faceVertex.x, faceVertex.y, faceVertex.z);
			vertex.setNormalVector(faceVertex.normal_x, faceVertex.normal_y, faceVertex.normal_z);
			vertex.u = faceVertex.u;
			vertex.v = faceVertex.v;
		}
		centroid.setCoordinates(face.centroid.x, face.centroid.y, face.centroid.z);
		centroid.setNormalVector(face.centroid.normal_x, face.centroid.normal_y, face.centroid.normal_z);

		col = face.col;

		normal_x = face.normal_x;
		normal_y = face.normal_y;
		normal_z = face.normal_z;

		tangentSpace = null;
		inverseTangentSpace = null;
	}

	public Point3D getCameraPosition() {
		return this.cameraPosition;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}
}
