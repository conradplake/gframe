package gframe.parser;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import gframe.engine.Face;
import gframe.engine.Model3D;
import gframe.engine.Point3D;

public class WavefrontObjParser {

	public static Model3D parse(File objFile, Color col) {

		Model3D model = new Model3D();

		Map<Integer, float[]> vertexIndex2Coordinates = new Hashtable<Integer, float[]>();
		Map<Integer, float[]> textureIndex2UVMapping = new Hashtable<Integer, float[]>();
		Map<Integer, float[]> normalIndex2Coordinates = new Hashtable<Integer, float[]>();

		int currentTextureIndex = 0;
		int currentVertexIndex = 0;
		int currentNormalIndex = 0;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(objFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();

				// System.out.println(line);

				if (line.startsWith("v ")) {
					String[] fields = line.split("\\s+");
					float x = -Float.parseFloat(fields[1]); // obj files are
															// defined left-hand
															// sided, see also
															// face creation
															// (vertices are
															// reversed)
					float y = Float.parseFloat(fields[2]);
					float z = Float.parseFloat(fields[3]);
					// model.addVertex(x, y, z);
					vertexIndex2Coordinates.put(currentVertexIndex, new float[] { x, y, z });
					currentVertexIndex++;
				} else if (line.startsWith("vt ")) {
					String[] fields = line.split("\\s+");
					float u = Float.parseFloat(fields[1]);
					float v = Float.parseFloat(fields[2]);
					textureIndex2UVMapping.put(currentTextureIndex, new float[] { u, v });
					currentTextureIndex++;
				} else if (line.startsWith("vn ")) {
					String[] fields = line.split("\\s+");
					float nx = -Float.parseFloat(fields[1]);
					float ny = Float.parseFloat(fields[2]);
					float nz = Float.parseFloat(fields[3]);
					normalIndex2Coordinates.put(currentNormalIndex, new float[] { nx, ny, nz });
					currentNormalIndex++;
				} else if (line.startsWith("f ")) {
					String[] fields = line.split("\\s+");

					Point3D[] vertices = new Point3D[fields.length - 1];

					for (int i = 1; i < fields.length; i++) {
						String[] subFields = fields[i].split("/");

						int vertexIndexInFile = Integer.parseInt(subFields[0]);

						if (vertexIndexInFile < 0) { // see wavefront obj format
														// description: negative
														// values refer to the
														// end of vertex list
							vertexIndexInFile = model.numberOfVertices() + vertexIndexInFile;
						} else {
							vertexIndexInFile--; // index count starts from 1
						}

						float[] coordinates = vertexIndex2Coordinates.get(vertexIndexInFile);

						Point3D vertex = new Point3D(coordinates[0], coordinates[1], coordinates[2]);

						// vertices[i-1] = vertex;
						vertices[vertices.length - i] = vertex; // need to
																// reverse as ob
																// files are
																// defined
																// left-hand
																// sided

						// texture mapping
						if (subFields.length > 1 && subFields[1].length() > 0) {
							Integer textureIndex = Integer.parseInt(subFields[1]);
							float[] uvs = textureIndex2UVMapping.get(textureIndex - 1);
							vertex.u = uvs[0];
							vertex.v = 1 - uvs[1];
						}

						// vertex normal mapping
						if (subFields.length > 2 && subFields[2].length() > 0) {
							Integer normalIndex = Integer.parseInt(subFields[2]);
							float[] ncoords = normalIndex2Coordinates.get(normalIndex - 1);
							vertex.normal_x = ncoords[0];
							vertex.normal_y = ncoords[1];
							vertex.normal_z = ncoords[2];
						}
					}
					Face face = new Face(vertices, col);
					model.addFace(face);
					for (Point3D v : vertices) {
						model.addVertex(v);
					}
				}
			}

			// System.out.println("# vert: "+vertexCount);
			// System.out.println("# text: "+textureCount);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) {
				}
			}
		}

		if (currentNormalIndex == 0) {
			model.computeVertexNormals();
		}

		return model;

	}

	
	/**
	 * Builds just a mesh without vertex normal and u,v mapping
	 * */
	public static Model3D parseGeometry(File objFile, Color col) {

		Model3D model = new Model3D();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(objFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();

				// System.out.println(line);

				if (line.startsWith("v ")) {
					String[] fields = line.split("\\s+");
					float x = Float.parseFloat(fields[1]);
					float y = Float.parseFloat(fields[2]);
					float z = Float.parseFloat(fields[3]);
					model.addVertex(x, y, z);

					// vertexCount++;
				} else if (line.startsWith("f ")) {
					String[] fields = line.split("\\s+");
					int[] vertices = new int[fields.length - 1];
					for (int i = 1; i < fields.length; i++) {
						String[] subFields = fields[i].split("/");

						int vertexIndexInFile = Integer.parseInt(subFields[0]);

						if (vertexIndexInFile < 0) { // see wavefront obj format
														// description: negative
														// values refer to the
														// end of vertex list
							vertexIndexInFile = model.numberOfVertices() + vertexIndexInFile;
						} else {
							vertexIndexInFile--; // index count starts from 1
						}
						vertices[i - 1] = vertexIndexInFile;
					}
					model.stretchFace(vertices, col);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) {
				}
			}
		}

		return model;

	}

}
