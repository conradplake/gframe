package gframe.engine;

import java.awt.Color;

import imaging.ImageRaster;


/**
 * Can rasterize any convex n-gon (triangles, quads, etc.)
 * */
public class DefaultConvexPolygonRasterizer implements Rasterizer {

	public final static int SCREEN_EDGE_LEFT = 1;
	public final static int SCREEN_EDGE_RIGHT = 2;
	public final static int SCREEN_EDGE_UP = 3;
	public final static int SCREEN_EDGE_DOWN = 4;	
	public final static int SCREEN_EDGE_NEAR = 5;
	public final static int SCREEN_EDGE_FAR = 6;

	protected int xoffset;
	protected int yoffset;
	protected int frameX;
	protected int frameY;

	protected EdgeTableEntry[] edgeTable;
	
	float[] edgeDeltas;
	
	int shadowColorRGB = Color.black.getRGB();
	

	public DefaultConvexPolygonRasterizer(int frameX, int frameY) {
		this(frameX / 2, frameY / 2, frameX, frameY);		
	}

	public DefaultConvexPolygonRasterizer(int xoffset, int yoffset, int frameX, int frameY) {
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		this.frameX = frameX;
		this.frameY = frameY;

		this.edgeDeltas = new float[11];
		
		edgeTable = new EdgeTableEntry[frameY];
		for (int i = 0; i < edgeTable.length; i++) {
			edgeTable[i] = new EdgeTableEntry();
		}
	}

	public ImageRaster createEmptyImageRaster() {
		return new ImageRaster(frameX, frameY, new int[frameX * frameY]);
	}

	public ZBuffer createZBuffer() {
		ZBuffer zBuffer = new ZBuffer(frameX, frameY);
		zBuffer.clear();
		return zBuffer;
	}

	public void setScreenSize(int xoffset, int yoffset, int frameX, int frameY) {
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		this.frameX = frameX;
		this.frameY = frameY;
		
		edgeTable = new EdgeTableEntry[frameY];
		for (int i = 0; i < edgeTable.length; i++) {
			edgeTable[i] = new EdgeTableEntry();
		}
	}
	

	/**
	 * Performs a scan line interpolation. Each scan line is represented at
	 * least by a min_x, max_x, min_z, max_z value. All pixels in between are
	 * painted using the specified shader.
	 * 
	 */
	@Override
	public void rasterize(RenderFace renderFace, ImageRaster colorBuffer, ZBuffer zBuffer, Shader shader) {			
		boolean doOnlyZPass  = colorBuffer == null && zBuffer!=null;
		int[] minMaxY = interpolateEdges(renderFace, doOnlyZPass);
		interpolateScanlines(renderFace, colorBuffer, zBuffer, shader, minMaxY[0], minMaxY[1], doOnlyZPass);
	}
	
	
	public int[] interpolateEdges(RenderFace renderFace, boolean onlyZPass) {
	
		// Determine bounding y-line
		int minY = Integer.MAX_VALUE;
		int maxY = 0;
		
				
		// interpolate along each polygon edge
		for (int i = 0; i < renderFace.vertices.length; i++) {

			int next = i + 1;
			if (next == renderFace.vertices.length)
				next = 0;
									
			int[] minYmaxY = interpolateEdge(renderFace, i, next, onlyZPass); // current edge: i -> next
			
			if(minYmaxY!=null){ // EDGE GOT CLIPPED?
				if(minYmaxY[0]<minY)
					minY = minYmaxY[0];
				
				if(minYmaxY[1]>maxY)
					maxY = minYmaxY[1];
			}
			
										
		}
		
		return new int[]{minY, maxY};
	}
	

	
	/**
	 * Interpolate along edge and fill edgeTable.
	 * */
	public int[] interpolateEdge(RenderFace renderFace, int vertex_i0, int vertex_i1, boolean onlyZPass) {
		
		int topVertex = vertex_i0;
		int botVertex = vertex_i1;
		
		if(yoffset-renderFace.cam_Y[topVertex] > yoffset-renderFace.cam_Y[botVertex]){
			topVertex = vertex_i1;
			botVertex = vertex_i0;
		}							
			
		float screen_y_top = yoffset - renderFace.cam_Y[topVertex];		
		float screen_y_bot = yoffset - renderFace.cam_Y[botVertex];
		
		
		// CLIP EDGE
		if(screen_y_bot<0 || screen_y_top>frameY){
			return null;
		}	
		//----------
		
		
		float lineHeight = screen_y_bot - screen_y_top;
		
		//sub pixel height
		float subPix = (int)screen_y_top - screen_y_top;		
		
		float cutOff = 0; // for pre-interpolation of negative part (outside top of screen)
		if(screen_y_top<0){
			cutOff = -screen_y_top;
		}
		
		float inverseHeight = 0;
		if(lineHeight>1){
			inverseHeight = 1f/lineHeight;			
		}		
		
	
		// compute relative deltas
		edgeDeltas[0] = (renderFace.cam_X[botVertex] - renderFace.cam_X[topVertex]) * inverseHeight;
		edgeDeltas[1] = (renderFace.cam_Z[botVertex] - renderFace.cam_Z[topVertex]) * inverseHeight;
		
		edgeDeltas[2] = (renderFace.zFactors[botVertex] - renderFace.zFactors[topVertex]) * inverseHeight;
		
		edgeDeltas[3] = (renderFace.texel_U[botVertex] - renderFace.texel_U[topVertex]) * inverseHeight;
		edgeDeltas[4] = (renderFace.texel_V[botVertex] - renderFace.texel_V[topVertex]) * inverseHeight;
						
		edgeDeltas[5] = (renderFace.vertices[botVertex].normal_x - renderFace.vertices[topVertex].normal_x) * inverseHeight;
		edgeDeltas[6] = (renderFace.vertices[botVertex].normal_y - renderFace.vertices[topVertex].normal_y) * inverseHeight;
		edgeDeltas[7] = (renderFace.vertices[botVertex].normal_z - renderFace.vertices[topVertex].normal_z) * inverseHeight;
						
		edgeDeltas[8]  = (renderFace.pcorrectedWorld_X[botVertex] - renderFace.pcorrectedWorld_X[topVertex]) * inverseHeight;
		edgeDeltas[9]  = (renderFace.pcorrectedWorld_Y[botVertex] - renderFace.pcorrectedWorld_Y[topVertex]) * inverseHeight;
		edgeDeltas[10] = (renderFace.pcorrectedWorld_Z[botVertex] - renderFace.pcorrectedWorld_Z[topVertex]) * inverseHeight;
		
		int top = clipY( (int)(screen_y_top) );
		int bot = clipY( (int)(screen_y_bot) );				
//		int top = clipY( (int)(Math.ceil(screen_y_top)) );
//		int bot = clipY( (int)(Math.ceil(screen_y_bot)) );
		
		int height = bot - top;
		
		fillEdgeTableEntry(edgeTable[top], renderFace, topVertex, onlyZPass, cutOff, 0, edgeDeltas);
		for(int h=1;h<height;h++){
			fillEdgeTableEntry(edgeTable[top+h], renderFace, topVertex, onlyZPass, cutOff+h, subPix, edgeDeltas);
		}		
		fillEdgeTableEntry(edgeTable[bot], renderFace, topVertex, onlyZPass, lineHeight, 0, edgeDeltas);
		
		return new int[]{top, bot};				
	}
	
	
	public void interpolateScanlines(RenderFace renderFace, ImageRaster colorBuffer, ZBuffer zBuffer,
				Shader shader, int minY, int maxY, boolean doOnlyZPass) {	
			
			int rgb = shader == null ? renderFace.col.getRGB() : shader.shade(renderFace);		
			
			
			Lightsource lightsource = shader!=null? shader.getLightsource() : null;
			boolean shadowsEnabled = lightsource!=null? lightsource.isShadowsEnabled() : false;				
			Matrix3D inverseLightMatrix = shadowsEnabled? lightsource.getInverseMatrix() : null;			
			ZBuffer shadowMap = shadowsEnabled? lightsource.getDepthMap() : null;
			
			for (int draw_y = minY; draw_y <= maxY; draw_y++) {
	
				EdgeTableEntry edgeTableEntry = edgeTable[draw_y]; 			// each
																			// entry
																			// represents
																			// one
																			// scan
																			// line
	
				if (!edgeTableEntry.visited) {
					continue;
				}
	
				final float iScanlineLength = 1f / (edgeTableEntry.max_draw_x - edgeTableEntry.min_draw_x + 1);
							
				final float pcorr_world_dx_stepfactor = (edgeTableEntry.max_pcorrectedWorld_x - edgeTableEntry.min_pcorrectedWorld_x) * iScanlineLength;						
				final float pcorr_world_dy_stepfactor = (edgeTableEntry.max_pcorrectedWorld_y - edgeTableEntry.min_pcorrectedWorld_y) * iScanlineLength;			
				final float pcorr_world_dz_stepfactor = (edgeTableEntry.max_pcorrectedWorld_z - edgeTableEntry.min_pcorrectedWorld_z) * iScanlineLength;
				
				final float texel_du_stepfactor = (edgeTableEntry.max_texel_u - edgeTableEntry.min_texel_u) * iScanlineLength;			
				final float texel_dv_stepfactor = (edgeTableEntry.max_texel_v - edgeTableEntry.min_texel_v) * iScanlineLength;
						
				final float zfactor_stepfactor = (edgeTableEntry.max_zFactor - edgeTableEntry.min_zFactor) * iScanlineLength;
				
				final float vn_dx_stepfactor = (edgeTableEntry.max_normal_x - edgeTableEntry.min_normal_x) * iScanlineLength;			
				final float vn_dy_stepfactor = (edgeTableEntry.max_normal_y - edgeTableEntry.min_normal_y) * iScanlineLength;			
				final float vn_dz_stepfactor = (edgeTableEntry.max_normal_z - edgeTableEntry.min_normal_z) * iScanlineLength;
	
				if (edgeTableEntry.min_draw_x < 0) {
					// das abgeschnittene ende auf alle min-werte drauf
					// interpolieren
					
					edgeTableEntry.min_pcorrectedWorld_x += -edgeTableEntry.min_draw_x * pcorr_world_dx_stepfactor;
					edgeTableEntry.min_pcorrectedWorld_y += -edgeTableEntry.min_draw_x * pcorr_world_dy_stepfactor;
					edgeTableEntry.min_pcorrectedWorld_z += -edgeTableEntry.min_draw_x * pcorr_world_dz_stepfactor;
	
					// texel
					edgeTableEntry.min_texel_u += -edgeTableEntry.min_draw_x * texel_du_stepfactor;
					edgeTableEntry.min_texel_v += -edgeTableEntry.min_draw_x * texel_dv_stepfactor;
					edgeTableEntry.min_zFactor += -edgeTableEntry.min_draw_x * zfactor_stepfactor;
	
					// vertex normals
					edgeTableEntry.min_normal_x += -edgeTableEntry.min_draw_x * vn_dx_stepfactor;
					edgeTableEntry.min_normal_y += -edgeTableEntry.min_draw_x * vn_dy_stepfactor;
					edgeTableEntry.min_normal_z += -edgeTableEntry.min_draw_x * vn_dz_stepfactor;
	
					edgeTableEntry.min_draw_x = 0;
				}
				if (edgeTableEntry.max_draw_x >= frameX) {
					edgeTableEntry.max_draw_x = frameX - 1;
				}
	
				
				final int startX = (int)edgeTableEntry.min_draw_x;
				final float endX = edgeTableEntry.max_draw_x;
				
				// sub-tex accuracy
				final float subTex = (float)startX - edgeTableEntry.min_draw_x;
	//			subTex = 0;
	//			System.out.println("subTex="+subTex);
				
				float dx=-1;
				for (int draw_x = startX; draw_x <= endX; draw_x++) {
	
					//final float dx = draw_x - startX;
					dx++;
					
					float zfa = edgeTableEntry.min_zFactor + (dx * zfactor_stepfactor);					
					
					if(dx>0){
						zfa += zfactor_stepfactor * subTex;
					}
					
					float inverseZfactor = 1/zfa;
					
					float draw_z = Engine3D.inverseZFactor(zfa, inverseZfactor);
					
					if (zBuffer.update(draw_x, draw_y, draw_z)) {
						
						if(doOnlyZPass)
							continue;
	
						if (shader != null && shader.isPerPixelShader()) {
							
							// lerp, lerp...
							float pcorr_world_x = edgeTableEntry.min_pcorrectedWorld_x + dx * pcorr_world_dx_stepfactor;
							float pcorr_world_y = edgeTableEntry.min_pcorrectedWorld_y + dx * pcorr_world_dy_stepfactor;
							float pcorr_world_z = edgeTableEntry.min_pcorrectedWorld_z + dx * pcorr_world_dz_stepfactor;
							
							float texel_u = edgeTableEntry.min_texel_u + dx * texel_du_stepfactor;
							float texel_v = edgeTableEntry.min_texel_v + dx * texel_dv_stepfactor;
							
							
							// vertex normals
							float vn_x = edgeTableEntry.min_normal_x + dx * vn_dx_stepfactor;
							float vn_y = edgeTableEntry.min_normal_y + dx * vn_dy_stepfactor;
							float vn_z = edgeTableEntry.min_normal_z + dx * vn_dz_stepfactor;
	
							
							// sub-tex accuracy
							if(dx>0){								
								pcorr_world_x += pcorr_world_dx_stepfactor * subTex;
								pcorr_world_y += pcorr_world_dy_stepfactor * subTex;
								pcorr_world_z += pcorr_world_dz_stepfactor * subTex;
								
								texel_u += texel_du_stepfactor * subTex;
								texel_v += texel_dv_stepfactor * subTex;
								
								vn_x += vn_dx_stepfactor * subTex;
								vn_y += vn_dy_stepfactor * subTex;
								vn_z += vn_dz_stepfactor * subTex;													
							}	
							
							
							// perspektiven korrektur
							texel_u *= inverseZfactor;
							texel_v *= inverseZfactor;

							// perspektivenkorrektur der weltkoordinaten aus kamera sicht								
							pcorr_world_x *= inverseZfactor;
							pcorr_world_y *= inverseZfactor;
							pcorr_world_z *= inverseZfactor;
							
//							vn_x *= inverseZfactor;
//							vn_y *= inverseZfactor;
//							vn_z *= inverseZfactor;
												
							// Shadow mapping: anhand der world space coordinaten noch den punkt im light space bestimmen
							if(shadowMap!=null){
								float[] lightCoords = inverseLightMatrix.transform(pcorr_world_x - lightsource.x, pcorr_world_y - lightsource.y, pcorr_world_z - lightsource.z);
																						
								// perspektiven korrektur innerhalb des light space
								float zf = Engine3D.zFactor(lightCoords[2]);
								lightCoords[0] = lightCoords[0] * zf;
								lightCoords[1] = lightCoords[1] * zf;
								
								// to "screenspace" (weil ja auf diese weise auch die werte beim eintragen in die shadow map berechnet wurden)						
								float light_x = shadowMap.xoffset + lightCoords[0];
								float light_y = shadowMap.yoffset - lightCoords[1];
								
								if(light_x<0 || light_x>=shadowMap.w || light_y<0 || light_y>=shadowMap.h){ // alles ausserhalb des lichtkegels ist im schatten								
									
									if(lightsource.isSpotLight()){
										colorBuffer.setPixel(draw_x, draw_y, shadowColorRGB);
										continue; // TODO: schatten-info an den shader weiter geben und ihn machen lassen
									}
								}
								else{
									if(shadowMap.getValue(light_x, light_y, true) < lightCoords[2] - /*bias gegen schatten akne=*/2){ // etwas anderes war näher dran -> wir sind im schatten
										colorBuffer.setPixel(draw_x, draw_y, shadowColorRGB);
										continue; // TODO: schatten-info an den shader weiter geben und ihn machen lassen
									}								
								}
							}
							
							rgb = shader.shade(renderFace, pcorr_world_x, pcorr_world_y, pcorr_world_z, vn_x, vn_y, vn_z, texel_u, texel_v, draw_x, draw_y);
						}
						
						colorBuffer.setPixel(draw_x, draw_y, rgb);
					}
	
				} // draw_x
	
				edgeTableEntry.clear();
	
			} // draw_y
		}

	private void fillEdgeTableEntry(EdgeTableEntry edgeTableEntry, RenderFace renderFace, int startVertex, boolean onlyZPass, float height, float subPix, float[] deltas){

		float draw_x = xoffset + renderFace.cam_X[startVertex] + height*deltas[0] + subPix*deltas[0];
		if(draw_x < edgeTableEntry.min_draw_x || !edgeTableEntry.visited){
			
			edgeTableEntry.min_draw_x = draw_x;			
			edgeTableEntry.min_zFactor = renderFace.zFactors[startVertex] + height*deltas[2] + subPix*deltas[2];
			
			if(!onlyZPass){
				edgeTableEntry.min_texel_u = renderFace.texel_U[startVertex] + height*deltas[3] + subPix*deltas[3];
				edgeTableEntry.min_texel_v = renderFace.texel_V[startVertex] + height*deltas[4] + subPix*deltas[4];
				
				edgeTableEntry.min_normal_x = renderFace.vertices[startVertex].normal_x + height*deltas[5] + subPix*deltas[5];
				edgeTableEntry.min_normal_y = renderFace.vertices[startVertex].normal_y + height*deltas[6] + subPix*deltas[6];
				edgeTableEntry.min_normal_z = renderFace.vertices[startVertex].normal_z + height*deltas[7] + subPix*deltas[7];				
				
				edgeTableEntry.min_pcorrectedWorld_x = renderFace.pcorrectedWorld_X[startVertex] + height*deltas[8] + subPix*deltas[8];
				edgeTableEntry.min_pcorrectedWorld_y = renderFace.pcorrectedWorld_Y[startVertex] + height*deltas[9] + subPix*deltas[9];
				edgeTableEntry.min_pcorrectedWorld_z = renderFace.pcorrectedWorld_Z[startVertex] + height*deltas[10] + subPix*deltas[10];	
			}											
		}
		if(draw_x > edgeTableEntry.max_draw_x || !edgeTableEntry.visited){
			
			edgeTableEntry.max_draw_x = draw_x;			
			edgeTableEntry.max_zFactor = renderFace.zFactors[startVertex] + height*deltas[2] + subPix*deltas[2];
			
			if(!onlyZPass){								
				edgeTableEntry.max_texel_u = renderFace.texel_U[startVertex] + height*deltas[3] + subPix*deltas[3];
				edgeTableEntry.max_texel_v = renderFace.texel_V[startVertex] + height*deltas[4] + subPix*deltas[4];		
				
				edgeTableEntry.max_normal_x = renderFace.vertices[startVertex].normal_x + height*deltas[5] + subPix*deltas[5];
				edgeTableEntry.max_normal_y = renderFace.vertices[startVertex].normal_y + height*deltas[6] + subPix*deltas[6];
				edgeTableEntry.max_normal_z = renderFace.vertices[startVertex].normal_z + height*deltas[7] + subPix*deltas[7];				
				
				edgeTableEntry.max_pcorrectedWorld_x = renderFace.pcorrectedWorld_X[startVertex] + height*deltas[8] + subPix*deltas[8];
				edgeTableEntry.max_pcorrectedWorld_y = renderFace.pcorrectedWorld_Y[startVertex] + height*deltas[9] + subPix*deltas[9];
				edgeTableEntry.max_pcorrectedWorld_z = renderFace.pcorrectedWorld_Z[startVertex] + height*deltas[10] + subPix*deltas[10];	
			}							
			
		}
		
		edgeTableEntry.visited = true;
	}
	
	

	public boolean isOutsideScreen(RenderFace renderFace) {
		if (renderFace.maxX() < -xoffset) {
			return true;
		}
		if (renderFace.minX() > xoffset) {
			return true;
		}

		if (renderFace.minY() > yoffset) {
			return true;
		}
		if (renderFace.maxY() < -yoffset) {
			return true;
		}

		return false;
	}

	
	public Point3D[] clipLine(Point3D a, Point3D b, int orientation) {

		float da = 0; // distance a to screen edge
		float db = 0; // distance b to screen edge

		if (orientation == SCREEN_EDGE_LEFT) {
			da = a.x + xoffset;
			db = b.x + xoffset;
		} else if (orientation == SCREEN_EDGE_RIGHT) {
			da = -(a.x - xoffset);
			db = -(b.x - xoffset);
		} else if (orientation == SCREEN_EDGE_UP) {
			da = yoffset - a.y;
			db = yoffset - b.y;
		} else if (orientation == SCREEN_EDGE_DOWN) {
			da = a.y + yoffset;
			db = b.y + yoffset;
		} else if (orientation == SCREEN_EDGE_NEAR) {
			da = a.z - 1;
			db = b.z - 1;
		}

		if (da < 0 && db < 0) {
			return null;
		}

		if (da < 0 && db >= 0) {
			float s = da / (da - db); // intersection factor (between 0 and 1)
			Point3D intersectionpoint = new Point3D(a.x + s * (b.x - a.x), a.y + s * (b.y - a.y),
					a.z + s * (b.z - a.z));
			return new Point3D[] { intersectionpoint, b };
		}
		if (db < 0 && da >= 0) {
			float s = da / (da - db); // intersection factor (between 0 and 1)
			Point3D intersectionpoint = new Point3D(a.x + s * (b.x - a.x), a.y + s * (b.y - a.y),
					a.z + s * (b.z - a.z));
			return new Point3D[] { a, intersectionpoint };
		}

		return new Point3D[] { a, b };
	}

	
	public int clipX(int x) {
		if (x < 0) {
			x = 0;
		}
		if (x >= frameX) {
			x = frameX - 1;
		}
		return x;
	}
	

	public int clipY(int y) {
		if (y < 0) {
			y = 0;
		}
		if (y >= frameY) {
			y = frameY - 1;
		}
		return y;
	}
	

	public boolean allInside(Point3D[] vertices) {
		boolean result = true;
		for (Point3D point3d : vertices) {
			if (point3d.x < -xoffset || point3d.x > xoffset || point3d.y > yoffset || point3d.y < -yoffset) {
				result = false;
				break;
			}
		}
		return result;
	}
	

	public boolean allOutside(Point3D[] vertices) {
		boolean result = true;
		for (Point3D point3d : vertices) {
			if (point3d.x >= -xoffset || point3d.x <= xoffset || point3d.y <= yoffset || point3d.y >= -yoffset) {
				result = false;
				break;
			}
		}
		return result;
	}


	protected class EdgeTableEntry {

		boolean visited;
		
		float min_draw_x;
		float max_draw_x;

		float min_texel_u;
		float min_texel_v;
		float max_texel_u;
		float max_texel_v;
		float min_zFactor;
		float max_zFactor;

		float min_normal_x;
		float min_normal_y;
		float min_normal_z;
		float max_normal_x;
		float max_normal_y;
		float max_normal_z;

		float min_pcorrectedWorld_x;
		float min_pcorrectedWorld_y;
		float min_pcorrectedWorld_z;
		float max_pcorrectedWorld_x;
		float max_pcorrectedWorld_y;
		float max_pcorrectedWorld_z;

		public final void clear() {

			min_draw_x = 0;
			max_draw_x = 0;

			min_texel_u = 0;
			min_texel_v = 0;
			max_texel_u = 0;
			max_texel_v = 0;
			min_zFactor = 0;
			max_zFactor = 0;

			min_normal_x = min_normal_y = min_normal_z = 0;
			max_normal_x = max_normal_y = max_normal_z = 0;

			min_pcorrectedWorld_x = min_pcorrectedWorld_y = min_pcorrectedWorld_z = 0;
			max_pcorrectedWorld_x = max_pcorrectedWorld_y = max_pcorrectedWorld_z = 0;
			
			visited = false;
		}

	}

}
