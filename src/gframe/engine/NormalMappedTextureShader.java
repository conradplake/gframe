package gframe.engine;

import gframe.engine.generator.TextureGenerator;
import imaging.ImageHelper;
import imaging.ImageRaster;

public class NormalMappedTextureShader extends TextureShader {

	ImageRaster normalMap;

	ImageRaster normalMapLOD0;
	ImageRaster normalMapLOD1;
	ImageRaster normalMapLOD2;
	ImageRaster normalMapLOD3;

	static final float iNormalNorm = 1 / 127f;

	private boolean specularityFromAlphaChannel;

	private Point3D camPosition;
	private float lightPos_x;
	private float lightPos_y;
	private float lightPos_z;
	
	private int texel;
	
	// texture coordinates
	private float u;
	private float v;

	// dither delta
	private float du;
	private float dv;
	

	private int texelNormal;
	private float tNormal_x;
	private float tNormal_y;
	private float tNormal_z;
	
	private float lightNormalProduct;
	
	private int diffuseAlpha;
	private float diffuseRed;
	private float diffuseGreen;
	private float diffuseBlue;
		
	private float ambientColor_red;
	private float ambientColor_green;
	private float ambientColor_blue;
		
	private float diffuseIntensity;
	private float diffuse_red;
	private float diffuse_green;
	private float diffuse_blue;
	
	private float viewReflectionProduct;
	
	private float specularIntensity;			
	private float specularCoefficient;

	private float specIntermediate;			
	private float specular_red;
	private float specular_green;
	private float specular_blue;

	private int redColor;
	private int greenColor;
	private int blueColor;
	
	/**
	 * Provide texture and normal map of equal dimensions!
	 */
	public NormalMappedTextureShader(Lightsource lightsource, ImageRaster texture, ImageRaster normalMap) {
		this(lightsource, texture, normalMap, false);
	}

	/**
	 * Provide texture and normal map of equal dimensions!
	 */
	public NormalMappedTextureShader(Lightsource lightsource, ImageRaster texture, ImageRaster normalMap,
			boolean specularityFromAlphaChannel) {
		super(lightsource, texture);
		this.normalMap = normalMap;

		this.specularityFromAlphaChannel = specularityFromAlphaChannel;

		this.normalMapLOD0 = normalMap;
		this.normalMapLOD1 = TextureGenerator.mipmap(normalMap);
		this.normalMapLOD2 = TextureGenerator.mipmap(normalMapLOD1);
		this.normalMapLOD3 = TextureGenerator.mipmap(normalMapLOD2);
	}

	/**
	 * Provide texture, normal and specular map of equal dimensions! Normal
	 * map's alpha channel will be overwritten by specular map gray values!
	 */
	public NormalMappedTextureShader(Lightsource lightsource, ImageRaster texture, ImageRaster normalMap,
			ImageRaster specularMap) {
		super(lightsource, texture);

		// specular map als alpha-channel in die normal map kopieren
		for (int x = 0; x < texture.getWidth(); x++) {
			for (int y = 0; y < texture.getHeight(); y++) {
				int specRgb = specularMap.getPixel(x, y);
				int grayValue = ImageHelper.toGray(specRgb);
				normalMap.setAlpha(x, y, grayValue);
			}
		}
		this.specularityFromAlphaChannel = true;

		this.normalMap = normalMap;

		this.normalMapLOD0 = normalMap;
		this.normalMapLOD1 = TextureGenerator.mipmap(normalMap);
		this.normalMapLOD2 = TextureGenerator.mipmap(normalMapLOD1);
		this.normalMapLOD3 = TextureGenerator.mipmap(normalMapLOD2);
	}

	@Override
	public void adjustLOD(RenderFace renderFace) {

		int lod = getLOD(renderFace);

		// System.out.println("adjustLOD to "+lod);

		ImageRaster lodTexture = textureLOD3;
		ImageRaster lodNormal = normalMapLOD3;

		if (lod == 0) {
			lodTexture = textureLOD0;
			lodNormal = normalMapLOD0;
		} else if (lod == 1) {
			lodTexture = textureLOD1;
			lodNormal = normalMapLOD1;
		} else if (lod == 2) {
			lodTexture = textureLOD2;
			lodNormal = normalMapLOD2;
		}

		this.texture = lodTexture;
		this.textureWidth = lodTexture.getWidth();
		this.textureHeight = lodTexture.getHeight();

		this.normalMap = lodNormal;
	}

	
	@Override
	void setEffectPixel(int x, int y, int c) {
		super.setEffectPixel(x, y, c);
		normalMap.setAlpha(x, y, 0);		
	};
	
	
	@Override
	void recomputeMipmaps() {
		super.recomputeMipmaps();
		this.normalMapLOD0 = normalMap;
		this.normalMapLOD1 = TextureGenerator.mipmap(normalMap);
		this.normalMapLOD2 = TextureGenerator.mipmap(normalMapLOD1);
		this.normalMapLOD3 = TextureGenerator.mipmap(normalMapLOD2);
	};
	
	
	@Override
	public void preShade(RenderFace renderFace) {
		
		super.preShade(renderFace);
		
		this.lightPos_x = lightsource.x;
		this.lightPos_y = lightsource.y;
		this.lightPos_z = lightsource.z;

		if (specularityFromAlphaChannel) {
			this.camPosition = renderFace.getCameraPosition();
			// world_to_camera in den tangentenraum transformieren (farbe wird im tangentraum errechnet)
			// this.tangentLocalViewPosition =
			// renderFace.getInverseTangentSpace().transform(new
			// Vector3D(camPosition.x - renderFace.centroid.x, camPosition.y -
			// renderFace.centroid.y, camPosition.z - renderFace.centroid.z));
			// tangentLocalViewPosition.normalize();
		}
	}
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		// dither delta
		du = 0;
		dv = 0;

		if (isDithering) {
			int x_index = screen_x & 1;
			int y_index = screen_y & 1;

			du = DITHER_KERNEL[x_index][y_index][0];
			dv = DITHER_KERNEL[x_index][y_index][1];
		}

		// float u = Math.max(0, Math.min(textureWidth-1, texel_u*(textureWidth)
		// + du));
		// float v = Math.max(0, Math.min(textureHeight-1,
		// texel_v*(textureHeight) + dv));
		u = Math.min(textureWidth - 1, texel_u * (textureWidth) + du);
		v = Math.min(textureHeight - 1, texel_v * (textureHeight) + dv);

		texel = super.getTexel(u, v);
		

		texelNormal = this.getNormal(u, v);
		tNormal_x = (((texelNormal >> 16) & 0xff) - 128) * iNormalNorm; // red
		tNormal_y = -(((texelNormal >> 8) & 0xff) - 128) * iNormalNorm; // green
		tNormal_z = (((texelNormal >> 0) & 0xff) - 128) * iNormalNorm; // blue

		// see:
		// - literature/26-BumpMap+ProcTex.pdf
		// - http://www.terathon.com/code/tangent.html
		Vector3D tangentLocalLightsourcePosition = renderFace.getInverseTangentSpace()
				.transform(new Vector3D(lightPos_x - world_x, lightPos_y - world_y, lightPos_z - world_z));
		tangentLocalLightsourcePosition.normalize();

		lightNormalProduct = tangentLocalLightsourcePosition.dotProduct(tNormal_x, tNormal_y, tNormal_z);

		if (!specularityFromAlphaChannel) {
			// return diffuse color
			return super.shade(texel, 1f, Math.max(lightNormalProduct, 0));
		} 
		else {
			// here we do phong lighting in tangent space

			// put vector world_position --> camera into tangent space
			Vector3D tangentLocalViewPosition = renderFace.getInverseTangentSpace()
					.transform(new Vector3D(camPosition.x - world_x, camPosition.y - world_y, camPosition.z - world_z));
			tangentLocalViewPosition.normalize();

			// an oberfläche reflekierten Lichtvektor berechnen
			// this overwrites the original vector so make sure it is not used
			// for seomething els later on!
			tangentLocalLightsourcePosition.x = tangentLocalLightsourcePosition.x - 2 * lightNormalProduct * tNormal_x;
			tangentLocalLightsourcePosition.y = tangentLocalLightsourcePosition.y - 2 * lightNormalProduct * tNormal_y;
			tangentLocalLightsourcePosition.z = tangentLocalLightsourcePosition.z - 2 * lightNormalProduct * tNormal_z;

			// object color
			diffuseAlpha = (texel >> 24) & 0xff;
			diffuseRed = ((texel >> 16) & 0xff) * iColorNorm;
			diffuseGreen = ((texel >> 8) & 0xff) * iColorNorm;
			diffuseBlue = ((texel >> 0) & 0xff) * iColorNorm;

			// AMBIENT
//			float ambientIntermediate = Lightsource.AMBIENT_LIGHT_INTENSITY * currentAmbientCoefficient;
//			float ambientColor_red = ambientIntermediate * diffuseRed;
//			float ambientColor_green = ambientIntermediate * diffuseGreen;
//			float ambientColor_blue = ambientIntermediate * diffuseBlue;
			ambientColor_red   = renderFace.material.ambientCoefficientRed   * diffuseRed;
			ambientColor_green = renderFace.material.ambientCoefficientGreen * diffuseGreen;
			ambientColor_blue  = renderFace.material.ambientCoefficientBlue  * diffuseBlue;

			// DIFFUSE
			diffuseIntensity = Math.max(lightNormalProduct, 0);
//			float diffuseIntermediate = diffuseIntensity * currentAmbientCoefficient;
//			diffuse_red = diffuseIntermediate * diffuseRed;
//			diffuse_green = diffuseIntermediate * diffuseGreen;
//			diffuse_blue = diffuseIntermediate * diffuseBlue;
			diffuse_red   = diffuseIntensity * renderFace.material.diffuseCoefficientRed   * diffuseRed;
			diffuse_green = diffuseIntensity * renderFace.material.diffuseCoefficientGreen * diffuseGreen;
			diffuse_blue  = diffuseIntensity * renderFace.material.diffuseCoefficientBlue  * diffuseBlue;

			// SPECULAR
			// wenn camera genau in den reflektierten lichtstrahl blickt, dann
			// haben wir maximale spekularität (shininess)
			viewReflectionProduct = tangentLocalViewPosition.dotProduct(tangentLocalLightsourcePosition);

			
//			 viewReflectionProduct = (float) Math.pow(viewReflectionProduct, 16); // too expensive :(			
			// --> schlick's approximation:
			viewReflectionProduct = -viewReflectionProduct
					/ (renderFace.material.shininess + (renderFace.material.shininess * viewReflectionProduct) - viewReflectionProduct);

			specularIntensity = Math.max(viewReflectionProduct, 0);			
			specularCoefficient = ((texelNormal >> 24) & 0xff) * iColorNorm; // specularity from normal map's alpha-channel in [0..1]

			specIntermediate = specularIntensity * specularCoefficient;
//			specular_red = specIntermediate * lightsource.rgbComponents[0];
//			specular_green = specIntermediate * lightsource.rgbComponents[1];
//			specular_blue = specIntermediate * lightsource.rgbComponents[2];			
			specular_red   = specIntermediate * renderFace.material.specularCoefficientRed   * lightsource.rgbComponents[0];
			specular_green = specIntermediate * renderFace.material.specularCoefficientGreen * lightsource.rgbComponents[1];
			specular_blue  = specIntermediate * renderFace.material.specularCoefficientBlue  * lightsource.rgbComponents[2];

			redColor = (int) ((ambientColor_red + diffuse_red + specular_red) * 255);
			greenColor = (int) ((ambientColor_green + diffuse_green + specular_green) * 255);
			blueColor = (int) ((ambientColor_blue + diffuse_blue + specular_blue) * 255);

			// clamp colors
			if (redColor > 255)
				redColor = 255;
			if (greenColor > 255)
				greenColor = 255;
			if (blueColor > 255)
				blueColor = 255;

			return ((diffuseAlpha & 0xFF) << 24) | ((redColor & 0xFF) << 16) | ((greenColor & 0xFF) << 8)
					| ((blueColor & 0xFF) << 0);
		}
	}
	

	private int getNormal(float x, float y) {

		int normal;

		if (!super.isBilinearFilteringEnabled) {
			// int x_int = (int)Math.floor(x + 0.5f);
			// int y_int = (int)Math.floor(y + 0.5f);
			normal = normalMap.getPixel((int) x, (int) y);
		} else {
			int x_int = (int) x;
			int y_int = (int) y;

			normal = normalMap.getPixel(x_int, y_int);

			float x_fract = x - x_int;
			float y_fract = y - y_int;

			int c0_x = (normal >> 16) & 0xff;
			int c0_y = (normal >> 8) & 0xff;
			int c0_z = (normal >> 0) & 0xff;

			int c1_normal = x_int + 1 < textureWidth ? normalMap.getPixel(x_int + 1, y_int) : normal;
			int c1_x = (c1_normal >> 16) & 0xff;
			int c1_y = (c1_normal >> 8) & 0xff;
			int c1_z = (c1_normal >> 0) & 0xff;

			int c2_normal = y_int + 1 < textureHeight ? normalMap.getPixel(x_int, y_int + 1) : normal;
			int c2_x = (c2_normal >> 16) & 0xff;
			int c2_y = (c2_normal >> 8) & 0xff;
			int c2_z = (c2_normal >> 0) & 0xff;

			int c3_normal = x_int + 1 < textureWidth && y_int + 1 < textureHeight
					? normalMap.getPixel(x_int + 1, y_int + 1) : normal;
			int c3_x = (c3_normal >> 16) & 0xff;
			int c3_y = (c3_normal >> 8) & 0xff;
			int c3_z = (c3_normal >> 0) & 0xff;

			float c0_weight = (1 - x_fract) * (1 - y_fract);
			float c1_weight = x_fract * (1 - y_fract);
			float c2_weight = (1 - x_fract) * y_fract;
			float c3_weight = x_fract * y_fract;

			int newX = (int) (c0_x * c0_weight + c1_x * c1_weight + c2_x * c2_weight + c3_x * c3_weight);

			int newY = (int) (c0_y * c0_weight + c1_y * c1_weight + c2_y * c2_weight + c3_y * c3_weight);

			int newZ = (int) (c0_z * c0_weight + c1_z * c1_weight + c2_z * c2_weight + c3_z * c3_weight);

			normal = ((newX & 0xFF) << 16) | ((newY & 0xFF) << 8) | ((newZ & 0xFF) << 0);

		}

		return normal;
	}

}
