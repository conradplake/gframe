package gframe.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Model3D extends Object3D {

	public Model3D() {
		super();
		vertices = new ArrayList<Point3D>();
		faces = new ArrayList<Face>();
		vertex2faces = new HashMap<Point3D, Set<Face>>();
	}

	public Model3D(Point3D origin) {
		super(origin);
		vertices = new ArrayList<Point3D>();
		faces = new ArrayList<Face>();
		vertex2faces = new HashMap<Point3D, Set<Face>>();
	}

	public void register(Engine3D engine3d) {
		engine3d.register(this);
	}

	public void deregister(Engine3D engine3d) {
		engine3d.deregister(this);
	}
	
	/**
	 * Called by the engine before this model is drawn
	 * */
	public void preDraw(){
		
	}

	/**
	 * ohne kinder-modelle
	 * 
	 * **/
	public List<Point3D> getVertices() {
		return vertices;
	}

	
	/**
	 * ohne kinder-modelle
	 * 
	 * **/
	public List<Face> getFaces() {
		return faces;
	}

	public Point3D findVertex(Point3D query, float maxRange) {
		for (Point3D vertex : vertices) {			
			if(vertex.distance(query)<=maxRange){
				return vertex;
			}
		}		
		return null;
	}
	
	
	public Point3D findVertex(float x, float y, float z, float maxRange) {
		Point3D query = new Point3D(x,y,z);		
		return findVertex(query, maxRange);
	}
	
	
	public void addVertex(Point3D p) {
		vertices.add(p);
		bsRad = -1;
	}

	public void addVertex(float x, float y, float z) {
		vertices.add(new Point3D(x, y, z));
		bsRad = -1;
	}

	
	/**
	 * 
	 * */
	public int numberOfVertices() {
		int count = vertices.size();
		
		for(Object child : getChildren()){			
			count += ((Model3D)child).numberOfVertices();
		}
		
		return count;
	}
	
	/**
	 * 
	 * */
	public int numberOfFaces() {
		int count = faces.size();
		
		for(Object child : getChildren()){			
			count += ((Model3D)child).numberOfFaces();
		}
		
		return count;
	}

	public Face stretchFace(int p1, int p2, int p3, int r, int g, int b) {
		return stretchFace(p1, p2, p3, new java.awt.Color(r, g, b));
	}
	
	public Face stretchFace(int p1, int p2, int p3, int p4, int r, int g, int b) {
		return stretchFace(p1, p2, p3, p4, new java.awt.Color(r, g, b));
	}
	
	public Face stretchFace(int p1, int p2, int p3, java.awt.Color col) {
		Point3D pi1 = (Point3D) vertices.get(p1);
		Point3D pi2 = (Point3D) vertices.get(p2);
		Point3D pi3 = (Point3D) vertices.get(p3);
		
		Face face = new Face(pi1, pi2, pi3, col);
		addFace(face);
		return face;
	}

	public Face stretchFace(int p1, int p2, int p3, int p4, java.awt.Color col) {
		Point3D pi1 = (Point3D) vertices.get(p1);
		Point3D pi2 = (Point3D) vertices.get(p2);
		Point3D pi3 = (Point3D) vertices.get(p3);
		Point3D pi4 = (Point3D) vertices.get(p4);
		
		Face face = new Face(pi1, pi2, pi3, pi4, col);
		addFace(face);		
		return face;
	}
		
	public void stretchFace(int[] vertexIndices, java.awt.Color col) {
		Point3D[] faceVertices = new Point3D[vertexIndices.length];	
		for(int i=0;i<vertexIndices.length;i++){
			faceVertices[i] = (Point3D)vertices.get(vertexIndices[i]);
		}
		Face face = new Face(faceVertices, faceVertices.length, col);
		addFace(face);
	}
	
	public void addFace(Face face){
		faces.add(face);
		addToVertexMapping(face);
	}
	
	
	private void addToVertexMapping(Face face){
		for(Point3D verttex : face.getVertices()){
			Set<Face> facesAtVertex = vertex2faces.get(verttex);
			if(facesAtVertex==null){
				facesAtVertex = new HashSet<Face>();
				vertex2faces.put(verttex, facesAtVertex);
			}
			facesAtVertex.add(face);
		}
	}
	
	
	public Set<Face> getFaces(Point3D vertex) {
		if(vertex2faces.containsKey(vertex)){
			return vertex2faces.get(vertex);
		}else{
			return Collections.emptySet();
		}		
	}
	
	
	public boolean deleteFace(Face face) {
		boolean result = faces.remove(face);
		if(result){								
			for(Point3D vertex : face.getVertices()){
				if(vertex2faces.containsKey(vertex)){
					vertex2faces.get(vertex).remove(face);
				}
			}
		}
		return result;
	}
	

	public void setColor(java.awt.Color col) {
		Iterator<Face> it = faces.iterator();
		while (it.hasNext()) {
			it.next().col = col;			
		}
	}
	
	
	public void recomputeFaceNormals(){	
		for (Face face : faces) {
			face.recompute();
		}				
		for(Object child : getChildren()){
			((Model3D)child).recomputeFaceNormals();
		}
	}
	
	
	public void computeVertexNormals(){
		for (Face face : faces) {
			for(Point3D vertex : face.vertices){				
				vertex.normal_x += face.normal_x;
				vertex.normal_y += face.normal_y;
				vertex.normal_z += face.normal_z;			
			}
		}
		
		for (Point3D vertex : vertices) {				
			Vector3D vertexNormal = new Vector3D(vertex.normal_x, vertex.normal_y, vertex.normal_z);
			vertexNormal.normalize();
			vertex.normal_x = vertexNormal.x;
			vertex.normal_y = vertexNormal.y;
			vertex.normal_z = vertexNormal.z;
		}
		
				
		for(Object child : getChildren()){
			((Model3D)child).computeVertexNormals();
		}		
	}
		

	/**
	 * 
	 * TODO Bezieht noch nicht die Kinder mit ein!!
	 */
	public float getBoundingSphereRadius() {
		if (bsRad == -1) {
			Iterator<Point3D> it = vertices.iterator();
			while (it.hasNext()) {
				float dist = (it.next()).distance(0, 0, 0);
				if (dist > bsRad){
					bsRad = dist;
				}
			}
		}
		return bsRad;
	}

	public boolean collided(Model3D model) {
		boolean ret = false;
		float val = getBoundingSphereRadius() + model.getBoundingSphereRadius();
		float distSquared = getOrigin().distanceSquared(model.getOrigin());
		if (distSquared < val * val) {
			ret = true;
		}
		return ret;
	}

	public boolean collided(Point3D point) {
		boolean ret = false;
		float rad = getBoundingSphereRadius();
		float distSquared = getOrigin().distanceSquared(point);
		if (distSquared < rad * rad) {
			ret = true;
		}
		return ret;
	}

	 
	public void scale(float sf_x, float sf_y, float sf_z) {
		Iterator<Point3D> it = vertices.iterator();
		while (it.hasNext()) {
			Point3D p = it.next();
			p.x *= sf_x;
			p.y *= sf_y;
			p.z *= sf_z;
		}			
		
		Iterator<Face> itFaces = faces.iterator();
		while(itFaces.hasNext()){
			Face face = itFaces.next();
			face.recompute();
		}
		
		bsRad = -1;
		
		for(Object child : getChildren()){
			((Model3D)child).scale(sf_x, sf_y, sf_z);
		}
	}

	public void removeFaces() {
		faces.clear();
		vertex2faces.clear();
	}
	
	
	public float[] getBBox(){				
			
		float minx =  Integer.MAX_VALUE;
		float maxx = -Integer.MAX_VALUE;
		
		float miny =  Integer.MAX_VALUE;
		float maxy = -Integer.MAX_VALUE;
		
		float minz =  Integer.MAX_VALUE;
		float maxz = -Integer.MAX_VALUE;
		
		for(int i=0;i<vertices.size();i++){
			Point3D vertex = vertices.get(i);
			if(vertex.x < minx){
				minx = vertex.x;
			}
			if(vertex.x > maxx){
				maxx = vertex.x;
			}
			if(vertex.y < miny){
				miny = vertex.y;
			}
			if(vertex.y > maxy){
				maxy = vertex.y;
			}
			if(vertex.z < minz){
				minz = vertex.z;
			}
			if(vertex.z > maxz){
				maxz = vertex.z;
			}
		}
		
		for(Object child : getChildren()){
			 float[] childBBox = ((Model3D)child).getBBox();
			 if(childBBox[0]<minx){
				 minx = childBBox[0];
			 }
			 if(childBBox[1]>maxx){
				 maxx = childBBox[1];
			 }
			 if(childBBox[2]<miny){
				 miny = childBBox[2];
			 }
			 if(childBBox[3]>maxy){
				 maxy = childBBox[3];
			 }
			 if(childBBox[4]<minz){
				 minz = childBBox[4];
			 }
			 if(childBBox[5]>maxz){
				 maxz = childBBox[5];
			 }
		}
		
		return new float[]{minx, maxx, miny, maxy, minz, maxz};
	}
	
	
	public void clear() {
		faces.clear();
		vertices.clear();
		vertex2faces.clear();
		bsRad = -1;
	}

	
	private float bsRad = -1;

	private List<Point3D> vertices;
	private List<Face> faces;	
	
	private Map<Point3D, Set<Face>> vertex2faces;

	public boolean isVisible = true;
	
}