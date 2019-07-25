package gframe.engine.generator;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gframe.engine.Face;
import gframe.engine.ImageRaster;
import gframe.engine.Model3D;
import gframe.engine.Point3D;
import gframe.engine.TextureShader;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;
import graph.Graph;
import graph.Node;

public class Model3DGenerator {
	
	
	public static Model3D buildSkydome(float radius, Color color){		
		Model3D result = buildSphere(radius, 32, true, color);	
		result.rotate(90, 0, 0);
		invertFaces(result);
		return result;
	}
	
	
	
	/**
	 * Replaces each face by a face defined in opposite vertex order.
	 * */
	public static void invertFaces(Model3D model) {
		Collection<Face> newFaces = new ArrayList<Face>();
		for(Face face : model.getFaces()){
			Point3D[] vertices = face.getVertices();
			Point3D[] reverse = new Point3D[vertices.length];
			for(int i=0;i<vertices.length;i++){
				reverse[i] = vertices[vertices.length-1-i];				
			}
			Face newFace = new Face(reverse, face.getColor());
			newFaces.add(newFace);
		}
		
		model.removeFaces();
		
		for (Face face : newFaces) {
			model.addFace(face);
		}
	}
	
	
	public static Model3D buildTerrainMeshFromHeightMap(File heightmapFile, int w, int h, Color color){
		
		ImageRaster heightMap = TextureShader.getRGBRaster(heightmapFile, w, h);
		
		Model3D mesh = buildFlatMesh(w, h, color);
			
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){									
				int rgb = heightMap.getPixel(x, y);																	
				int grayValue = Toolbox.toGray(rgb);															
				Point3D vertex = mesh.getVertices().get(w*y+x);											
				vertex.move(0, grayValue, 0);			
			}
		}
				
		mesh.recomputeFaceNormals();	
		
		return mesh;	
	}

	
	public static void colorTerrainMesh(Model3D mesh){					
		for(Face face : mesh.getFaces()){			
			float relativeHeight = face.getCentroid().y / 255f;			
			Color c = getTerrainColor(face.getCentroid().y, relativeHeight);				
			face.setColor(c);
		}		
	}
	
	/**
	 * Assumes a height given as gray value in [0..255] 
	 * */
	public static Color getTerrainColor(float height, float relativeHeight){
		
		Color waterLevel = new Color(0, 0, 255);
		Color vegetationLevel = new Color(50, 255, 50);
		Color rockLevel = new Color(200, 200, 200);
		Color snowLevel = new Color(255, 255, 255);
		
		
		Color minColor, maxColor = null; 
		if(height<55){
			minColor = waterLevel;
			maxColor = vegetationLevel;
		} else if(height<170){
			minColor = vegetationLevel;
			maxColor = rockLevel;
		} else {
			minColor = rockLevel;
			maxColor = snowLevel;
		}
			
			
		int color_r = (int)Toolbox.lerp(minColor.getRed(), maxColor.getRed(), relativeHeight);
		int color_g = (int)Toolbox.lerp(minColor.getGreen(), maxColor.getGreen(), relativeHeight);
		int color_b = (int)Toolbox.lerp(minColor.getBlue(), maxColor.getBlue(), relativeHeight);
		
		if(color_r > 255)
			color_r = 255;		
		if(color_r < 0)
			color_r = 0;
		
		if(color_g > 255)
			color_g = 255;
		if(color_g < 0)
			color_g = 0;
		
		if(color_b > 255)
			color_b = 255;
		if(color_b < 0)
			color_b = 0;
		
		return new Color(color_r, color_g, color_b);
	}
	
	
	public static Model3D buildRandomVoronoiTerrainMesh(int w, int h, int tilesize, int numberOfAreas, Color color){
				
		Color[] colorPalette = new Color[numberOfAreas];
		
		for (int i=0; i<numberOfAreas; i++) {
			colorPalette[i] =  new Color(0, 0, i+1);
		}
		
		ImageRaster voronoiTexture = TextureGenerator.generateVoronoiTexture(w, h, numberOfAreas, colorPalette);
		
		Model3D mesh = buildFlatMesh(w, h, tilesize, color);		
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){									
				int rgb = voronoiTexture.getPixel(x, y);				
				int blue = (rgb >> 0) & 0xff;				
				Point3D vertex = mesh.getVertices().get(w*y+x);								
				vertex.move(0, blue, 0);																		
			}
		}
				
		mesh.recomputeFaceNormals();
		mesh.computeVertexNormals();
		return mesh;
	}
	
	
	public static Model3D buildSphere(float radius, int bands, Color color){
		return buildSphere(radius, bands, false, color);
	}
	
	public static Model3D buildSphere(float radius, int bands, boolean halfSphere, Color color){
		Model3D result = new Model3D();
		
		int latitudeBands = halfSphere? bands/2 : bands;
		
		for(int i=0;i<=latitudeBands;i++){						
			// map i from 0 to PI
			float latitude = (float)Toolbox.map(i, 0, bands, 0, Math.PI);			
			for(int j=0;j<bands;j++){
				// map j from 0 to 2xPI
				float longitude = (float)Toolbox.map(j, 0, bands, 0, 2*Math.PI);
				
				float x = radius * (float)Math.cos(longitude) * (float)Math.sin(latitude);				
				float y = radius * (float)Math.sin(latitude)  * (float)Math.sin(longitude);								
				float z = radius * (float)Math.cos(latitude);
				
				Point3D vertex = new Point3D(x, y, z);
				result.addVertex(vertex);
			}	
		}
		
//		System.out.println("# vertices: "+result.getVertices().size());
		
		// make triangle strips
		for(int i=0;i<latitudeBands;i++){										
			int next_i = i+1;
			
			for(int j=0;j<bands;j++){
				int next_j = j+1;
				if(next_j==bands){
					next_j=0;
				}
				
				int nodeId1 = i*bands+j;
				int nodeId2 = (next_i)*bands+j;
				int nodeId3 = (next_i)*bands+next_j;
				int nodeId4 = i*bands+next_j;
				
				
				if(i==0){
					result.stretchFace(nodeId1, nodeId2, nodeId3, color);
//					if(j%2==0){
//						result.stretchFace(nodeId1, nodeId2, nodeId3, color);
//						//result.stretchFace(nodeId1, nodeId3, nodeId4, color);
//					}else{
//						result.stretchFace(nodeId1, nodeId2, nodeId3, Color.RED);
//						//result.stretchFace(nodeId1, nodeId3, nodeId4, Color.RED);
//					}
				}
				else if(i==bands-1){
					result.stretchFace(nodeId1, nodeId3, nodeId4, color);
					
//					if(j%2==0){
//						//result.stretchFace(nodeId1, nodeId2, nodeId3, color);
//						result.stretchFace(nodeId1, nodeId3, nodeId4, color);
//					}else{
//						//result.stretchFace(nodeId1, nodeId2, nodeId3, Color.RED);
//						result.stretchFace(nodeId1, nodeId3, nodeId4, Color.RED);
//					}
				}
				else{
					result.stretchFace(nodeId1, nodeId2, nodeId3, color);
					result.stretchFace(nodeId1, nodeId3, nodeId4, color);
//					if(j%2==0){
//						result.stretchFace(nodeId1, nodeId2, nodeId3, color);
//						result.stretchFace(nodeId1, nodeId3, nodeId4, color);
//					}else{
//						result.stretchFace(nodeId1, nodeId2, nodeId3, Color.RED);
//						result.stretchFace(nodeId1, nodeId3, nodeId4, Color.RED);
//					}	
				}							
			}	
		}
		
//		System.out.println("# faces: "+result.getFaces().size());
		
		return result;
	}	

	
	
	/**
	 * Sets u,v coordinates of vertices in mesh to span given texture
	 * */
	public static void applyTexture(Model3D mesh, int meshWidth, int meshHeight, ImageRaster texture){
		for(int y=0;y<meshHeight;y++){
			for(int x=0;x<meshWidth;x++){
				int nodeId1 = y*meshWidth+x;
				Point3D vertex = mesh.getVertex(nodeId1);
				vertex.u = (float)x/(float)meshWidth;
				vertex.v = (float)y/(float)meshHeight;
			}
		}
	}


	public static Model3D buildFlatMesh(int w, int h, Color color){
		return buildFlatMesh(w, h, 20, color);
	}
	
	
	public static Model3D buildFlatMesh(int w, int h, int tilesize, Color color){		
		return buildFlatMesh(new Model3D(), w, h, tilesize, color);
	}
	
	
	public static Model3D buildFlatMesh(Model3D startMesh, int w, int h, int tilesize, Color color){
		int nodeIdOffset = startMesh.numberOfVertices();
		
//		for(int y=0;y<=h;y++){
//			for(int x=0;x<=w;x++){													
//				Point3D vertex = new Point3D(x*tilesize, 0, y*tilesize); // y --> z because we want our terrain aligend with the x,z-plane											
//				startMesh.addVertex(vertex); 														
//			}
//		}
//				
//		for(int y=0;y<h;y++){
//			for(int x=0;x<w;x++){					
//				int nodeId1 = nodeIdOffset + (y*(w+1)+x);
//				int nodeId2 = nodeIdOffset + ((y+1)*(w+1)+x);
//				int nodeId3 = nodeIdOffset + ((y+1)*(w+1)+x+1);
//				int nodeId4 = nodeIdOffset + (y*(w+1)+x+1);
//				
//				// one tile = two triangles											
//				startMesh.stretchFace(nodeId1, nodeId2, nodeId3, color);				
//				startMesh.stretchFace(nodeId1, nodeId3, nodeId4, color);
//			}	
//		}
		
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){													
				Point3D vertex = new Point3D(x*tilesize, 0, y*tilesize); // y --> z because we want our terrain aligend with the x,z-plane											
				startMesh.addVertex(vertex); 														
			}
		}
				
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){	
				
				if(y==0 || x==0 || y==h-1 || x==w-1){
					continue;
				}
				
				int nodeId1 = nodeIdOffset + (y*w+x);
				int nodeId2 = nodeIdOffset + ((y+1)*w+x);
				int nodeId3 = nodeIdOffset + ((y+1)*w+x+1);
				int nodeId4 = nodeIdOffset + (y*w+x+1);
				
				// one tile = two triangles											
				startMesh.stretchFace(nodeId1, nodeId2, nodeId3, color);				
				startMesh.stretchFace(nodeId1, nodeId3, nodeId4, color);
			}	
		}
		
		return startMesh;				
	}
	
	
	public static Model3D toCircledMesh(Model3D mesh, int radius){
		
		float[] bbox = mesh.getBBox();
		
		float origin_x = (bbox[1]-bbox[0])/2;
		float origin_y = (bbox[3]-bbox[2])/2;
		float origin_z = (bbox[5]-bbox[4])/2;
		Point3D origin = new Point3D(origin_x, origin_y, origin_z);
		
		// remove faces outside radius
		List<Face> faces = new ArrayList<Face>(mesh.getFaces());
		for (Face face : faces) {
			if(face.getCentroid().distance(origin) > radius){
				mesh.deleteFace(face);
			}
		}
		
		return mesh;				
	}
	
	
	public static Model3D buildQuadBasedMesh(int w, int h, int tilesize, Color color){
		
		Model3D mesh = new Model3D();
		
		for(int y=0;y<=h;y++){
			for(int x=0;x<=w;x++){													
				Point3D vertex = new Point3D(x*tilesize, 0, y*tilesize); // y --> z because we want our terrain aligned with the x,z-plane											
				mesh.addVertex(vertex); 														
			}
		}
		
//		System.out.println("# nodes: "+mesh.getVertices().size());
				
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){				
				int nodeId1 = (y*(w+1))+x;
				int nodeId2 = ((y+1)*(w+1))+x;
				int nodeId3 = ((y+1)*(w+1))+x+1;
				int nodeId4 = (y*(w+1))+x+1;
				mesh.stretchFace(nodeId1, nodeId2, nodeId3, nodeId4, color);				
			}	
		}
		
//		System.out.println("# faces: "+mesh.getFaces().size());
		
		return mesh;				
	}
	
	
	public static Model3D buildTiledFloor(int tiles_x, int tiles_y, int tileSize, Color color){
		Model3D result = new Model3D();

		int vertexCount = 0;
		
		for (int x = 0; x < tiles_x; x++) {
			for (int y = 0; y < tiles_y; y++) {
//				Model3D tile = new Model3D();
				Point3D tileorigin = new Point3D(tileSize / 2 + (x * tileSize), 0, tileSize / 2 + (y * tileSize));
//				tile.setOrigin(tileorigin);

//				tile.addVertex(-tileSize / 2, 0, -tileSize / 2);
//				tile.addVertex(-tileSize / 2, 0, tileSize / 2);
//				tile.addVertex(tileSize / 2, 0, tileSize / 2);
//				tile.addVertex(tileSize / 2, 0, -tileSize / 2);
				
				result.addVertex(tileorigin.x + -tileSize / 2, tileorigin.y + 0, tileorigin.z + -tileSize / 2);
				result.addVertex(tileorigin.x + -tileSize / 2, tileorigin.y + 0, tileorigin.z +  tileSize / 2);
				result.addVertex(tileorigin.x +  tileSize / 2, tileorigin.y + 0, tileorigin.z +  tileSize / 2);
				result.addVertex(tileorigin.x +  tileSize / 2, tileorigin.y + 0, tileorigin.z + -tileSize / 2);
				
				//tile.stretchFace(0, 1, 2, 3, color);
				//result.stretchFace(vertexCount+1, vertexCount+2, vertexCount+3, vertexCount, color);											
				result.stretchFace(vertexCount, vertexCount+1, vertexCount+2, vertexCount+3, color);
//				tile.rotate(0, 90, 0);
//				result.addChild(tile);
				
				vertexCount += 4;
			}
		}
		
		return result;	
	}
	
	
	public static Model3D buildRoom(float width, float height, float depth, java.awt.Color col) {
		return buildBlock(width, height, depth, col, true);		
	}
	

	public static Model3D buildCube(float size, java.awt.Color col) {
		return buildBlock(size, size, size, col, false);
	}
	
	
	public static Model3D buildBlock(float width, float height, float depth, java.awt.Color col) {
		return buildBlock(width, height, depth, col, false);
	}
	

	public static Model3D buildBlock(float width, float height, float depth, java.awt.Color col, boolean reverseFaces) {
		Model3D result = new Model3D();
		
		
//		Model3D front = buildPlane(width, height, new Point3D(), col);
//		front.move(0, 0, -depth/2);
//		result.addChild(front);
//		
//		Model3D back = buildPlane(width, height, new Point3D(), col);
//		back.rotate(180, 0, 0);
//		back.move(0, 0, depth/2);
//		result.addChild(back);
//		
//		Model3D top = buildPlane(width, height, new Point3D(), col);
//		top.move(0, height/2, 0);
//		top.rotate(-90, 0, 0);
//		result.addChild(top);
//		
//		
//		Model3D bottom = buildPlane(width, height, new Point3D(), col);
//		bottom.move(0, -height/2, 0);
//		bottom.rotate(90, 0, 0);
//		result.addChild(bottom);
//		
//		
//		Model3D left = buildPlane(width, height, new Point3D(), col);
//		left.move(-width/2, 0, 0);
////		left.rotate(0, -90, 0);
//		result.addChild(left);
//		
//		Model3D right = buildPlane(width, height, new Point3D(), col);
//		right.move(-width/2, 0, 0);
////		right.rotate(0, -90, 0);
//		result.addChild(right);
		
		result.addVertex(-width / 2, -height / 2, -depth / 2); // 0
		result.addVertex(-width / 2, -height / 2, depth / 2); // 1
		result.addVertex(width / 2, -height / 2, depth / 2); // 2
		result.addVertex(width / 2, -height / 2, -depth / 2); // 3

		result.addVertex(-width / 2, height / 2, -depth / 2); // 4
		result.addVertex(-width / 2, height / 2, depth / 2); // 5
		result.addVertex(width / 2, height / 2, depth / 2); // 6
		result.addVertex(width / 2, height / 2, -depth / 2); // 7

		if(reverseFaces)
			result.stretchFace(7, 6, 5, 4, col); // dach
		else
			result.stretchFace(4, 5, 6, 7, col); // dach
		
		if(reverseFaces)
			result.stretchFace(3, 7, 4, 0, col); // rueckwand
		else
			result.stretchFace(0, 4, 7, 3, col); // rueckwand
		
		
		if(reverseFaces)
			result.stretchFace(0, 4, 5, 1, col); // linke wand
		else
			result.stretchFace(1, 5, 4, 0, col); // linke wand
		
		if(reverseFaces)
			result.stretchFace(1, 5, 6, 2, col); // vorderwand
		else
			result.stretchFace(2, 6, 5, 1, col); // vorderwand	
	
		if(reverseFaces)
			result.stretchFace(2, 6, 7, 3, col); // rechte wand
		else
			result.stretchFace(3, 7, 6, 2, col); // rechte wand
		
		if(reverseFaces)
			result.stretchFace(0, 1, 2, 3, col); // boden
		else
			result.stretchFace(3, 2, 1, 0, col); // boden

		return result;
	}

	public static Model3D buildPlane(int size, Point3D origin, java.awt.Color col) {
		Model3D figure = new Model3D(origin);
		figure.addVertex(-size / 2, size / 2, 0);
		figure.addVertex(size / 2, size / 2, 0);
		figure.addVertex(size / 2, -size / 2, 0);
		figure.addVertex(-size / 2, -size / 2, 0);
		figure.stretchFace(0, 1, 2, 3, col);
		return figure;
	}
	
	public static Model3D buildPlane(int size, Point3D origin, java.awt.Color col, boolean doublesided) {
		Model3D figure = new Model3D(origin);
		figure.addVertex(-size / 2, size / 2, 0);
		figure.addVertex(size / 2, size / 2, 0);
		figure.addVertex(size / 2, -size / 2, 0);
		figure.addVertex(-size / 2, -size / 2, 0);
		figure.stretchFace(0, 1, 2, 3, col);
		
		if(doublesided){
			figure.stretchFace(3, 2, 1, 0, col);
		}
		return figure;
	}
	
	public static Model3D buildTile(int size, Point3D origin, java.awt.Color col) {
		Model3D figure = new Model3D(origin);
		figure.addVertex(-size / 2, 0, size / 2);
		figure.addVertex(size / 2, 0, size / 2);
		figure.addVertex(size / 2, 0, -size / 2);
		figure.addVertex(-size / 2, 0, -size / 2);
		figure.stretchFace(0, 1, 2, 3, col);
		return figure;
	}
	
	
	public static Model3D buildPlane(float width, float height, Point3D origin, java.awt.Color col) {
		Model3D figure = new Model3D(origin);
		figure.addVertex(-width / 2, height / 2, 0);
		figure.addVertex(width / 2, height / 2, 0);
		figure.addVertex(width / 2, -height / 2, 0);
		figure.addVertex(-width / 2, -height / 2, 0);
		
		figure.stretchFace(1, 2, 3, 0, col);
//		figure.stretchFace(3, 0, 1, 2, col);
		return figure;
	}

	
	
	public static Model3D buildMengerSponge(int level, int size, Color color){
		
		Model3D masterMengerCube = Model3DGenerator.buildCube(size, color);
		masterMengerCube.move(0, 0, 500);
		
		Model3D[] mengerSponge = new Model3D[(int)Math.pow(20, level)]; // level 3: 8000 cubes
		int cubeCounter = 0;
		
		Model3D[] mengerSpongeLevel1 = Model3DGenerator.toMengerSponge(masterMengerCube, size/3, Color.blue, true);		
		for (Model3D model3d : mengerSpongeLevel1) {			
			Model3D[] mengerSpongeLevel2 = Model3DGenerator.toMengerSponge(model3d, size/9, Color.blue, true);
			for (Model3D model3d2 : mengerSpongeLevel2) {
				Model3D[] mengerSpongeLevel3 = Model3DGenerator.toMengerSponge(model3d2, size/27, Color.blue, true);
				for (Model3D model3d3 : mengerSpongeLevel3) {
					
					 mengerSponge[cubeCounter++] = model3d3;
					 
//					 Model3D[] mengerSpongeLevel4 = Model3DGenerator.toMengerSponge(model3d3, 81, Color.blue, true);
//					 for (Model3D model3d4 : mengerSpongeLevel4) {
//						 mengerSponge[cubeCounter++] = model3d4;
//					 }
					 
				}
			}
		}
		
		Model3DGenerator.removeHiddenFaces(mengerSponge);
		
		masterMengerCube.clearGeometry();		
		masterMengerCube.setOrigin(new Point3D());				
		int totalVertexCount = 0;
		for (Model3D model3d : mengerSponge) {
			for(Face face : model3d.getFaces()){
				for(Point3D vertex : face.getVertices()){
					masterMengerCube.addVertex(vertex.copy().add(model3d.getOrigin()));
				}
				
				if(face.normal_y<0){ // otherwise normal mapping will produce inverted output
					masterMengerCube.stretchFace(totalVertexCount+2, totalVertexCount+3, totalVertexCount, totalVertexCount+1, face.getColor());
				}else{
					masterMengerCube.stretchFace(totalVertexCount, totalVertexCount+1, totalVertexCount+2, totalVertexCount+3, face.getColor());	
				}	
				
				totalVertexCount += 4;
			}
		}
												
		return masterMengerCube;
	}
	
	
	
	/**
	 * Returns the 20 new sub cubes that result from a menger sponge iteration
	 * 
	 * cubesize - size of new cubes (one third of original size)
	 * */
	public static Model3D[] toMengerSponge(Model3D cube, int cubeSize, Color col, boolean removeHiddenFaces) {

		Model3D[] result = new Model3D[20];
		int counter = 0;

		for (int r = 0; r < 3; r++) {
			for (int s = 0; s < 3; s++) {
				for (int t = 0; t < 3; t++) {

					boolean putSegmentCube;
					if ((r == 1 && s == 1) || (s == 1 && t == 1) || (t == 1 && r == 1)) {
						putSegmentCube = false;
					} else {
						putSegmentCube = true;
					}

					if (putSegmentCube) {
						Model3D segmentCube = Model3DGenerator.buildCube(cubeSize, col);
						segmentCube.setOrigin(cube.getOrigin().copy());
						segmentCube.move(r * cubeSize, s * cubeSize, t * cubeSize);
						// segmentCube.getMatrix().apply(cube.getMatrix().getArray());
						// segmentCube.setMatrix(cube.getMatrix());
						result[counter++] = segmentCube;
					}
				}
			}
		}

		if (removeHiddenFaces) {
			removeHiddenFaces(result);
		}

		return result;
	}
	
	
	
	/**
	 * Transforms the given mesh into individual faces. 
	 * Vertices that are shared thus become multiplied.
	 * 
	 * Given model must be made of triangles or quads.
	 * */
	public static Model3D facify(Model3D model){
		Model3D result = new Model3D();		
		int totalVertexCount = 0;
		
		for(Face face : model.getFaces()){
			Point3D[] vertices = face.getVertices();
			for(int i=0;i<vertices.length;i++){
				Point3D vertex = vertices[i].copy();
				result.addVertex(vertex);
				
//				if (i == 0) {
//					vertex.u = 0f;
//					vertex.v = 0f;
//				} else if (i == 1) {
//					vertex.u = 0f;
//					vertex.v = 1f;
//				} else if (i == 2) {
//					vertex.u = 1f;
//					vertex.v = 1f;
//				} else {
//					vertex.u = 1f;
//					vertex.v = 0f;
//				}
			}
			
//			if(vertices.length==3){
//				result.stretchFace(totalVertexCount, totalVertexCount+1, totalVertexCount+2, face.getColor());
//				totalVertexCount+=3;
//			}
//			else if(vertices.length==4){
			if(vertices.length==4){
				
				if(face.normal_y<0){ // otherwise normal mapping will produce inverted output
					result.stretchFace(totalVertexCount+2, totalVertexCount+3, totalVertexCount, totalVertexCount+1, face.getColor());
				}else{
					result.stretchFace(totalVertexCount, totalVertexCount+1, totalVertexCount+2, totalVertexCount+3, face.getColor());	
				}			
				totalVertexCount+=4;
			}
			else{
				throw new RuntimeException("Only quads are supported!");
			}			
		}
		
		return result;
	}
	
	
	/**
	 * Assumes the specified array contains cubes with touching sides
	 * */
	public static void removeHiddenFaces(Model3D[] cubes) {
			
//		System.out.println(">> removeHiddenFaces");
		
		Map<Face, Model3D> face2model = new HashMap<Face, Model3D>();		
		for (Model3D cube : cubes) {
			for (Face face : cube.getFaces()) {
				face2model.put(face, cube);						
			}
		}
				
		List<Face> allFaces = new ArrayList<Face>(face2model.keySet());
		Set<Face> toDelete = new HashSet<Face>();
		
		int numFaces = allFaces.size();
		for(int i=0;i<numFaces-1;i++){
			Face face_i = allFaces.get(i);
			
			Point3D face_i_centroid = new Point3D();
			face_i_centroid.add(face2model.get(face_i).getOrigin());
			face_i_centroid.add(face_i.getCentroid());
			
			for(int j=i+1;j<numFaces;j++){
				Face face_j = allFaces.get(j);				
				
				Point3D face_j_centroid = new Point3D();
				face_j_centroid.add(face2model.get(face_j).getOrigin());
				face_j_centroid.add(face_j.getCentroid());
				
				if (face_i_centroid.distanceSquared(face_j_centroid) < 0.01f) {
					toDelete.add(face_i);
					toDelete.add(face_j);
					break;
				}
			}
		}
		
//		System.out.println("removeHiddenFaces: toDelete: "+toDelete.size());

		for (Face face : toDelete) {
			Model3D model = face2model.get(face);
			model.deleteFace(face);
		}
		
//		System.out.println("<< removeHiddenFaces");
	}
	
	
	
	/**
	 * Performs a laplacian smoothing pass, i.e. adjusting each vertex position based on the average of its neighbors positions. 
	 * The given model must resemble a grid as obtained e.g. by buildTerrainMeshFromHeightMap.
	 * */
	public static void laplacianSmooth(int w, int h, Model3D mesh){		
					
		float[] newHeightValues = new float[w*h];
		
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){
				
				if(y==0 || x==0 || y==h-1 || x==w-1){
					continue;
				}
				
				Point3D vertex = mesh.getVertices().get(w*y+x);								

				float neighUpHeight = mesh.getVertices().get(w*(y-1)+x).y;
				float neighDownHeight = mesh.getVertices().get(w*(y+1)+x).y;				
				float neighLeftHeight  = mesh.getVertices().get(w*y+x-1).y;
				float neighRightHeight = mesh.getVertices().get(w*y+x+1).y;
				
				float avgNeighHeight = 0.25f * (neighUpHeight + neighDownHeight + neighLeftHeight + neighRightHeight);
				
				// durchschnitt aus alter höhe und durchschnittlicher nachbar höhe
				float newHeight = 0.5f * vertex.y  +  0.5f * avgNeighHeight; 				
				// wert merken und später setzen!
				newHeightValues[w*y+x] = newHeight;
			}
		}
		
		
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){				
				if(y==0 || x==0 || y==h-1 || x==w-1){
					continue;
				}
				Point3D vertex = mesh.getVertices().get(w*y+x);	
				vertex.y = newHeightValues[w*y+x];
			}
		}
		
		
		mesh.recomputeFaceNormals();			
	}
	
	
	
	/**
	 * Assumes a given model made up of triangles only. Cuts each triangle into two by splitting the hypotenuse in half.
	 * */
	public static void simpleSubdivide(Model3D model){
		
//		System.out.println("Number of triangles before subdivision: "+model.getFaces().size());
				
		Collection<Face> newFaces = new ArrayList<Face>(model.getFaces().size() * 2);
		
		for(Face face : model.getFaces()){			
			
			/*
			 *   B------C
			 *   |\    /
			 *   | \  /
			 *   |	\/   
			 *   |  / D
			 *   | /
			 *   |/
			 *   A
			 * 
			 * find hypotenuse and cut in half. find new vertex D by interpolation between end points of hypotenuse
			 * 
			 * 
			 */
			
			Point3D[] vertices = face.getVertices();
			
			Point3D a = vertices[0];
			Point3D b = vertices[1];
			Point3D c = vertices[2];
			
			Vector3D bc = new Vector3D(b, c);
			Vector3D ca = new Vector3D(c, a);
			
			Point3D hypotenuseL = a;
			Point3D hypotenuseR = b;
			Point3D oppositeD = c;
			
			if(bc.length() > hypotenuseL.distance(hypotenuseR)){
				hypotenuseL = b;
				hypotenuseR = c;
				oppositeD = a;
			}
			if(ca.length() > hypotenuseL.distance(hypotenuseR)){
				hypotenuseL = c;
				hypotenuseR = a;
				oppositeD = b;
			}
			
			Point3D d = hypotenuseL.copy();
			d.lerp(hypotenuseR, 0.5f);
			
			Point3D vertexAtD = model.findVertex(d, 0.1f);
			if(vertexAtD==null){
				model.addVertex(d);				
			}else{
				d = vertexAtD;
			}			
															
			Face newFace1 = new Face(hypotenuseL, d, oppositeD, face.getColor());			
			Face newFace2 = new Face(d, hypotenuseR, oppositeD, face.getColor());		
			
			newFaces.add(newFace1);
			newFaces.add(newFace2);				
		}
		
		// remove all faces and add new ones
		model.removeFaces();
		for (Face face : newFaces) {
			model.addFace(face);
		}		
		
//		System.out.println("Number of triangles after subdivision: "+model.getFaces().size());
	}
	
	
	/**
	 * Assumes the given model is made up of triangles only. It then splits each triangle into 4 new.	 
	 * */
	public static void loopSubdivide(Model3D model){
		
//		System.out.println("Number of triangles before subdivision: "+model.getFaces().size());
				
		Collection<Face> newFaces = new ArrayList<Face>(model.getFaces().size() * 4);
		
		for(Face face : model.getFaces()){			
			
			Point3D[] vertices = face.getVertices();
			
			Point3D a = vertices[0];
			Point3D b = vertices[1];
			Point3D c = vertices[2];
			
			/*
			 * alle drei kanten halbieren (3 neue vertices)
			 			 * 
			 * */
			
			Point3D d = a.copy();
			Point3D e = b.copy();
			Point3D f = c.copy();			
			d.lerp(b, 0.5f);						
			e.lerp(c, 0.5f);						
			f.lerp(a, 0.5f);
			
			Point3D vertexAtD = model.findVertex(d, 0.1f);
			if(vertexAtD==null){
				model.addVertex(d);				
			}else{			
				d = vertexAtD;
			}
			
			Point3D vertexAtE = model.findVertex(e, 0.1f);
			if(vertexAtE==null){
				model.addVertex(e);				
			}else{
				e = vertexAtE;
			}
			
			Point3D vertexAtF = model.findVertex(f, 0.1f);
			if(vertexAtF==null){
				model.addVertex(f);				
			}else{
				f = vertexAtF;
			}
			
			Face f1 = new Face(a, d, f, face.getColor());
			Face f2 = new Face(d, b, e, face.getColor());
			Face f3 = new Face(e, c, f, face.getColor());
			Face f4 = new Face(d, e, f, face.getColor());
			
			newFaces.add(f1);
			newFaces.add(f2);
			newFaces.add(f3);
			newFaces.add(f4);
		}
		
		// remove all faces and add new ones
		model.removeFaces();
		for (Face face : newFaces) {
			model.addFace(face);
		}
		
//		System.out.println("Number of triangles after subdivision: "+model.getFaces().size());
	}
	
	
	/**
	 * Returns all connected regions in a given model. 
	 * Two regions are connected if they share a vertex.
	 * **/
	public static List<Model3D> splitToParts(Model3D model){
		
		Graph modelGraph = Toolbox.toGraph(model);
		
//		System.out.println("model graph; #nodes: "+modelGraph.countNodes());
//		System.out.println("model graph; #edges: "+modelGraph.countEdges());		
//		System.out.println("model graph; #areas: "+modelGraph.coherentAreas());
		
		List<Model3D> result = new ArrayList<Model3D>();	
		
		List<Point3D> vertices = model.getVertices();					
		boolean[] verticesSeen = new boolean[vertices.size()];
		for(int i=0;i<vertices.size();i++){			
											
			if(verticesSeen[i]){ // waren wir schon hier?
				continue;
			}
						
			List<Node> nodeList = modelGraph.getArea(i+1);
	
			// all vertices  in einen topf werfen
			// bekommen dann über vertex2face mapping aus dem ausgangsmodell die richtigen faces
			
			Model3D part = new Model3D();
			Set<Face> faces = new HashSet<Face>();
			
			for(Node node : nodeList){
				Point3D vertex = vertices.get(node.getId()-1);
				part.addVertex(vertex);
				faces.addAll(model.getFaces(vertex));
				
				verticesSeen[node.getId()-1] = true;
			}
			
			for (Face face : faces) {
				part.addFace(face);
			}
	
			if(part.getFaces().size()>0){
				result.add(part);
			}
//			else{
//				System.out.println("Ignoring empty part");
//			}
		}
		
		return result;
	}
	
	
	public static Model3D split(Model3D model){
		Collection<Model3D> parts = Model3DGenerator.splitToParts(model);
		model = new Model3D(model.getOrigin());
		for (Model3D part : parts) {			
			model.addSubModel(part);
		}
		return model;
	}
	
	
	/**
	 * Adjusts origin (pivot point!) of given model so that afterwards all coordinates are relative to its bbox centerpoint.
	 * */
	public static void normalizeOrigin(Model3D model){
		float[] bbox = model.getBBox();
		
		float minx = bbox[0];
		float maxx = bbox[1];
		float miny = bbox[2];
		float maxy = bbox[3];
		float minz = bbox[4];
		float maxz = bbox[5];
			
		Point3D center = new Point3D(minx + (maxx-minx)/2f, miny + (maxy-miny)/2f, minz + (maxz-minz)/2f);
		
		model.getOrigin().setCoordinates(0, 0, 0);
		
		List<Point3D> vertices = new ArrayList<Point3D>(model.getVertices());
		// nur direkt kinder?!
		for(Object child : model.getSubModels()){
			vertices.addAll( ((Model3D)child).getVertices() );
		}
		
		for(Point3D vertex : vertices){
			float new_x = vertex.x - center.x;
			float new_y = vertex.y - center.y;
			float new_z = vertex.z - center.z;			
			vertex.setCoordinates(new_x, new_y, new_z);
		}		
		
		List<Face> faces = new ArrayList<Face>(model.getFaces());
		// nur direkt kinder?!
		for(Object child : model.getSubModels()){
			faces.addAll( ((Model3D)child).getFaces() );
		}
		
		for(Face face : faces){
			face.recompute();
		}
	}
}
