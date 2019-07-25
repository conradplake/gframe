package gframe.engine;

public class NormalMappedTextureShader extends TextureShader {

	private ImageRaster normalMap;
	
	private ImageRaster[] normalMapLODs;
	
	private boolean specularityFromAlphaChannel;

	private int texel;

	// dither delta
	private float du;
	private float dv;
	
	private int texelNormal;
	private float tNormal_x;
	private float tNormal_y;
	private float tNormal_z;
		
	
	private int diffuseAlpha;
	private float diffuseRed;
	private float diffuseGreen;
	private float diffuseBlue;
						
	private float specularCoefficient;

	private Vector3D tangentLocalLightsourcePosition = new Vector3D();
	private Vector3D tangentLocalViewPosition = new Vector3D();

	
	
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
		this.normalMapLODs = mipmaps(normalMap);
		this.specularityFromAlphaChannel = specularityFromAlphaChannel;
	}

	/**
	 * Provide texture, normal and specular map of equal dimensions! Normal
	 * map's alpha channel will be overwritten by specular map gray values!
	 */
	public NormalMappedTextureShader(Lightsource lightsource, ImageRaster texture, ImageRaster normalMap,
			ImageRaster specularMap) {
		super(lightsource, texture);

		// specular map als alpha-channel in die normal map kopieren
		copySpecularMapToAlphaChannel(specularMap, normalMap);
				
		this.normalMap = normalMap;
		this.normalMapLODs = mipmaps(normalMap);
		this.specularityFromAlphaChannel = true;
	}

	@Override
	public void adjustLOD(RenderFace renderFace) {

		int lod = getLOD(renderFace);

		ImageRaster lodTexture = textureLODs[lod];
		ImageRaster lodNormal = normalMapLODs[lod];

		this.normalMap = lodNormal;
		this.texture = lodTexture;
		this.textureWidth = lodTexture.getWidth();
		this.textureHeight = lodTexture.getHeight();		
	}

	
	@Override
	void setEffectPixel(int x, int y, int c) {
		super.setEffectPixel(x, y, c);
		normalMap.setAlpha(x, y, 0);		
	};
	
	
	@Override
	void recomputeMipmaps() {
		super.recomputeMipmaps();
		this.normalMapLODs = mipmaps(normalMapLODs[0]);		
	};
	
	

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

		u = Toolbox.clamp(texel_u * textureWidth + du, 0, textureWidth - 1);
		v = Toolbox.clamp(texel_v * textureHeight + dv, 0, textureHeight - 1);
		
		texel = super.getTexel(u, v);
		

		texelNormal = this.getNormal(u, v);
		tNormal_x = (((texelNormal >> 16) & 0xff) - 128) * iNormalNorm; // red
		tNormal_y = -(((texelNormal >> 8) & 0xff) - 128) * iNormalNorm; // green
		tNormal_z = (((texelNormal >> 0) & 0xff) - 128) * iNormalNorm; // blue

		// see:
		// - literature/26-BumpMap+ProcTex.pdf
		// - http://www.terathon.com/code/tangent.html
		
		Matrix3D inverseTangentSpace = renderFace.getInverseTangentSpace();
		
		// overwrite z-axis with interpolated normal vector
		inverseTangentSpace.setZAxis(normal_x, normal_y, normal_z);
		
		
		tangentLocalLightsourcePosition.x = lightPos_x - world_x;
		tangentLocalLightsourcePosition.y = lightPos_y - world_y;
		tangentLocalLightsourcePosition.z = lightPos_z - world_z;
		inverseTangentSpace.transform(tangentLocalLightsourcePosition);
		tangentLocalLightsourcePosition.normalize();

		lightNormalProduct = tangentLocalLightsourcePosition.dotProduct(tNormal_x, tNormal_y, tNormal_z);

		// object color
		diffuseAlpha = (texel >> 24) & 0xff;
		diffuseRed = ((texel >> 16) & 0xff) * iColorNorm;
		diffuseGreen = ((texel >> 8) & 0xff) * iColorNorm;
		diffuseBlue = ((texel >> 0) & 0xff) * iColorNorm;
		
		diffuseIntensity = Math.max(lightNormalProduct, 0) * lightsource.getIntensity(world_x, world_y, world_z);
		
		if (!specularityFromAlphaChannel) {
			// return diffuse color
			
			diffuse_red   = diffuseIntensity * lightsource.rgbComponents[0] * diffuseRed;
			diffuse_green = diffuseIntensity * lightsource.rgbComponents[1] * diffuseGreen;
			diffuse_blue  = diffuseIntensity * lightsource.rgbComponents[2] * diffuseBlue;
			
			redColor = (int) ((ambientColor_red + diffuse_red) * 255);
			greenColor = (int) ((ambientColor_green + diffuse_green) * 255);
			blueColor = (int) ((ambientColor_blue + diffuse_blue) * 255);

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
		else {
			// here we do phong lighting in tangent space

			// DIFFUSE
			diffuse_red   = diffuseIntensity * renderFace.material.diffuseCoefficientRed   * diffuseRed;
			diffuse_green = diffuseIntensity * renderFace.material.diffuseCoefficientGreen * diffuseGreen;
			diffuse_blue  = diffuseIntensity * renderFace.material.diffuseCoefficientBlue  * diffuseBlue;

			// SPECULAR
			specular_red = 0;
			specular_green = 0;
			specular_blue = 0;
			if(addSpecularity){		
				
				// put vector world_position --> camera into tangent space
				tangentLocalViewPosition.x = camPosition.x - world_x;
				tangentLocalViewPosition.y = camPosition.y - world_y;
				tangentLocalViewPosition.z = camPosition.z - world_z;			
				tangentLocalViewPosition = inverseTangentSpace.transform(tangentLocalViewPosition);
				tangentLocalViewPosition.normalize();			

				// an oberfläche reflekierten Lichtvektor berechnen
				// this overwrites the original vector so make sure it is not used
				// for seomething els later on!
				reflection.x = tangentLocalLightsourcePosition.x - 2 * lightNormalProduct * tNormal_x;
				reflection.y = tangentLocalLightsourcePosition.y - 2 * lightNormalProduct * tNormal_y;
				reflection.z = tangentLocalLightsourcePosition.z - 2 * lightNormalProduct * tNormal_z;
				
				// wenn camera genau in den reflektierten lichtstrahl blickt, dann
				// haben wir maximale spekularität (shininess)
				viewReflectionProduct = tangentLocalViewPosition.dotProduct(reflection);

				
//				 viewReflectionProduct = (float) Math.pow(viewReflectionProduct, 16); // too expensive :(			
				// --> schlick's approximation:
				viewReflectionProduct = -viewReflectionProduct
						/ (renderFace.material.shininess + (renderFace.material.shininess * viewReflectionProduct) - viewReflectionProduct);

				specularIntensity = Math.max(viewReflectionProduct, 0);
				specularCoefficient = ((texelNormal >> 24) & 0xff) * iColorNorm; // specularity from normal map's alpha-channel in [0..1]

				specIntermediate = specularIntensity * specularCoefficient;			
				specular_red   = specIntermediate * renderFace.material.specularCoefficientRed   * lightsource.rgbComponents[0];
				specular_green = specIntermediate * renderFace.material.specularCoefficientGreen * lightsource.rgbComponents[1];
				specular_blue  = specIntermediate * renderFace.material.specularCoefficientBlue  * lightsource.rgbComponents[2];
			}				

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
