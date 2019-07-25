package gframe.engine;

import java.awt.Color;

/**
 * Can rasterize any convex n-gon. Rasterization is done on instances of
 * ImageRaster (32-bit pixel buffer).
 */
public class DefaultConvexPolygonRasterizer implements Rasterizer {

	public final static int SCREEN_EDGE_LEFT = 1;
	public final static int SCREEN_EDGE_RIGHT = 2;
	public final static int SCREEN_EDGE_UP = 3;
	public final static int SCREEN_EDGE_DOWN = 4;
	public final static int SCREEN_EDGE_NEAR = 5;
	public final static int SCREEN_EDGE_FAR = 6;

	private int xoffset;
	private int yoffset;
	private int frameX;
	private int frameY;

	
	// array of length frameY. Stores values of certain variables in that screen line at minimum and maximum x position.
	private EdgeTableEntry[] edgeTable;

	// stores deltas of all variables for polygon edge interpolation
	private final float[] edgeDeltas;

	private final int shadowColorRGB = Color.black.getRGB();

	// only draws every second screen line
	public boolean interlacedMode = false;

	
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
		// doOnlyZPass: just write into the zBuffer
		boolean doOnlyZPass = colorBuffer == null && zBuffer != null;
		int[] minMaxY = interpolateEdges(renderFace, doOnlyZPass);
		interpolateScanlines(renderFace, colorBuffer, zBuffer, shader, minMaxY[0], minMaxY[1], doOnlyZPass);
	}

	public int[] interpolateEdges(final RenderFace renderFace, boolean onlyZPass) {

		// determine bounding y-line
		int minY = Integer.MAX_VALUE;
		int maxY = 0;

		// interpolate along each polygon edge
		for (int i = 0; i < renderFace.vertices.length; i++) {

			int next = i + 1;
			if (next == renderFace.vertices.length)
				next = 0;

			// current edge: i ->  next
			int[] minYmaxY = interpolateEdge(renderFace, i, next, onlyZPass); 

			// if null then edge is clipped entirely (edge is completely outside of screen)
			if (minYmaxY != null) {
				if (minYmaxY[0] < minY)
					minY = minYmaxY[0];

				if (minYmaxY[1] > maxY)
					maxY = minYmaxY[1];
			}

		}

		return new int[] { minY, maxY };
	}

	/**
	 * Interpolates along edge i0 -> i1 and fills edgeTable as it goes.
	 */
	public int[] interpolateEdge(final RenderFace renderFace, int vertex_i0, int vertex_i1, final boolean onlyZPass) {		
		
		int topVertex = vertex_i0;
		int botVertex = vertex_i1;

		if (yoffset - renderFace.cam_Y[topVertex] > yoffset - renderFace.cam_Y[botVertex]) {
			topVertex = vertex_i1;
			botVertex = vertex_i0;
		}

		final float screen_y_top = yoffset - renderFace.cam_Y[topVertex];
		final float screen_y_bot = yoffset - renderFace.cam_Y[botVertex];

		// CLIP EDGE
		if (screen_y_bot < 0 || screen_y_top > frameY) {
			return null;
		}
		// ----------

		final float lineHeight = screen_y_bot - screen_y_top;
		
		// sub pixel height
		final float subPix = ((int)screen_y_top - screen_y_top);		
		
		// for pre-interpolation of negative part (outside top of screen)
		final float cutOff = screen_y_top < 0? -screen_y_top : 0;		

		// the scanline indices
		int top = clipY((int) (screen_y_top));
		int bot = clipY((int) (screen_y_bot));	

		int int_height = bot - top;
		   
		// line spans 2 or more scan lines
		float inverseHeight = 0f;
		if(lineHeight>1f){
			inverseHeight = 1f / (float) lineHeight;
			inverseHeight = Math.min( 1f/(float)lineHeight, 1f );
			inverseHeight = Math.max(inverseHeight, 0f);
		}
			

		// compute relative deltas..
		edgeDeltas[0] = (renderFace.cam_X[botVertex] - renderFace.cam_X[topVertex]) * inverseHeight;
		// .. and adjust all start variables with cutoff
		float cam_x = xoffset + renderFace.cam_X[topVertex] + (cutOff * edgeDeltas[0]);
		
		float zf = 0;
		float z = 0;;
		
		float u = 0;
		float v = 0;

		float nx = 0;
		float ny = 0;
		float nz = 0;
		
		float wx = 0;
		float wy = 0;
		float wz = 0;
		
		if (!onlyZPass) {
			// we need more deltas for zf, u, v, nx, ny, nz, wx, wy, wz
			edgeDeltas[1] = (renderFace.zFactors[botVertex] - renderFace.zFactors[topVertex]) * inverseHeight;
			
			edgeDeltas[3] = (renderFace.texel_U[botVertex] - renderFace.texel_U[topVertex]) * inverseHeight;
			edgeDeltas[4] = (renderFace.texel_V[botVertex] - renderFace.texel_V[topVertex]) * inverseHeight;

			edgeDeltas[5] = (renderFace.vertices[botVertex].normal_x - renderFace.vertices[topVertex].normal_x)
					* inverseHeight;
			edgeDeltas[6] = (renderFace.vertices[botVertex].normal_y - renderFace.vertices[topVertex].normal_y)
					* inverseHeight;
			edgeDeltas[7] = (renderFace.vertices[botVertex].normal_z - renderFace.vertices[topVertex].normal_z)
					* inverseHeight;

			edgeDeltas[8] = (renderFace.pcorrectedWorld_X[botVertex] - renderFace.pcorrectedWorld_X[topVertex])
					* inverseHeight;
			edgeDeltas[9] = (renderFace.pcorrectedWorld_Y[botVertex] - renderFace.pcorrectedWorld_Y[topVertex])
					* inverseHeight;
			edgeDeltas[10] = (renderFace.pcorrectedWorld_Z[botVertex] - renderFace.pcorrectedWorld_Z[topVertex])
					* inverseHeight;
			
			zf = renderFace.zFactors[topVertex] + (cutOff * edgeDeltas[1]);

			u = renderFace.texel_U[topVertex] + (cutOff * edgeDeltas[3]);
			v = renderFace.texel_V[topVertex] + (cutOff * edgeDeltas[4]);
			
			nx = renderFace.vertices[topVertex].normal_x + (cutOff * edgeDeltas[5]);
			ny = renderFace.vertices[topVertex].normal_y + (cutOff * edgeDeltas[6]);
			nz = renderFace.vertices[topVertex].normal_z + (cutOff * edgeDeltas[7]);
			
			wx = renderFace.pcorrectedWorld_X[topVertex] + (cutOff * edgeDeltas[8]);
			wy = renderFace.pcorrectedWorld_Y[topVertex] + (cutOff * edgeDeltas[9]);
			wz = renderFace.pcorrectedWorld_Z[topVertex] + (cutOff * edgeDeltas[10]);
		}else{
			// we only need a delta for z
			edgeDeltas[2] = (renderFace.cam_Z[botVertex] - renderFace.cam_Z[topVertex]) * inverseHeight;
			 z = renderFace.cam_Z[topVertex] + (cutOff * edgeDeltas[2]);
		}
		
		for (int h = 0; h <= int_height; h++) {
			// update edge table
			EdgeTableEntry edgeTableEntry = edgeTable[top + h];
			
			if (!edgeTableEntry.visited || cam_x < edgeTableEntry.min_draw_x) {
				edgeTableEntry.min_draw_x = cam_x;
								
				if (!onlyZPass) {
					edgeTableEntry.min_zFactor = zf;
					edgeTableEntry.min_texel_u = u;
					edgeTableEntry.min_texel_v = v;
					edgeTableEntry.min_normal_x = nx;
					edgeTableEntry.min_normal_y = ny;
					edgeTableEntry.min_normal_z = nz;
					edgeTableEntry.min_pcorrectedWorld_x = wx;
					edgeTableEntry.min_pcorrectedWorld_y = wy;
					edgeTableEntry.min_pcorrectedWorld_z = wz;
				}else{
					edgeTableEntry.min_z = z;
				}
			}
			if (!edgeTableEntry.visited || cam_x > edgeTableEntry.max_draw_x) {
				edgeTableEntry.max_draw_x = cam_x;
								
				if (!onlyZPass) {
					edgeTableEntry.max_zFactor = zf;
					edgeTableEntry.max_texel_u = u;
					edgeTableEntry.max_texel_v = v;
					edgeTableEntry.max_normal_x = nx;
					edgeTableEntry.max_normal_y = ny;
					edgeTableEntry.max_normal_z = nz;
					edgeTableEntry.max_pcorrectedWorld_x = wx;
					edgeTableEntry.max_pcorrectedWorld_y = wy;
					edgeTableEntry.max_pcorrectedWorld_z = wz;
				}else{
					edgeTableEntry.max_z = z;
				}
			}

			edgeTableEntry.visited = true;

			// lerp variables
			cam_x += edgeDeltas[0];						
						
			if (!onlyZPass) {
				zf += edgeDeltas[1];
				
				u += edgeDeltas[3];
				v += edgeDeltas[4];

				nx += edgeDeltas[5];
				ny += edgeDeltas[6];
				nz += edgeDeltas[7];

				wx += edgeDeltas[8];
				wy += edgeDeltas[9];
				wz += edgeDeltas[10];
			}else{
				z += edgeDeltas[2];
			}
			
			
			// add sub-pixel correction
			if(h==0){
				cam_x += (subPix * edgeDeltas[0]);
								
				if(!onlyZPass){
					zf += (subPix * edgeDeltas[1]);
					u  += (subPix * edgeDeltas[3]);	
					v  += (subPix * edgeDeltas[4]);
					nx += (subPix * edgeDeltas[5]);
					ny += (subPix * edgeDeltas[6]);
					nz += (subPix * edgeDeltas[7]);
					wx += (subPix * edgeDeltas[8]);
					wy += (subPix * edgeDeltas[9]);
					wz += (subPix * edgeDeltas[10]);
				}else{
					z  += (subPix * edgeDeltas[2]);	
				}
			}
		}
		
//		if(int_height==0){
//			
//			EdgeTableEntry edgeTableEntry = edgeTable[bot];			
//			
//			cam_x = xoffset + renderFace.cam_X[botVertex] + (subPix * edgeDeltas[0]);
//			zf    = renderFace.zFactors[botVertex] + (subPix * edgeDeltas[1]);
//			
//			if (!edgeTableEntry.visited || cam_x < edgeTableEntry.min_draw_x) {	
//				edgeTableEntry.min_draw_x = cam_x;
//				edgeTableEntry.min_zFactor = zf;
//				
//				if (!onlyZPass) {
//					edgeTableEntry.min_texel_u = renderFace.texel_U[botVertex] + (subPix * edgeDeltas[2]); 
//					edgeTableEntry.min_texel_v = renderFace.texel_V[botVertex] + (subPix * edgeDeltas[3]); 
//					edgeTableEntry.min_normal_x = renderFace.vertices[botVertex].normal_x + (subPix * edgeDeltas[4]);
//					edgeTableEntry.min_normal_y = renderFace.vertices[botVertex].normal_y + (subPix * edgeDeltas[5]);
//					edgeTableEntry.min_normal_z = renderFace.vertices[botVertex].normal_z + (subPix * edgeDeltas[6]);
//					edgeTableEntry.min_pcorrectedWorld_x = renderFace.pcorrectedWorld_X[botVertex] + (subPix * edgeDeltas[7]);
//					edgeTableEntry.min_pcorrectedWorld_y = renderFace.pcorrectedWorld_Y[botVertex] + (subPix * edgeDeltas[8]);
//					edgeTableEntry.min_pcorrectedWorld_z = renderFace.pcorrectedWorld_Z[botVertex] + (subPix * edgeDeltas[9]);
////					edgeTableEntry.min_texel_u = u;
////					edgeTableEntry.min_texel_v = v;
////					edgeTableEntry.min_normal_x = nx;
////					edgeTableEntry.min_normal_y = ny;
////					edgeTableEntry.min_normal_z = nz;
////					edgeTableEntry.min_pcorrectedWorld_x = wx;
////					edgeTableEntry.min_pcorrectedWorld_y = wy;
////					edgeTableEntry.min_pcorrectedWorld_z = wz;
//				}							
//			}
//			if (!edgeTableEntry.visited || cam_x > edgeTableEntry.max_draw_x) {
//				edgeTableEntry.max_draw_x = cam_x;
//				edgeTableEntry.max_zFactor = zf;
//				
//				if (!onlyZPass) {
//					edgeTableEntry.max_texel_u = renderFace.texel_U[botVertex] + (subPix * edgeDeltas[2]); 
//					edgeTableEntry.max_texel_v = renderFace.texel_V[botVertex] + (subPix * edgeDeltas[3]); 
//					edgeTableEntry.max_normal_x = renderFace.vertices[botVertex].normal_x + (subPix * edgeDeltas[4]);
//					edgeTableEntry.max_normal_y = renderFace.vertices[botVertex].normal_y + (subPix * edgeDeltas[5]);
//					edgeTableEntry.max_normal_z = renderFace.vertices[botVertex].normal_z + (subPix * edgeDeltas[6]);
//					edgeTableEntry.max_pcorrectedWorld_x = renderFace.pcorrectedWorld_X[botVertex] + (subPix * edgeDeltas[7]);
//					edgeTableEntry.max_pcorrectedWorld_y = renderFace.pcorrectedWorld_Y[botVertex] + (subPix * edgeDeltas[8]);
//					edgeTableEntry.max_pcorrectedWorld_z = renderFace.pcorrectedWorld_Z[botVertex] + (subPix * edgeDeltas[9]);
////					edgeTableEntry.max_texel_u = u;
////					edgeTableEntry.max_texel_v = v;
////					edgeTableEntry.max_normal_x = nx;
////					edgeTableEntry.max_normal_y = ny;
////					edgeTableEntry.max_normal_z = nz;
////					edgeTableEntry.max_pcorrectedWorld_x = wx;
////					edgeTableEntry.max_pcorrectedWorld_y = wy;
////					edgeTableEntry.max_pcorrectedWorld_z = wz;
//				}
//			}				
//			edgeTableEntry.visited = true;					
//		}

		return new int[] { top, bot };
	}

	

	public void interpolateScanlines(final RenderFace renderFace, final ImageRaster colorBuffer, final ZBuffer zBuffer, final Shader shader,
			final int minY, final int maxY, final boolean doOnlyZPass) {

		int rgb = shader == null ? renderFace.col.getRGB() : shader.shade(renderFace);

		final Lightsource lightsource = shader != null ? shader.getLightsource() : null;
		final boolean shadowsEnabled = lightsource != null ? lightsource.isShadowsEnabled() : false;
		final Matrix3D inverseLightMatrix = shadowsEnabled ? lightsource.getInverseMatrix() : null;
		final ZBuffer shadowMap = shadowsEnabled ? lightsource.getDepthMap() : null;

		for (int draw_y = minY; draw_y <= maxY; draw_y++) {

			// one entry per scan line
			final EdgeTableEntry edgeTableEntry = edgeTable[draw_y];

			if (!edgeTableEntry.visited) {
				continue;
			}

			final float iScanlineLength = 1f / (edgeTableEntry.max_draw_x - edgeTableEntry.min_draw_x + 1);

			// compute deltas
			float z_stepfactor = 0;
			float zfactor_stepfactor = 0;
			float texel_du_stepfactor = 0;
			float texel_dv_stepfactor = 0;
			float vn_dx_stepfactor = 0;
			float vn_dy_stepfactor = 0;
			float vn_dz_stepfactor = 0;
			float pcorr_world_dx_stepfactor = 0;
			float pcorr_world_dy_stepfactor = 0;
			float pcorr_world_dz_stepfactor = 0;
			
			if(!doOnlyZPass){
				zfactor_stepfactor = (edgeTableEntry.max_zFactor - edgeTableEntry.min_zFactor)
						* iScanlineLength;
				
				texel_du_stepfactor = (edgeTableEntry.max_texel_u - edgeTableEntry.min_texel_u)
						* iScanlineLength;
				texel_dv_stepfactor = (edgeTableEntry.max_texel_v - edgeTableEntry.min_texel_v)
						* iScanlineLength;
				
				vn_dx_stepfactor = (edgeTableEntry.max_normal_x - edgeTableEntry.min_normal_x)
						* iScanlineLength;
				vn_dy_stepfactor = (edgeTableEntry.max_normal_y - edgeTableEntry.min_normal_y)
						* iScanlineLength;
				vn_dz_stepfactor = (edgeTableEntry.max_normal_z - edgeTableEntry.min_normal_z)
						* iScanlineLength;

				pcorr_world_dx_stepfactor = (edgeTableEntry.max_pcorrectedWorld_x
						- edgeTableEntry.min_pcorrectedWorld_x) * iScanlineLength;
				pcorr_world_dy_stepfactor = (edgeTableEntry.max_pcorrectedWorld_y
						- edgeTableEntry.min_pcorrectedWorld_y) * iScanlineLength;
				pcorr_world_dz_stepfactor = (edgeTableEntry.max_pcorrectedWorld_z
						- edgeTableEntry.min_pcorrectedWorld_z) * iScanlineLength;
			}else{
				z_stepfactor = (edgeTableEntry.max_z - edgeTableEntry.min_z) * iScanlineLength; 
			}
			
			// scanline begin and end
			int startX = (int) edgeTableEntry.min_draw_x;
			float endX = edgeTableEntry.max_draw_x;

			// clip against left side of screen
			if (edgeTableEntry.min_draw_x < 0) {
				// das abgeschnittene ende auf alle min-werte drauf
				// interpolieren								
				if(!doOnlyZPass){
					edgeTableEntry.min_zFactor += -edgeTableEntry.min_draw_x * zfactor_stepfactor;
					
					// texel
					edgeTableEntry.min_texel_u += -edgeTableEntry.min_draw_x * texel_du_stepfactor;
					edgeTableEntry.min_texel_v += -edgeTableEntry.min_draw_x * texel_dv_stepfactor;

					// vertex normals
					edgeTableEntry.min_normal_x += -edgeTableEntry.min_draw_x * vn_dx_stepfactor;
					edgeTableEntry.min_normal_y += -edgeTableEntry.min_draw_x * vn_dy_stepfactor;
					edgeTableEntry.min_normal_z += -edgeTableEntry.min_draw_x * vn_dz_stepfactor;
					
					// world coordinates
					edgeTableEntry.min_pcorrectedWorld_x += -edgeTableEntry.min_draw_x * pcorr_world_dx_stepfactor;
					edgeTableEntry.min_pcorrectedWorld_y += -edgeTableEntry.min_draw_x * pcorr_world_dy_stepfactor;
					edgeTableEntry.min_pcorrectedWorld_z += -edgeTableEntry.min_draw_x * pcorr_world_dz_stepfactor;
				}else{
					edgeTableEntry.min_z += -edgeTableEntry.min_draw_x * z_stepfactor;
				}
				
				edgeTableEntry.min_draw_x = 0;
				startX = 0;
			}
			if (edgeTableEntry.max_draw_x >= frameX) {
				endX = frameX - 1;
			}

			// here we go across the scan line..
			boolean shadeScanline = true;
			for (int draw_x = startX; draw_x <= endX; draw_x++) {

				float inverseZfactor;
				float draw_z;				
				if(doOnlyZPass){
					inverseZfactor = 0;
					draw_z = edgeTableEntry.min_z;
				}else{
					// compute perspective correct z-value;
					// this is the pixel divide
					// for perspective
					// correction.
					// since we linearly
					// interpolated 1/z we now have
					// to transform back to z.
					inverseZfactor = 1 / edgeTableEntry.min_zFactor;
					draw_z = Engine3D.inverseZFactor(edgeTableEntry.min_zFactor, inverseZfactor);
				}

				if (zBuffer.update(draw_x, draw_y, draw_z)) {

					shadeScanline = interlacedMode == false || (draw_y & 1) == 0;
					if (!doOnlyZPass && shadeScanline) {

						if (shader != null && shader.isPerPixelShader()) {

							boolean insideShadow = false;

							// perspektivenkorrektur der weltkoordinaten aus
							// kamera
							// sicht
							float pcorr_world_x = edgeTableEntry.min_pcorrectedWorld_x * inverseZfactor;
							float pcorr_world_y = edgeTableEntry.min_pcorrectedWorld_y * inverseZfactor;
							float pcorr_world_z = edgeTableEntry.min_pcorrectedWorld_z * inverseZfactor;

							// Shadow mapping: anhand der world space
							// coordinaten
							// noch den punkt im light space bestimmen
							if (shadowMap != null) {
								
								// all this shadow-mapping stuff should go somewhere else!
								
								// add a bit of normal vector as offset to avoid shadow acne 
								float[] lightCoords = inverseLightMatrix.transform(pcorr_world_x + (0.5f * edgeTableEntry.min_normal_x) - lightsource.x,
										pcorr_world_y + (0.5f * edgeTableEntry.min_normal_y) - lightsource.y, pcorr_world_z + (0.5f * edgeTableEntry.min_normal_z) - lightsource.z);
								
								// perspektiven korrektur innerhalb des light-space (depth map render pass was done with perspective projection, so here we need to do the same)
								float zf = Engine3D.zFactor(lightCoords[2]);
								lightCoords[0] *= zf;
								lightCoords[1] *= zf;

								// to "screenspace" da auf diese weise
								// auch die werte beim eintragen in die 
								// shadow map/depth map berechnet wurden								
								float light_x = shadowMap.xoffset + lightCoords[0] + 0.5f;
								float light_y = shadowMap.yoffset - lightCoords[1] + 0.5f;
								
								if (light_x < 0 || light_x >= shadowMap.w || light_y < 0 || light_y >= shadowMap.h) {
									// outside shadow map
									insideShadow = true;									
								} else if (shadowMap.getValue(light_x, light_y, false) < lightCoords[2]) {
									insideShadow = true;
								}
							}

							if (insideShadow) {
								rgb = shadowColorRGB;
							} else {
								rgb = shader.shade(renderFace, pcorr_world_x, pcorr_world_y, pcorr_world_z, edgeTableEntry.min_normal_x, edgeTableEntry.min_normal_y,
										edgeTableEntry.min_normal_z, edgeTableEntry.min_texel_u*inverseZfactor, edgeTableEntry.min_texel_v*inverseZfactor, draw_x, draw_y);
							}
						}

						colorBuffer.setPixel(draw_x, draw_y, rgb);
					}
				}

				// lerp..						
				if(!doOnlyZPass){
					edgeTableEntry.min_zFactor += zfactor_stepfactor;
					
					edgeTableEntry.min_texel_u += texel_du_stepfactor;
					edgeTableEntry.min_texel_v += texel_dv_stepfactor;

					edgeTableEntry.min_normal_x += vn_dx_stepfactor;
					edgeTableEntry.min_normal_y += vn_dy_stepfactor;
					edgeTableEntry.min_normal_z += vn_dz_stepfactor;
					
					edgeTableEntry.min_pcorrectedWorld_x += pcorr_world_dx_stepfactor;
					edgeTableEntry.min_pcorrectedWorld_y += pcorr_world_dy_stepfactor;
					edgeTableEntry.min_pcorrectedWorld_z += pcorr_world_dz_stepfactor;
				}else{
					edgeTableEntry.min_z += z_stepfactor;
				}
				
				if(draw_x == startX){
					// sub-tex accuracy adjustments
					final float subTex = ((float) startX) - edgeTableEntry.min_draw_x;
					if(!doOnlyZPass){
						edgeTableEntry.min_zFactor += subTex * zfactor_stepfactor;
						
						edgeTableEntry.min_texel_u += subTex * texel_du_stepfactor;
						edgeTableEntry.min_texel_v += subTex * texel_dv_stepfactor;
						
						edgeTableEntry.min_normal_x += subTex * vn_dx_stepfactor;
						edgeTableEntry.min_normal_y += subTex * vn_dy_stepfactor;
						edgeTableEntry.min_normal_z += subTex * vn_dz_stepfactor;
						
						edgeTableEntry.min_pcorrectedWorld_x += subTex * pcorr_world_dx_stepfactor;
						edgeTableEntry.min_pcorrectedWorld_y += subTex * pcorr_world_dy_stepfactor;
						edgeTableEntry.min_pcorrectedWorld_z += subTex * pcorr_world_dz_stepfactor;
					}else{
						edgeTableEntry.min_z += subTex * z_stepfactor;
					}
				}

			} // draw_x

			edgeTableEntry.clear();

		} // draw_y
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

	@Override
	public int getXOffset() {
		return xoffset;
	}

	@Override
	public int getYOffset() {
		return yoffset;
	}

	protected class EdgeTableEntry {

		boolean visited;

		float min_draw_x;
		float max_draw_x;
			
		float min_z;
		float max_z;
		
		// represents 1/z
		float min_zFactor;
		float max_zFactor;
		
		
		float min_texel_u;
		float min_texel_v;
		float max_texel_u;
		float max_texel_v;
				

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

			min_zFactor = 0;
			max_zFactor = 0;
			
			min_z = max_z = 0;
			
			min_texel_u = 0;
			min_texel_v = 0;
			max_texel_u = 0;
			max_texel_v = 0;
			

			min_normal_x = min_normal_y = min_normal_z = 0;
			max_normal_x = max_normal_y = max_normal_z = 0;

			min_pcorrectedWorld_x = min_pcorrectedWorld_y = min_pcorrectedWorld_z = 0;
			max_pcorrectedWorld_x = max_pcorrectedWorld_y = max_pcorrectedWorld_z = 0;
			
			visited = false;
		}

	}

}
