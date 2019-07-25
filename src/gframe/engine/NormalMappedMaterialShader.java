package gframe.engine;

public class NormalMappedMaterialShader extends TextureShader {

	private int texelNormal;
	private float tNormal_x;
	private float tNormal_y;
	private float tNormal_z;

	private float specularCoefficient;
	
	private Vector3D tangentLocalLightsourcePosition = new Vector3D();
	private Vector3D tangentLocalViewPosition = new Vector3D();

	
	
	/**
	 * 
	 */
	public NormalMappedMaterialShader(Lightsource lightsource, ImageRaster normalMap) {
		super(lightsource, normalMap);		
	}

	/**
	 * Provide normal and specular map of equal dimensions! Normal map's alpha
	 * channel will be overwritten by specular map gray values!
	 */
	public NormalMappedMaterialShader(Lightsource lightsource, ImageRaster normalMap, ImageRaster specularMap) {
		super(lightsource, normalMap);
		copySpecularMapToAlphaChannel(specularMap, normalMap);
		super.recomputeMipmaps();
	}


	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		u = Toolbox.clamp(texel_u * textureWidth, 0, textureWidth - 1);
		v = Toolbox.clamp(texel_v * textureHeight, 0, textureHeight - 1);

		texelNormal = this.getTexel(u, v);
		tNormal_x = (((texelNormal >> 16) & 0xff) - 128) * iNormalNorm; // red
		tNormal_y = -(((texelNormal >> 8) & 0xff) - 128) * iNormalNorm; // green
		tNormal_z = (((texelNormal >> 0) & 0xff) - 128) * iNormalNorm; // blue

		Matrix3D inverseTangentSpace = renderFace.getInverseTangentSpace();

		// overwrite the face normal with interpolated normal
		// doing so will make tangent space not orthogonal by bending the z-axis along the face.
		// however, it looks acceptably good.
		inverseTangentSpace.setZAxis(normal_x, normal_y, normal_z);

		tangentLocalLightsourcePosition.x = lightPos_x - world_x;
		tangentLocalLightsourcePosition.y = lightPos_y - world_y;
		tangentLocalLightsourcePosition.z = lightPos_z - world_z;
		tangentLocalLightsourcePosition = inverseTangentSpace.transform(tangentLocalLightsourcePosition);
		tangentLocalLightsourcePosition.normalize();

		// DIFFUSE
		lightNormalProduct = tangentLocalLightsourcePosition.dotProduct(tNormal_x, tNormal_y, tNormal_z);
		diffuseIntensity = Math.max(lightNormalProduct, 0) *  lightsource.getIntensity(world_x, world_y, world_z);
		diffuse_red = diffuseIntensity * renderFace.material.diffuseCoefficientRed ;
		diffuse_green = diffuseIntensity * renderFace.material.diffuseCoefficientGreen;
		diffuse_blue = diffuseIntensity * renderFace.material.diffuseCoefficientBlue;

		// SPECULAR
		specular_red = 0;
		specular_green = 0;
		specular_blue = 0;
		if (addSpecularity) {

			// put vector world_position --> camera into tangent space
			tangentLocalViewPosition.x = camPosition.x - world_x;
			tangentLocalViewPosition.y = camPosition.y - world_y;
			tangentLocalViewPosition.z = camPosition.z - world_z;			
			tangentLocalViewPosition = inverseTangentSpace.transform(tangentLocalViewPosition);
			tangentLocalViewPosition.normalize();

			// an oberfläche reflekierten Lichtvektor berechnen
			reflection.x = tangentLocalLightsourcePosition.x - 2 * lightNormalProduct * tNormal_x;
			reflection.y = tangentLocalLightsourcePosition.y - 2 * lightNormalProduct * tNormal_y;
			reflection.z = tangentLocalLightsourcePosition.z - 2 * lightNormalProduct * tNormal_z;

			// wenn camera genau in den reflektierten lichtstrahl blickt, dann
			// haben wir maximale spekularität (shininess)
			viewReflectionProduct = tangentLocalViewPosition.dotProduct(reflection);

			// viewReflectionProduct = (float) Math.pow(viewReflectionProduct,
			// 16);
			// // too expensive :(
			// --> schlick's approximation:
			viewReflectionProduct = -viewReflectionProduct / (renderFace.material.shininess
					+ (renderFace.material.shininess * viewReflectionProduct) - viewReflectionProduct);

			specularIntensity = Math.max(viewReflectionProduct, 0);
			specularCoefficient = ((texelNormal >> 24) & 0xff) * iColorNorm; // specularity
																				// from
																				// normal
																				// map's
																				// alpha-channel
																				// in
																				// [0..1]

			specIntermediate = specularIntensity * specularCoefficient;
			specular_red = specIntermediate * renderFace.material.specularCoefficientRed * lightsource.rgbComponents[0];
			specular_green = specIntermediate * renderFace.material.specularCoefficientGreen * lightsource.rgbComponents[1];
			specular_blue = specIntermediate * renderFace.material.specularCoefficientBlue * lightsource.rgbComponents[2];
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

		return ((255 & 0xFF) << 24) | ((redColor & 0xFF) << 16) | ((greenColor & 0xFF) << 8)
				| ((blueColor & 0xFF) << 0);

	}

}
