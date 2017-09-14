package gframe.engine;

/**
 * 
 * See interface class for comments.
 * 
 **/
public abstract class AbstractShader implements Shader {

	Lightsource lightsource;

	static final float iColorNorm = 1 / 255f;
	static final float iNormalNorm = 1 / 127f;

	boolean addSpecularity = true;

	private Material material;
	
	private Point3D camPosition;
	private float lightPos_x;
	private float lightPos_y;
	private float lightPos_z;

	private Vector3D toLight;

	private float lightNormalProduct;

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
	
	
	

	public AbstractShader(Lightsource lightsource) {
		this.lightsource = lightsource;
	}

	public void setLightsource(Lightsource ls) {
		this.lightsource = ls;
	}

	public Lightsource getLightsource() {
		return this.lightsource;
	}

	public boolean isAddSpecularity() {
		return addSpecularity;
	}

	public void setAddSpecularity(boolean addSpecularity) {
		this.addSpecularity = addSpecularity;
	}

	/**
	 * Set ambient color etc
	 */
	public void preShade(RenderFace renderFace) {
		
		this.lightPos_x = lightsource.x;
		this.lightPos_y = lightsource.y;
		this.lightPos_z = lightsource.z;
		
		if (lightsource.isDirectional()) {
			toLight = lightsource.getZVector().copy().multiply(-1);
		}
		
		material = renderFace.material;
		if(material!=null){			
			ambientColor_red = material.ambientCoefficientRed * Lightsource.AMBIENT_LIGHT_INTENSITY;
			ambientColor_green = material.ambientCoefficientGreen * Lightsource.AMBIENT_LIGHT_INTENSITY;
			ambientColor_blue = material.ambientCoefficientBlue * Lightsource.AMBIENT_LIGHT_INTENSITY;
						
		}	else{
			ambientColor_red = 0;
			ambientColor_green = 0;
			ambientColor_blue = 0;
		}	
							
		this.camPosition = renderFace.getCameraPosition();
	}

	public int shade(RenderFace renderFace) {
		return renderFace.col.getRGB();
	}

//	public int shade(int diffuseColor, float world_x, float world_y, float world_z, float normal_x, float normal_y,
//			float normal_z) {
//
//		Vector3D ls_face;
//
//		if (lightsource.isDirectional()) {
//			ls_face = lightsource.getZVector();
//		} else {
//			ls_face = new Vector3D(world_x - lightsource.x, world_y - lightsource.y, world_z - lightsource.z);
//			ls_face.normalize();
//		}
//
//		float dp = -ls_face.dotProduct(normal_x, normal_y, normal_z);
//		if (dp < 0) {
//			dp = 0;
//		}
//
//		return shade(diffuseColor, 1.0f, dp);
//	}

	public int shade(int diffuseColor, float diffuseReflectionCoefficient, float lightNormalProduct) {

		int diffuseAlpha = (diffuseColor >> 24) & 0xff;
		int diffuseRed = (diffuseColor >> 16) & 0xff;
		int diffuseGreen = (diffuseColor >> 8) & 0xff;
		int diffuseBlue = (diffuseColor >> 0) & 0xff;

		float diffuseRedIntensity = diffuseRed * iColorNorm;
		float diffuseGreenIntensity = diffuseGreen * iColorNorm;
		float diffuseBlueIntensity = diffuseBlue * iColorNorm;

		float redIntensity = diffuseRedIntensity * diffuseReflectionCoefficient * lightsource.rgbComponents[0]
				* lightsource.intensity * lightNormalProduct;
		float greenIntensity = diffuseGreenIntensity * diffuseReflectionCoefficient * lightsource.rgbComponents[1]
				* lightsource.intensity * lightNormalProduct;
		float blueIntensity = diffuseBlueIntensity * diffuseReflectionCoefficient * lightsource.rgbComponents[2]
				* lightsource.intensity * lightNormalProduct;

		int newDiffuseRed = (int) (diffuseRed * redIntensity);
		int newDiffuseGreen = (int) (diffuseGreen * greenIntensity);
		int newDiffuseBlue = (int) (diffuseBlue * blueIntensity);

		return ((diffuseAlpha & 0xFF) << 24) | ((newDiffuseRed & 0xFF) << 16) | ((newDiffuseGreen & 0xFF) << 8)
				| ((newDiffuseBlue & 0xFF) << 0);
	}
	

	public int shade(int diffuseColor, float world_x, float world_y, float world_z,
			float normal_x, float normal_y, float normal_z) {

		if (!lightsource.isDirectional()) {
			toLight = new Vector3D(lightPos_x - world_x, lightPos_y - world_y, lightPos_z - world_z);
			toLight.normalize();
		}

		lightNormalProduct = toLight.dotProduct(normal_x, normal_y, normal_z);
		
		// DIFFUSE
		int diffuseAlpha = (diffuseColor >> 24) & 0xff;
		diffuse_red = ((diffuseColor >> 16) & 0xff) * iColorNorm;
		diffuse_green = ((diffuseColor >> 8) & 0xff) * iColorNorm;
		diffuse_blue = ((diffuseColor >> 0) & 0xff) * iColorNorm;

		diffuseIntensity = Math.max(lightNormalProduct, 0) * lightsource.getIntensity();
		if(material!=null){
			diffuse_red = diffuseIntensity * material.diffuseCoefficientRed * diffuse_red;
			diffuse_green = diffuseIntensity * material.diffuseCoefficientGreen * diffuse_green;
			diffuse_blue = diffuseIntensity * material.diffuseCoefficientBlue * diffuse_blue;
		}else{
			diffuse_red = diffuseIntensity * diffuse_red;
			diffuse_green = diffuseIntensity * diffuse_green;
			diffuse_blue = diffuseIntensity * diffuse_blue;	
		}
		

		specular_red = 0;
		specular_green = 0;
		specular_blue = 0;
		if (addSpecularity && material!=null) {

			Vector3D viewPosition = new Vector3D(camPosition.x - world_x, camPosition.y - world_y,
					camPosition.z - world_z);
			viewPosition.normalize();

			// an oberfläche reflekierten Lichtvektor berechnen
			Vector3D reflection = new Vector3D(toLight.x - 2 * lightNormalProduct * normal_x,  toLight.y - 2 * lightNormalProduct * normal_y,  toLight.z - 2 * lightNormalProduct * normal_z);

			// wenn camera genau in den reflektierten lichtstrahl blickt, dann
			// haben wir maximale spekularität (shininess)
			viewReflectionProduct = viewPosition.dotProduct(reflection);

			// viewReflectionProduct = (float) Math.pow(viewReflectionProduct,
			// 16);
			// // too expensive :(
			// --> schlick's approximation:
			viewReflectionProduct = -viewReflectionProduct
					/ (material.shininess + (material.shininess * viewReflectionProduct) - viewReflectionProduct);

			specularIntensity = Math.max(viewReflectionProduct, 0) * lightsource.getIntensity();
			specularCoefficient = material.specularCoefficient;
			specIntermediate = specularIntensity * specularCoefficient;
			specular_red = specIntermediate * material.specularCoefficientRed * lightsource.rgbComponents[0];
			specular_green = specIntermediate * material.specularCoefficientGreen * lightsource.rgbComponents[1];
			specular_blue = specIntermediate * material.specularCoefficientBlue * lightsource.rgbComponents[2];

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
