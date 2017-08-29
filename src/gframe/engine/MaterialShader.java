package gframe.engine;

import gframe.engine.generator.TextureGenerator;
import imaging.ImageRaster;

public class MaterialShader extends TextureShader {

	static final float iNormalNorm = 1 / 127f;

	private Point3D camPosition;
	private float lightPos_x;
	private float lightPos_y;
	private float lightPos_z;

	// texture normal coordinates
	private float u;
	private float v;

	private int texelNormal;
	private float tNormal_x;
	private float tNormal_y;
	private float tNormal_z;

	private float lightNormalProduct;

	private int diffuseAlpha = 255;
	private float diffuseRed = 1;
	private float diffuseGreen = 1;
	private float diffuseBlue = 1;

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
	 * 
	 */
	public MaterialShader(Lightsource lightsource, ImageRaster normalMap) {
		super(lightsource, normalMap);
	}

	/**
	 * Provide normal and specular map of equal dimensions! Normal map's alpha
	 * channel will be overwritten by specular map gray values!
	 */
	public MaterialShader(Lightsource lightsource, ImageRaster normalMap, ImageRaster specularMap) {
		super(lightsource, normalMap);
		TextureGenerator.copySpecularMapToAlphaChannel(specularMap, normalMap);
		super.recomputeMipmaps();
	}

	public boolean isAddSpecularity() {
		return addSpecularity;
	}

	public void setAddSpecularity(boolean addSpecularity) {
		this.addSpecularity = addSpecularity;
	}

	@Override
	public void preShade(RenderFace renderFace) {

		super.preShade(renderFace);
		
		this.lightPos_x = lightsource.x;
		this.lightPos_y = lightsource.y;
		this.lightPos_z = lightsource.z;

		this.camPosition = renderFace.getCameraPosition();
		// world_to_camera in den tangentenraum transformieren (farbe wird im
		// tangentraum errechnet)
		// this.tangentLocalViewPosition =
		// renderFace.getInverseTangentSpace().transform(new
		// Vector3D(camPosition.x - renderFace.centroid.x, camPosition.y -
		// renderFace.centroid.y, camPosition.z - renderFace.centroid.z));
		// tangentLocalViewPosition.normalize();
	}

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

				
		u = Math.min(textureWidth - 1, texel_u * (textureWidth));
		v = Math.min(textureHeight - 1, texel_v * (textureHeight));
		if(u<0)u=0;
		if(v<0)v=0;

		texelNormal = this.getTexel(u, v);
		tNormal_x = (((texelNormal >> 16) & 0xff) - 128) * iNormalNorm; // red
		tNormal_y = -(((texelNormal >> 8) & 0xff) - 128) * iNormalNorm; // green
		tNormal_z = (((texelNormal >> 0) & 0xff) - 128) * iNormalNorm; // blue

		Matrix3D inverseTangentSpace = renderFace.getInverseTangentSpace();

		// overwrite the face normal with interpolated normal
		inverseTangentSpace.setZAxis(normal_x, normal_y, normal_z);

		Vector3D tangentLocalLightsourcePosition = inverseTangentSpace
				.transform(new Vector3D(lightPos_x - world_x, lightPos_y - world_y, lightPos_z - world_z));
		tangentLocalLightsourcePosition.normalize();

		// diffuse Intensity
		lightNormalProduct = tangentLocalLightsourcePosition.dotProduct(tNormal_x, tNormal_y, tNormal_z);

		// object/diffuse color
		diffuseAlpha = 255;
		diffuseRed = 1;
		diffuseGreen = 1;
		diffuseBlue = 1;

		// AMBIENT
		// float ambientIntermediate = Lightsource.AMBIENT_LIGHT_INTENSITY *
		// currentAmbientCoefficient;
		// float ambientColor_red = ambientIntermediate * diffuseRed;
		// float ambientColor_green = ambientIntermediate * diffuseGreen;
		// float ambientColor_blue = ambientIntermediate * diffuseBlue;
		ambientColor_red = renderFace.material.ambientCoefficientRed * diffuseRed;
		ambientColor_green = renderFace.material.ambientCoefficientGreen * diffuseGreen;
		ambientColor_blue = renderFace.material.ambientCoefficientBlue * diffuseBlue;

		// DIFFUSE
		diffuseIntensity = Math.max(lightNormalProduct, 0);
		// float diffuseIntermediate = diffuseIntensity *
		// currentAmbientCoefficient;
		// diffuse_red = diffuseIntermediate * diffuseRed;
		// diffuse_green = diffuseIntermediate * diffuseGreen;
		// diffuse_blue = diffuseIntermediate * diffuseBlue;
		diffuse_red = diffuseIntensity * renderFace.material.diffuseCoefficientRed * diffuseRed;
		diffuse_green = diffuseIntensity * renderFace.material.diffuseCoefficientGreen * diffuseGreen;
		diffuse_blue = diffuseIntensity * renderFace.material.diffuseCoefficientBlue * diffuseBlue;

		// SPECULAR
		specular_red = 0;
		specular_green = 0;
		specular_blue = 0;

		if (addSpecularity) {

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

			// wenn camera genau in den reflektierten lichtstrahl blickt, dann
			// haben wir maximale spekularität (shininess)
			viewReflectionProduct = tangentLocalViewPosition.dotProduct(tangentLocalLightsourcePosition);

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
			// specular_red = specIntermediate * lightsource.rgbComponents[0];
			// specular_green = specIntermediate * lightsource.rgbComponents[1];
			// specular_blue = specIntermediate * lightsource.rgbComponents[2];
			specular_red = specIntermediate * renderFace.material.specularCoefficientRed * lightsource.rgbComponents[0];
			specular_green = specIntermediate * renderFace.material.specularCoefficientGreen
					* lightsource.rgbComponents[1];
			specular_blue = specIntermediate * renderFace.material.specularCoefficientBlue
					* lightsource.rgbComponents[2];

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
