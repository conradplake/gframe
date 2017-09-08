package gframe.engine;

/**
 * 
 * See interface class for comments.
 * 
 **/
public abstract class AbstractShader implements Shader {

	Lightsource lightsource;
	static final float iColorNorm = 1 / 255f;
	boolean addSpecularity = true;
	
	
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
	 * Does nothing by default.
	 */
	public void preShade(RenderFace renderFace) {
	}

	public int shade(RenderFace renderFace) {
		return renderFace.col.getRGB();
	}
	
	
	public int shade(int diffuseColor, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z) {

		Vector3D ls_face;

		if (lightsource.isDirectional()) {
			ls_face = lightsource.getZVector();
		} else {
			ls_face = new Vector3D(world_x - lightsource.x, world_y - lightsource.y, world_z - lightsource.z);
			ls_face.normalize();
		}

		float dp = -ls_face.dotProduct(normal_x, normal_y, normal_z);
		if (dp < 0) {
			dp = 0;
		}

		return shade(diffuseColor, 1.0f, dp);
	}


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


}
