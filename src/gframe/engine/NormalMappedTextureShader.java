package gframe.engine;

import gframe.engine.generator.TextureGenerator;
import imaging.ImageRaster;

public class NormalMappedTextureShader extends TextureShader{

	ImageRaster normalMap;
	
	ImageRaster normalMapLOD0;
	ImageRaster normalMapLOD1;
	ImageRaster normalMapLOD2;
	ImageRaster normalMapLOD3;
	
	static final float iNormalNorm = 1/127f;	
	
	/*
	 * Provide texture and normal map of equal size
	 * */
	public NormalMappedTextureShader(Lightsource lightsource, ImageRaster texture, ImageRaster normalMap) {
		super(lightsource, texture);		
		this.normalMap = normalMap;
		
		this.normalMapLOD0 = normalMap;
		this.normalMapLOD1 = TextureGenerator.mipmap(normalMap);
		this.normalMapLOD2 = TextureGenerator.mipmap(normalMapLOD1);
		this.normalMapLOD3 = TextureGenerator.mipmap(normalMapLOD2);
	}
	
	
	@Override
	public void adjustLOD(RenderFace renderFace) {	
		
		int lod = getLOD(renderFace);					
				
//		System.out.println(lod);
		
		ImageRaster lodTexture = textureLOD3;
		ImageRaster lodNormal = normalMapLOD3;
		
		if(lod==0){
			lodTexture = textureLOD0;
			lodNormal = normalMapLOD0;
		}
		else if(lod==1){			
			lodTexture = textureLOD1;
			lodNormal = normalMapLOD1;
		}
		else if(lod==2){			
			lodTexture = textureLOD2;
			lodNormal = normalMapLOD2;
		}
						
		this.texture = lodTexture;		
		this.textureWidth = lodTexture.getWidth();
		this.textureHeight = lodTexture.getHeight();
		
		this.normalMap = lodNormal;			
	}
	
	
	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float pcorr_world_x, float pcorr_world_y, float pcorr_world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		// dither delta
		float du = 0;
		float dv = 0;
				
		if(isDithering){
			int x_index = screen_x & 1;
			int y_index = screen_y & 1;
				
			du = DITHER_KERNEL[x_index][y_index][0];
			dv = DITHER_KERNEL[x_index][y_index][1];						
		}
			
//		float u = Math.max(0, Math.min(textureWidth-1, texel_u*(textureWidth) + du));
//		float v = Math.max(0, Math.min(textureHeight-1, texel_v*(textureHeight) + dv));
		float u = Math.min(textureWidth-1, texel_u*(textureWidth) + du);
		float v = Math.min(textureHeight-1, texel_v*(textureHeight) + dv);
		
		int texel = super.getTexel(u, v);
		
		int texelNormal = this.getNormal(u, v);																		
		float tNormal_x =  (((texelNormal >> 16) & 0xff)-128) * iNormalNorm; // red
		float tNormal_y = -(((texelNormal >> 8)  & 0xff)-128) * iNormalNorm; // green
		float tNormal_z =  (((texelNormal >> 0)  & 0xff)-128) * iNormalNorm; // blue									
		
		
		// see:
		// - literature/26-BumpMap+ProcTex.pdf
		// - http://www.terathon.com/code/tangent.html
		Vector3D tangentLocalLightsourcePosition = renderFace.getInverseTangentSpace().transform(new Vector3D(world_x-lightsource.x, world_y-lightsource.y, world_z-lightsource.z));
		
//		float[] tnormalTransformed = renderFace.getTangentSpace().transform(tNormal_x, tNormal_y, tNormal_z);
//		tNormal_x = tnormalTransformed[0];
//		tNormal_y = tnormalTransformed[1];
//		tNormal_z = tnormalTransformed[2];
		
		tangentLocalLightsourcePosition.normalize();		
		float lightNormalProduct = -tangentLocalLightsourcePosition.dotProduct(tNormal_x, tNormal_y, tNormal_z);
		
		if (lightNormalProduct < 0) {
			lightNormalProduct = 0;
			//lightNormalProduct = Math.abs(lightNormalProduct);
		}
		
		return super.shade(texel, 1.0f, lightNormalProduct);
	}
	
	
	public int getNormal(float x, float y){
		
		int normal;
		
		if(!super.isBilinearFilteringEnabled){
//			int x_int = (int)Math.floor(x + 0.5f);
//			int y_int = (int)Math.floor(y + 0.5f);			
			int x_int = (int)x;
			int y_int = (int)y;
			normal = normalMap.getPixel(x_int, y_int);
		}		
		else{			
			int x_int = (int)x;
			int y_int = (int)y;
					
			normal = normalMap.getPixel(x_int, y_int);
			
			float x_fract = x - x_int;
			float y_fract = y - y_int;
								
			int c0_x = (normal >> 16) & 0xff;
			int c0_y = (normal >> 8) & 0xff;
			int c0_z = (normal >> 0) & 0xff;			
				
			int c1_normal = x_int+1<textureWidth? normalMap.getPixel(x_int+1, y_int) : normal;
			int c1_x = (c1_normal >> 16) & 0xff;
			int c1_y = (c1_normal >> 8) & 0xff;
			int c1_z = (c1_normal >> 0) & 0xff;			
				
			int c2_normal = y_int+1<textureHeight? normalMap.getPixel(x_int, y_int+1) : normal;
			int c2_x = (c2_normal >> 16) & 0xff;
			int c2_y = (c2_normal >> 8) & 0xff;
			int c2_z = (c2_normal >> 0) & 0xff;			
				
			int c3_normal = x_int+1<textureWidth && y_int+1<textureHeight? normalMap.getPixel(x_int+1, y_int+1) : normal;
			int c3_x = (c3_normal >> 16) & 0xff;
			int c3_y = (c3_normal >> 8) & 0xff;
			int c3_z = (c3_normal >> 0) & 0xff;			
									
			float c0_weight = (1-x_fract) * (1-y_fract);
			float c1_weight = x_fract * (1-y_fract);
			float c2_weight = (1-x_fract) * y_fract;
			float c3_weight = x_fract * y_fract;
				
			int newX = (int)( c0_x * c0_weight
						+ c1_x * c1_weight
						+ c2_x * c2_weight
						+ c3_x * c3_weight );
						
			int newY = (int)( c0_y * c0_weight
						+ c1_y * c1_weight
						+ c2_y * c2_weight
						+ c3_y * c3_weight );
				
			int newZ = (int)( c0_z * c0_weight
						+ c1_z * c1_weight
						+ c2_z * c2_weight
						+ c3_z * c3_weight );
				
				
			normal = ((newX & 0xFF) << 16) |
		                 ((newY & 0xFF) << 8)  |
		                 ((newZ & 0xFF) << 0);	
			
		
		}
		
		return normal;
	}
	

	
}
