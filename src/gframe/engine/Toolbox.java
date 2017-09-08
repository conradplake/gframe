package gframe.engine;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import graph.Graph;

/**
 * Really just a collection of static helper methods.
 **/
public class Toolbox {

	public static float lerp(float a, float b, float t) {
		return (a + (b - a) * t);
	}

	public static int lerp(int a, int b, float t) {
		return (int) (a + (b - a) * t);
	}

	/**
	 * Maps the given value within interval min/max to a corresponding new value
	 * between newIntervall min/max.
	 */
	public static double map(double value, double intervall_min, double intervall_max, double newIntervall_min,
			double newIntervall_max) {

		if (value >= intervall_min && value <= intervall_max) {
			double intervallPercentage = (value - intervall_min) / (intervall_max - intervall_min);
			return newIntervall_min + (intervallPercentage) * (newIntervall_max - newIntervall_min);
		} else {
			return value;
		}
	}

	public static final double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static final float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static boolean isOutisde(double value, double min, double max) {
		if (value < min) {
			return true;
		}
		if (value > max) {
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

	public static Graph toGraph(Model3D model) {

		List<Point3D> vertices = model.getVertices();
		Map<Point3D, Integer> vertex2index = new HashMap<Point3D, Integer>();
		for (int i = 0; i < vertices.size(); i++) {
			vertex2index.put(vertices.get(i), i);
		}

		Graph modelGraph = new Graph(vertices.size());

		for (Face face : model.getFaces()) {
			Point3D[] verticesAtFace = face.getVertices();
			for (int i = 0; i < verticesAtFace.length; i++) {
				Point3D vertex = verticesAtFace[i];
				Point3D nextVertex = verticesAtFace[i == verticesAtFace.length - 1 ? 0 : i + 1];

				int vertexId = vertex2index.get(vertex) + 1; // graph node ids
																// start at 1
				int nextVertexId = vertex2index.get(nextVertex) + 1;

				modelGraph.addEdge(vertexId, nextVertexId, 1); // graph takes
																// care of
																// doublings
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

	/**
	 * see: http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
	 */
	public static float distanceToLine(Point3D lineFrom, Point3D lineTo, Point3D p) {

		Vector3D v1 = p.copy().subtract(lineFrom).toVector();
		Vector3D v2 = p.copy().subtract(lineTo).toVector();
		Vector3D v3 = lineTo.copy().subtract(lineFrom).toVector();

		float nominator = v1.crossProduct(v2).length();
		float denominator = v3.length();

		return nominator / denominator;
	}

	public static void drawPolygon(ImageRaster raster, int[] X, int[] Y, int[] rgb) {

		for (int i = 0; i < X.length; i++) {

			int next = i + 1;
			if (next == X.length)
				next = 0;

			int x = X[i];
			int y = Y[i];
			int x2 = X[next];
			int y2 = Y[next];

			int dx = x2 - x;
			int dy = y2 - y;

			int len = (int) Math.sqrt(dx * dx + dy * dy);

			float fx = (float) dx / (float) len;
			float fy = (float) dy / (float) len;

			for (int j = 0; j < len; j++) {
				raster.setPixel(x + (int) (j * fx), y + (int) (j * fy), rgb[i]);
			}

		}

	}

	public static void drawPolygon(ImageRaster raster, int[] X, int[] Y, int rgb) {

		for (int i = 0; i < X.length; i++) {

			int next = i + 1;
			if (next == X.length)
				next = 0;

			int x = X[i];
			int y = Y[i];
			int x2 = X[next];
			int y2 = Y[next];

			int dx = x2 - x;
			int dy = y2 - y;

			int len = (int) Math.sqrt(dx * dx + dy * dy);

			float fx = (float) dx / (float) len;
			float fy = (float) dy / (float) len;

			for (int j = 0; j < len; j++) {
				raster.setPixel(x + (int) (j * fx), y + (int) (j * fy), rgb);
			}

		}

	}

	/*
	 * für jeden pixel in der Bounding Box (BB) "insideness" mittels even-odd
	 * rule berechnen. einfärben mit face.col
	 */
	public static void fillPolygon(ImageRaster raster, int[] X, int[] Y, int rgb) {

		drawPolygon(raster, X, Y, rgb);

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (int i = 0; i < X.length; i++) {
			if (X[i] < minX) {
				minX = X[i];
			}
			if (X[i] > maxX) {
				maxX = X[i];
			}
		}
		for (int i = 0; i < Y.length; i++) {
			if (Y[i] < minY) {
				minY = Y[i];
			}
			if (Y[i] > maxY) {
				maxY = Y[i];
			}
		}

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				if (isInside(x, y, X, Y)) {
					raster.setPixel(x, y, rgb);
				}
			}
		}
	}

	/**
	 * Even-odd Algorithmus
	 */
	public static boolean isInside(int x, int y, int[] X, int[] Y) {
		boolean result = false;
		int num = X.length;
		int j = num - 1;
		for (int i = 0; i < num; i++) {
			if (((Y[i] > y) != (Y[j] > y)) && (x < (X[j] - X[i]) * (y - Y[i]) / (Y[j] - Y[i]) + X[i])) {
				result = !result;
			}
			j = i;
		}
		return result;
	}

	public static ImageRaster getImageRaster(Image image, int x, int y, int w, int h) {

		int[] pixels = new int[w * h];

		PixelGrabber pg = new PixelGrabber(image, x, y, w, h, pixels, 0, w);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("ImageHelper.getRGBRaster(): interrupted while fetching pixels!");
			return null;
		}

		return new ImageRaster(w, h, pixels);
	}

	public static int toGray(int rgb) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		return (r + g + b) / 3;

	}

	public static final float[][] TPFILTER33 = new float[][] { { 1 / 9f, 1 / 9f, 1 / 9f }, { 1 / 9f, 1 / 9f, 1 / 9f },
			{ 1 / 9f, 1 / 9f, 1 / 9f } };

	public static final float[][] HPFILTER33 = new float[][] { { -0.5f, 1.0f, -0.5f }, { -0.5f, 1.0f, -0.5f },
			{ -0.5f, 1.0f, -0.5f } };

	public static final float[][] GAUSSFILTER33 = new float[][] { { 1 / 16f, 2 / 16f, 1 / 16f },
			{ 2 / 16f, 4 / 16f, 2 / 16f }, { 1 / 16f, 2 / 16f, 1 / 16f } };

}