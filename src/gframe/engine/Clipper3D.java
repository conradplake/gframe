package gframe.engine;

/**
 * A 3D polygon clipper.
 * 
 * Clipping should be done in object space, i.e. before any face transformation
 * was applied for rendering.
 * 
 */
public class Clipper3D {

	/**
	 * Assumes all given parameters to be relative to object space. Returns null
	 * if complete face is behind camera (near plane at dist=0) or if face is
	 * oriented backside.
	 */
	public static Face clip(Face face, Point3D camPos, Vector3D camZ) {

		// lets do back face culling first: its fast and eliminates approx. 50%
		// of all polys alone!
		boolean isBackfaced = ((camPos.x - face.centroid.x) * face.normal_x
				+ (camPos.y - face.centroid.y) * face.normal_y + (camPos.z - face.centroid.z) * face.normal_z) < 0;

		if (isBackfaced) {
			return null;
		}

		// near plane clipping..
		Point3D[] vertices = face.getVertices();
		float[] cosines = new float[vertices.length];
		int vertexClipCount = 0;

		for (int i = 0; i < vertices.length; i++) {
			Point3D vertex = vertices[i];
			float cosine = camZ.dotProduct(vertex.x - camPos.x, vertex.y - camPos.y, vertex.z - camPos.z);
			cosines[i] = cosine;
			if (cosine < 0) {
				vertexClipCount++;
			}
		}

		if (vertexClipCount == 0) {
			return face; // all vertices in front of camera
		}

		if (vertexClipCount == vertices.length) {
			return null; // all vertices behind camera
		}

		// we need to clip *sigh*
		Point3D[] newVertices = new Point3D[vertices.length << 1]; // double
																	// size is
																	// an upper
																	// bound
		int newVertexCount = 0;

		// for each edge we get four vertex combinations:
		// in -> in (both vertices inside)
		// in -> out (from in to out)
		// out -> in (from out to in)
		// out -> out (both outside)
		for (int i = 0; i < vertices.length; i++) {

			int prev = i == 0 ? vertices.length - 1 : i - 1; // index of
																// previous
																// vertex

			float cosine_vertex = cosines[i];
			float cosine_prevVertex = cosines[prev];

			if (cosine_vertex < 0) {
				if (cosine_prevVertex < 0) {
					// both behind camera -> do nothing
					// out -> out
				} else {
					// compute intersection and add to result
					// in -> out
					Point3D newVertex = vertices[prev].copy();
					float lerpFactor = Math.abs(cosine_prevVertex / (cosine_prevVertex - cosine_vertex));
					newVertex.lerp(vertices[i], lerpFactor);
					newVertices[newVertexCount++] = newVertex;
				}

			} else {
				if (cosine_prevVertex < 0) {
					// prev behind camera -> interpolate
					// out -> in

					Point3D newVertex = vertices[prev].copy();
					float lerpFactor = Math.abs(cosine_prevVertex / (cosine_vertex - cosine_prevVertex));

					newVertex.lerp(vertices[i], lerpFactor);
					newVertices[newVertexCount++] = newVertex;

					// dont forget the current vertex i
					newVertices[newVertexCount++] = vertices[i].copy();
					
				} else {
					// both in front of camera -> transfer vertex to result face
					// in -> in
					newVertices[newVertexCount++] = vertices[i].copy();
				}
			}
		}

		// return new clipped face
		return new Face(newVertices, newVertexCount, face.col);
	}
}
