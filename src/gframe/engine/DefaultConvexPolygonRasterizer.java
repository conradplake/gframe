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
	
	
	// DEPRACATED BRUTE FORCE EDGE INTERPOLATION!!
//	public int[] interpolateEdges(RenderFace renderFace, boolean onlyZPass) {
//		// screen coordinates
//		int[] X = new int[renderFace.vertices.length];
//		int[] Y = new int[renderFace.vertices.length];
//		float[] Z = new float[renderFace.vertices.length];
//
//		// Determine bounding y-line
//		int minY = Integer.MAX_VALUE;
//		int maxY = 0;
//
//		for (int i = 0; i < renderFace.vertices.length; i++) {
//
//			// compute true screen coordinates
//			X[i] = xoffset + (int) Math.floor(renderFace.cam_X[i] + 0.5f);
//			Y[i] = yoffset - (int) Math.floor(renderFace.cam_Y[i] + 0.5f);
//			Z[i] = renderFace.cam_Z[i];
//
//			if (Y[i] < minY) {
//				minY = Y[i];
//			}
//			if (Y[i] > maxY) {
//				maxY = Y[i];
//			}
//		}
//
//		minY = clipY(minY);
//		maxY = clipY(maxY);
//
//		// interpolate along polygon edges
//		for (int i = 0; i < renderFace.vertices.length; i++) {
//
//			int next = i + 1;
//			if (next == renderFace.vertices.length)
//				next = 0;
//
//			int dx = X[next] - X[i];
//			int dy = Y[next] - Y[i];
//			// int dz = Z[next]-Z[i];
//
//			// edgeLength = #interpolation_steps
//			// float edgeLength = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
//			float edgeLength = Math.abs(dx) + Math.abs(dy);
//			// float edgeLength = (float)Math.sqrt(dx*dx + dy*dy);
//			// float edgeLength = Math.max(Math.abs(dx), Math.abs(dy));
//			// float edgeLength = Math.abs(dx)+Math.abs(dy)+Math.abs(dz);
//			// float edgeLength = Math.abs(dy)+1;
//			float iEdgeLength = 1 / edgeLength;
//
//			//
//			float dx_step = (X[next] - X[i]) * iEdgeLength;
//			float dy_step = (Y[next] - Y[i]) * iEdgeLength;
//			float dz_step = (Z[next] - Z[i]) * iEdgeLength;
//
//			//
//			float world_dx_step = (renderFace.vertices[next].x - renderFace.vertices[i].x) * iEdgeLength;
//			float world_dy_step = (renderFace.vertices[next].y - renderFace.vertices[i].y) * iEdgeLength;
//			float world_dz_step = (renderFace.vertices[next].z - renderFace.vertices[i].z) * iEdgeLength;
//
//			//
//			float pcorr_world_dx_step = (renderFace.pcorrectedWorld_X[next] - renderFace.pcorrectedWorld_X[i]) * iEdgeLength;
//			float pcorr_world_dy_step = (renderFace.pcorrectedWorld_Y[next] - renderFace.pcorrectedWorld_Y[i]) * iEdgeLength;			
//			float pcorr_world_dz_step = (renderFace.pcorrectedWorld_Z[next] - renderFace.pcorrectedWorld_Z[i]) * iEdgeLength;
//			
//			//
//			float texel_du_step = (renderFace.texel_U[next] - renderFace.texel_U[i]) * iEdgeLength;
//			float texel_dv_step = (renderFace.texel_V[next] - renderFace.texel_V[i]) * iEdgeLength;
//			float zFactor_step = (renderFace.zFactors[next] - renderFace.zFactors[i]) * iEdgeLength;
//
//			//
//			float vertexNormal_dx_step = (renderFace.vertices[next].normal_x - renderFace.vertices[i].normal_x)
//					* iEdgeLength;
//			float vertexNormal_dy_step = (renderFace.vertices[next].normal_y - renderFace.vertices[i].normal_y)
//					* iEdgeLength;
//			float vertexNormal_dz_step = (renderFace.vertices[next].normal_z - renderFace.vertices[i].normal_z)
//					* iEdgeLength;
//			
//			
//			boolean beenInside=false;
//			
//			//brute force interpolate along edge 
//			for (int j = 0; j <= edgeLength; j++) {
//						
//				int drawPixel_y = Y[i] + (int) Math.floor(j*dy_step + 0.5f);
//
//				if (drawPixel_y < 0 || drawPixel_y >= frameY) {
//					if(beenInside){						
//						break; // went from inside to outside of screen -> we can quit
//					}else{
//						continue;	
//					}									
//				}
//				
//				beenInside = true;
//
//				//EdgeTableEntry edgeTableEntry = edgeTable[drawPixel_y - minY];
//				EdgeTableEntry edgeTableEntry = edgeTable[drawPixel_y];
//
//				int drawPixel_x = X[i] + (int) Math.floor(j * dx_step + 0.5f); // lerp,
//																				// lerp
//																				// :)				
//
//				if (drawPixel_x < edgeTableEntry.min_draw_x || !edgeTableEntry.visited) {								
//					
//					edgeTableEntry.min_draw_x = drawPixel_x;
//
//					// z-value of draw pixel (needed for zBuffer comparison)
//					edgeTableEntry.min_draw_z = Z[i] + (j * dz_step);
//					
//					if(!onlyZPass){
//						// store world coordinates of current position
//						edgeTableEntry.min_world_x = (int) (renderFace.vertices[i].x + (j * world_dx_step));
//						edgeTableEntry.min_world_y = (int) (renderFace.vertices[i].y + (j * world_dy_step));
//						edgeTableEntry.min_world_z = (int) (renderFace.vertices[i].z + (j * world_dz_step));
//						
//						edgeTableEntry.min_pcorrectedWorld_x = renderFace.pcorrectedWorld_X[i] + (j * pcorr_world_dx_step);
//						edgeTableEntry.min_pcorrectedWorld_y = renderFace.pcorrectedWorld_Y[i] + (j * pcorr_world_dy_step);
//						edgeTableEntry.min_pcorrectedWorld_z = renderFace.pcorrectedWorld_Z[i] + (j * pcorr_world_dz_step);
//
//						// texel coords
//						edgeTableEntry.min_texel_u = renderFace.texel_U[i] + (j * texel_du_step);
//						edgeTableEntry.min_texel_v = renderFace.texel_V[i] + (j * texel_dv_step);
//						
//						// perspective correction factor
//						edgeTableEntry.min_zFactor = renderFace.zFactors[i] + (j * zFactor_step);
//
//						// vertex normal
//						edgeTableEntry.min_normal_x = renderFace.vertices[i].normal_x + (j * vertexNormal_dx_step);
//						edgeTableEntry.min_normal_y = renderFace.vertices[i].normal_y + (j * vertexNormal_dy_step);
//						edgeTableEntry.min_normal_z = renderFace.vertices[i].normal_z + (j * vertexNormal_dz_step);	
//					}
//										
//				}
//				if (drawPixel_x > edgeTableEntry.max_draw_x || !edgeTableEntry.visited) {
//					edgeTableEntry.max_draw_x = drawPixel_x;
//
//					// z-value of pixel (for zBuffer comparison)
//					edgeTableEntry.max_draw_z = Z[i] + (j * dz_step);
//
//					if(!onlyZPass){
//						// store world coordinates of current position
//						edgeTableEntry.max_world_x = (int) (renderFace.vertices[i].x + (j * world_dx_step));
//						edgeTableEntry.max_world_y = (int) (renderFace.vertices[i].y + (j * world_dy_step));
//						edgeTableEntry.max_world_z = (int) (renderFace.vertices[i].z + (j * world_dz_step));
//						
//						edgeTableEntry.max_pcorrectedWorld_x = renderFace.pcorrectedWorld_X[i] + (j * pcorr_world_dx_step);
//						edgeTableEntry.max_pcorrectedWorld_y = renderFace.pcorrectedWorld_Y[i] + (j * pcorr_world_dy_step);
//						edgeTableEntry.max_pcorrectedWorld_z = renderFace.pcorrectedWorld_Z[i] + (j * pcorr_world_dz_step);
//
//						// texel coords
//						edgeTableEntry.max_texel_u = renderFace.texel_U[i] + (j * texel_du_step);
//						edgeTableEntry.max_texel_v = renderFace.texel_V[i] + (j * texel_dv_step);
//						
//						// perspective correction factor
//						edgeTableEntry.max_zFactor = renderFace.zFactors[i] + (j * zFactor_step);
//
//						// vertex normal
//						edgeTableEntry.max_normal_x = renderFace.vertices[i].normal_x + (j * vertexNormal_dx_step);
//						edgeTableEntry.max_normal_y = renderFace.vertices[i].normal_y + (j * vertexNormal_dy_step);
//						edgeTableEntry.max_normal_z = renderFace.vertices[i].normal_z + (j * vertexNormal_dz_step);	
//					}									
//				}
//
//				edgeTableEntry.visited = true;
//			}
//		}
//		
//		return new int[]{minY, maxY};
//	}
	
	
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
			
			DirectionalLight directionalLight = shader!=null? shader.getDirectionalLight() : null;		
			Matrix3D inverseLightMatrix = directionalLight!=null? directionalLight.getInverseMatrix() : null;
			Point3D lightOrigin = directionalLight!=null? directionalLight.getOrigin() : null;
			ZBuffer shadowMap = directionalLight!=null? directionalLight.getDepthMap() : null;
			
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
												
							// Shadow mapping: anhand der world space coordinaten noch den punkt im light space bestimmen
							if(shadowMap!=null){
								float[] lightCoords = inverseLightMatrix.transform(pcorr_world_x - lightOrigin.x, pcorr_world_y - lightOrigin.y, pcorr_world_z - lightOrigin.z);
																						
								// perspektiven korrektur innerhalb des light space
								float zf = Engine3D.zFactor(lightCoords[2]);
								lightCoords[0] = lightCoords[0] * zf;
								lightCoords[1] = lightCoords[1] * zf;
								
								// to "screenspace" (weil ja auf diese weise auch die werte beim eintragen in die shadow map berechnet wurden)						
								float light_x = shadowMap.xoffset + lightCoords[0];
								float light_y = shadowMap.yoffset - lightCoords[1];
								
								if(light_x<0 || light_x>=shadowMap.w || light_y<0 || light_y>=shadowMap.h){ // alles ausserhalb des lichtkegels ist im schatten								
									
									if(directionalLight.isSpotLight()){
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
	
	
	
	public RenderFace nearPlaneClipping(RenderFace renderFace, Point3D camOrigin, Matrix3D icammat, boolean perspectiveCorrect){
		
		// interpolate along each polygon edge
		Point3D[] newVertices = new Point3D[100];
		int newVertexCount=0;
		
		for (int i = 0; i < renderFace.vertices.length; i++) {
			
			
			if(renderFace.cam_Z[i] < 1){ // <-- kein cam-space!!
				
				// this vertex needs to be clipped
				
				int next = i + 1;
				if (next == renderFace.vertices.length)
					next = 0;
				
				int prev = i - 1;
				if(prev<0)
					prev = renderFace.vertices.length-1;
													
				
				Point3D p1 = new Point3D(renderFace.cam_X[prev], renderFace.cam_Y[prev], renderFace.cam_Z[prev]);
				Point3D p2 = new Point3D(renderFace.cam_X[i], renderFace.cam_Y[i], renderFace.cam_Z[i]);
				
				//Point3D[] clippedPrevLine = clipLine(renderFace.vertices[prev], renderFace.vertices[i], SCREEN_EDGE_NEAR);
				Point3D[] clippedPrevLine = clipLine(p1, p2, SCREEN_EDGE_NEAR);
				if(clippedPrevLine!=null){
					Point3D a = clippedPrevLine[0];
					Point3D b = clippedPrevLine[1];					
					newVertices[newVertexCount++] = a;
					newVertices[newVertexCount++] = b;
				}else{
					// skip line
				}
								
				Point3D p3 = new Point3D(renderFace.cam_X[i], renderFace.cam_Y[i], renderFace.cam_Z[i]);
				Point3D p4 = new Point3D(renderFace.cam_X[next], renderFace.cam_Y[next], renderFace.cam_Z[next]);
				
				//Point3D[] clippedNextLine = clipLine(renderFace.vertices[i], renderFace.vertices[next], SCREEN_EDGE_NEAR);
				Point3D[] clippedNextLine = clipLine(p3, p4, SCREEN_EDGE_NEAR);
				if(clippedNextLine!=null){
					Point3D a = clippedNextLine[0];
					Point3D b = clippedNextLine[1];							
					newVertices[newVertexCount++] = a;
					newVertices[newVertexCount++] = b;
				}else{
					// skip line
				}	
			}
			else{
				newVertices[newVertexCount++] = renderFace.vertices[i];
			}											
		}
		
		if(newVertexCount > renderFace.vertices.length){
			Face face = new Face(newVertices, newVertexCount, renderFace.getColor());
			RenderFace newRenderFace = face.createRenderFace();
//			newRenderFace.transformToCamSpace(camOrigin, icammat, perspectiveCorrect);
			return newRenderFace;
			//return face.createRenderFace();
		}
		else{
			return renderFace; // no clipping needed
		}
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
	

	/*
	 * clips face in screen space. returns a new face object
	 */
	public Face clipFace(Face face) {

		// do quick check for complete inside-ness (all vertices are inside
		// screen region)
		// return the original face
		if (allInside(face.vertices)) {
			return face;
		}

		Point3D[] polyPoints = new Point3D[face.vertices.length << 1];
		int i_vertices = 0;

		for (int i = 0; i < face.vertices.length; i++) {
			int next_i = i + 1;
			if (next_i == face.vertices.length) {
				next_i = 0;
			}

			Point3D[] newPiP_next = clipLine(face.vertices[i], face.vertices[next_i], SCREEN_EDGE_LEFT);
			boolean isCanceled = true;
			if (newPiP_next != null) {
				newPiP_next = clipLine(newPiP_next[0], newPiP_next[1], SCREEN_EDGE_RIGHT);
				if (newPiP_next != null) {
					newPiP_next = clipLine(newPiP_next[0], newPiP_next[1], SCREEN_EDGE_UP);
					if (newPiP_next != null) {
						newPiP_next = clipLine(newPiP_next[0], newPiP_next[1], SCREEN_EDGE_DOWN);
						if (newPiP_next != null) {
							isCanceled = false;
							polyPoints[i_vertices++] = newPiP_next[0];
							polyPoints[i_vertices++] = newPiP_next[1];
						}
					}
				}
			}

			if (isCanceled) {
				// eckpunkt einsetzen wenn kante über eck geht
				// if minx < -xoffset && ymin < -yoffset then ecke links unten
				Point3D[] cornerPoints = cornerize(face.vertices[i], face.vertices[next_i]);
				if (cornerPoints[0] != null) {
					// polyPoints.add(cornerPoints[0]);
					polyPoints[i_vertices++] = cornerPoints[0];
					if (cornerPoints[1] != null) {
						// polyPoints.add(cornerPoints[1]);
						polyPoints[i_vertices++] = cornerPoints[1];
					}
				}
			}
		}

		return new Face(polyPoints, i_vertices, face.col);
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

	// gibt den eckpunkt wenn kante über eck geht, zwei wenn komplette seite
	// ageschnitten wird
	private Point3D[] cornerize(Point3D p1, Point3D p2) {
		float minX = p1.x;
		if (p2.x < minX)
			minX = p2.x;

		float maxX = p1.x;
		if (p2.x > maxX)
			maxX = p2.x;

		float minY = p1.y;
		if (p2.y < minY)
			minY = p2.y;

		float maxY = p1.y;
		if (p2.y > maxY)
			maxY = p2.y;

		Point3D[] result = new Point3D[2];

		float newz = (p1.z + p2.z) / 2;

		if (minX < -xoffset && minY < -yoffset) {
			if (result[0] == null) {
				result[0] = new Point3D(-xoffset, -yoffset, newz); // bottom
																	// left
			} else {
				result[1] = new Point3D(-xoffset, -yoffset, newz); // bottom
																	// left
			}
			// System.out.println("new bottom left corner");
			// return new Point3D(-xoffset, -yoffset, 0); // bottom left
		}

		if (minX < -xoffset && maxY > yoffset) {
			if (result[0] == null) {
				result[0] = new Point3D(-xoffset, yoffset, 0); // top left
			} else {
				result[1] = new Point3D(-xoffset, yoffset, 0); // top left
			}
			// System.out.println("new top left corner");
			// return new Point3D(-xoffset, yoffset, 0); // top left
		}

		if (maxX > xoffset && minY < -yoffset) {
			if (result[0] == null) {
				result[0] = new Point3D(xoffset, -yoffset, 0); // bottom right
			} else {
				result[1] = new Point3D(xoffset, -yoffset, 0); // bottom right
			}
			// System.out.println("new bottom right corner");
			// return new Point3D(xoffset, -yoffset, 0); // bottom right
		}

		if (maxX > xoffset && maxY > yoffset) {
			if (result[0] == null) {
				result[0] = new Point3D(xoffset, yoffset, 0); // top right
			} else {
				result[1] = new Point3D(xoffset, yoffset, 0); // top right
			}
			// System.out.println("new top right corner");
			// return new Point3D(xoffset, yoffset, 0); // top right
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
