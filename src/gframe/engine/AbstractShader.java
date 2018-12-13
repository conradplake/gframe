package gframe.engine;

/**
 * 
 * See interface class for comments.
 * 
 **/
public abstract class AbstractShader implements Shader {

	protected Lightsource lightsource;

	public static final float iColorNorm = 1 / 255f;
	public static final float iNormalNorm = 1 / 127f;

	
	boolean addSpecularity = true;

	private Material material;
	
	Point3D camPosition;
	float lightPos_x;
	float lightPos_y;
	float lightPos_z;
	float lightIntensity;

	Vector3D toLight;
	Vector3D toCamera;
	Vector3D reflection;

	float lightNormalProduct;

	float ambientColor_red;
	float ambientColor_green;
	float ambientColor_blue;

	float diffuseIntensity;
	float diffuse_red;
	float diffuse_green;
	float diffuse_blue;
	int diffuse_alpha;

	float viewReflectionProduct;

	float specularIntensity;
	float specIntermediate;
	float specular_red;
	float specular_green;
	float specular_blue;

	int redColor;
	int greenColor;
	int blueColor;
	
	
	
	public AbstractShader(Lightsource lightsource) {
		this.lightsource = lightsource;
		
		this.toLight = new Vector3D();
		this.toCamera = new Vector3D();
		this.reflection = new Vector3D();
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
		this.lightIntensity = lightsource.getIntensity();
		
		if (lightsource.isDirectional()) {
			toLight.x = -lightsource.getZVector().x;
			toLight.y = -lightsource.getZVector().y;
			toLight.z = -lightsource.getZVector().z;		
		}		
		
		this.camPosition = renderFace.getCameraPosition();
		
		
		// AMBIENT
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
	}

	public int shade(RenderFace renderFace) {
		return renderFace.col.getRGB();
	}
	

	/**
	 * Returns a color made of ambient, diffuse and specular components. 
	 * */
	public int shade(int diffuseColor, float world_x, float world_y, float world_z,
			float normal_x, float normal_y, float normal_z) {

		if (!lightsource.isDirectional()) {
			toLight.x = lightPos_x - world_x;
			toLight.y = lightPos_y - world_y;
			toLight.z = lightPos_z - world_z;
			toLight.normalize();
		}

		lightNormalProduct = toLight.dotProduct(normal_x, normal_y, normal_z);
		
		if(lightsource.isAddAttenuation()){
		  lightIntensity = lightsource.getIntensity(world_x, world_y, world_z);
		}
		
		// DIFFUSE
		diffuse_alpha = (diffuseColor >> 24) & 0xff;
		diffuse_red = ((diffuseColor >> 16) & 0xff) * iColorNorm;
		diffuse_green = ((diffuseColor >> 8) & 0xff) * iColorNorm;
		diffuse_blue = ((diffuseColor >> 0) & 0xff) * iColorNorm;

		diffuseIntensity = Math.max(lightNormalProduct, 0) * lightIntensity;
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

			toCamera.x = camPosition.x - world_x;
			toCamera.y = camPosition.y - world_y;
			toCamera.z = camPosition.z - world_z;
			toCamera.normalize();

			// an oberfläche reflekierten Lichtvektor berechnen
			reflection.x = toLight.x - 2 * lightNormalProduct * normal_x;
			reflection.y = toLight.y - 2 * lightNormalProduct * normal_y;
			reflection.z = toLight.z - 2 * lightNormalProduct * normal_z;

			// wenn camera genau in den reflektierten lichtstrahl blickt, dann
			// haben wir maximale spekularität (shininess)
			viewReflectionProduct = toCamera.dotProduct(reflection);

			// viewReflectionProduct = (float) Math.pow(viewReflectionProduct,
			// material.shininess);
			// // too expensive :(
			// --> schlick's approximation:
			viewReflectionProduct = -viewReflectionProduct
					/ (material.shininess + (material.shininess * viewReflectionProduct) - viewReflectionProduct);

			specularIntensity = Math.max(viewReflectionProduct, 0) * lightIntensity;			
			specIntermediate = specularIntensity * material.specularCoefficient;
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

		return ((diffuse_alpha & 0xFF) << 24) | ((redColor & 0xFF) << 16) | ((greenColor & 0xFF) << 8)
				| ((blueColor & 0xFF) << 0);

	}
	
}
