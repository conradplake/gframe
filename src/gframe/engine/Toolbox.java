package gframe.engine;

import java.util.*;

import graph.Graph;
import imaging.ImageRaster;

public class Toolbox {

	public static float lerp(float a, float b, float t) {
		return (a + (b - a) * t);
	}

	public static int lerp(int a, int b, float t) {
		return (int) (a + (b - a) * t);
	}
	
	
	/**
	 * Maps the given value within interval min/max to a corresponding new value between newIntervall min/max.
	 * */
	public static double map(double value, double intervall_min, double intervall_max, double newIntervall_min, double newIntervall_max) {
		
		if(value>=intervall_min && value<=intervall_max){
			double intervallPercentage = (value-intervall_min) / (intervall_max-intervall_min);		
			return newIntervall_min + (intervallPercentage)*(newIntervall_max-newIntervall_min);			
		}
		else{
			return value;
		}			
	}
	
	
	public static double constrain(double value, double min, double max){
		if (value<min){
			value = min;			
		}
		if(value>max){
			value=max;
		}
		return value;
	}
	
	
	public static boolean isOutisde(double value, double min, double max){
		if (value<min){
			return true;			
		}
		if(value>max){
			return true;
		}
		return false;
	}
	

	public static float radiantToDegree(float rad) {
		return (float) (rad * 180 / Math.PI);
	}

	public static float degreeToRadiant(float degree) {
		return (float) (degree * Math.PI / 180);
	}

	public static Matrix3D getRotMatrix(float deg_x, float deg_y, float deg_z) {
		Matrix3D rotMat = new Matrix3D();
		if (deg_x != 0) {
			rotMat.transform(getXrotMatrixArray(deg_x));
		}
		if (deg_y != 0) {
			rotMat.transform(getYrotMatrixArray(deg_y));
		}
		if (deg_z != 0) {
			rotMat.transform(getZrotMatrixArray(deg_z));
		}
		return rotMat;
	}

	public static float[][] getXrotMatrixArray(float deg) {
		float rad = degreeToRadiant(deg);
		float cosa = (float) Math.cos(rad); // besser mit lookup tables wenn oft
											// benutzt!!
		float sina = (float) Math.sin(rad);
		float[][] xrot = { { 1, 0, 0 }, { 0, cosa, -sina }, { 0, sina, cosa } };
		return xrot;
	}

	public static Matrix3D getXrotMatrix(float deg) {
		Matrix3D xrotMat = new Matrix3D();
		float rad = degreeToRadiant(deg);
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		xrotMat.setYAxis(0, cosa, -sina);
		xrotMat.setZAxis(0, sina, cosa);
		return xrotMat;
	}
	
	public static Matrix3D getXrotMatrixFromRadiant(float rad) {
		Matrix3D xrotMat = new Matrix3D();		
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		xrotMat.setYAxis(0, cosa, -sina);
		xrotMat.setZAxis(0, sina, cosa);
		return xrotMat;
	}

	public static float[][] getYrotMatrixArray(float deg) {
		float rad = degreeToRadiant(deg);
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		float[][] yrot = { { cosa, 0, sina }, { 0, 1, 0 }, { -sina, 0, cosa } };
		return yrot;
	}

	public static Matrix3D getYrotMatrix(float deg) {
		Matrix3D yrotMat = new Matrix3D();
		float rad = degreeToRadiant(deg);
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		yrotMat.setXAxis(cosa, 0, sina);
		yrotMat.setZAxis(-sina, 0, cosa);
		return yrotMat;
	}
	
	public static Matrix3D getYrotMatrixFromRadiant(float rad) {
		Matrix3D yrotMat = new Matrix3D();		
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		yrotMat.setXAxis(cosa, 0, sina);
		yrotMat.setZAxis(-sina, 0, cosa);
		return yrotMat;
	}

	public static float[][] getZrotMatrixArray(float deg) {
		float rad = degreeToRadiant(deg);
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		float[][] zrot = { { cosa, sina, 0 }, { -sina, cosa, 0 }, { 0, 0, 1 } };
		return zrot;
	}

	public static Matrix3D getZrotMatrix(float deg) {
		Matrix3D zrotMat = new Matrix3D();
		float rad = degreeToRadiant(deg);
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		zrotMat.setXAxis(cosa, sina, 0);
		zrotMat.setYAxis(-sina, cosa, 0);
		return zrotMat;
	}
	
	
	public static Matrix3D getZrotMatrixFromRadiant(float rad) {
		Matrix3D zrotMat = new Matrix3D();		
		float cosa = (float) Math.cos(rad);
		float sina = (float) Math.sin(rad);
		zrotMat.setXAxis(cosa, sina, 0);
		zrotMat.setYAxis(-sina, cosa, 0);
		return zrotMat;
	}
	

	public static Graph toGraph(Model3D model){
		
		List<Point3D> vertices = model.getVertices();
		Map<Point3D, Integer> vertex2index = new HashMap<Point3D, Integer>();
		for(int i=0;i<vertices.size();i++){
			vertex2index.put(vertices.get(i), i);
		}
		
		Graph modelGraph = new Graph(vertices.size());
		
		for(Face face : model.getFaces()){
			Point3D[] verticesAtFace = face.getVertices();
			for(int i=0;i<verticesAtFace.length;i++){
				Point3D vertex = verticesAtFace[i];
				Point3D nextVertex = verticesAtFace[i==verticesAtFace.length-1? 0 : i+1];
				
				int vertexId = vertex2index.get(vertex)+1;	// graph node ids start at 1
				int nextVertexId = vertex2index.get(nextVertex)+1;
				
				modelGraph.addEdge(vertexId, nextVertexId, 1); // graph takes care of doublings			
			}			
		}
		
		return modelGraph;
	}
	
	
	public static List toCurve(Point3D[] pArr, int steps) {
		List curve = new LinkedList();
		float t, t2, t3, k1, k2, k3, bsp_x, bsp_y, bsp_z;
		float reziSteps = 1 / (float) steps;

		curve.add(pArr[0].copy());
		for (int k = 1; k < pArr.length - 2; k++) {
			t = 0;
			while (t < 1.0) {
				t2 = t * t;
				t3 = t2 * t;
				k1 = 1 - 3 * t + 3 * t2 - t3;
				k2 = 4 - 6 * t2 + 3 * t3;
				k3 = 1 + 3 * t + 3 * t2 - 3 * t3;
				bsp_x = 1 / 6f * (k1 * pArr[k - 1].x + k2 * pArr[k].x + k3 * pArr[k + 1].x + t3 * pArr[k + 2].x);
				bsp_y = 1 / 6f * (k1 * pArr[k - 1].y + k2 * pArr[k].y + k3 * pArr[k + 1].y + t3 * pArr[k + 2].y);
				bsp_z = 1 / 6f * (k1 * pArr[k - 1].z + k2 * pArr[k].z + k3 * pArr[k + 1].z + t3 * pArr[k + 2].z);
				curve.add(new Point3D(bsp_x, bsp_y, bsp_z));
				t = t + reziSteps;
			}
		}
		curve.add(pArr[pArr.length - 1].copy());
		return curve;
	}

}