package gframe.engine;

import java.awt.Color;


/**
 * 
 * See interface class for comments.
 *  
 * **/
public abstract class AbstractShader implements Shader {

	Lightsource lightsource;
	
	DirectionalLight directionalLight;
	
	float[] diffuseRGBComponents;
	
	static final float iColorNorm = 1/255f;	
	
	public AbstractShader(Lightsource lightsource) {
		this.lightsource = lightsource;				
		this.diffuseRGBComponents = new float[4];
	}
	
	public void setLightsource(Lightsource ls){
		this.lightsource = ls;
	}
	
	public Lightsource getLightsource(){
		return this.lightsource;
	}
	
	
	public void setDirectionalLight(DirectionalLight dl){
		this.directionalLight = dl;
	}
	
	public DirectionalLight getDirectionalLight(){
		return this.directionalLight;
	}
	
	
	/**
	 * Does nothing by default.
	 * */
	public void preShade(RenderFace renderFace) {		
	}
	
	
	public int shade(RenderFace renderFace) {
		return renderFace.col.getRGB();
	}
	
		
	public int shade(int diffuseAlpha, int diffuseRed, int diffuseGreen, int diffuseBlue, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z) {		
		Color diffuseColor = new Color(diffuseRed, diffuseGreen, diffuseBlue);		
		return this.shade(diffuseColor, world_x, world_y, world_z, normal_x, normal_y, normal_z);		
	}
	
	
	public int shade(Color diffuseColor, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z) {
		Vector3D ls_face = new Vector3D(world_x - lightsource.x, world_y - lightsource.y, world_z - lightsource.z);		
		
		ls_face.normalize();
		
//		if(directionalLight!=null){
//			ls_face = directionalLight.getZVector();
//		}else{
//			ls_face.normalize();
//		}
		
		float dp = -ls_face.dotProduct(normal_x, normal_y, normal_z);
		if (dp < 0) {
			dp = 0;
		}
						
		return shade(diffuseColor, 1.0f, dp);
	}	
	
	
	// Intensity = Intensity_lightsource x Kd x cos(theta)  or Intensity = Intensity_lightsource x Kd x (N' * L')
	// Kd - diffuse reflection coefficient in [0..1]
	public int shade(Color diffuseColor, float diffuseReflectionCoefficient, float lightNormalProduct){							
			
		diffuseColor.getRGBComponents(diffuseRGBComponents);						
		
		float redIntensity 		= diffuseRGBComponents[0] * diffuseReflectionCoefficient * lightsource.rgbComponents[0] * lightsource.intensity * lightNormalProduct;
		float greenIntensity 	= diffuseRGBComponents[1] * diffuseReflectionCoefficient * lightsource.rgbComponents[1] * lightsource.intensity * lightNormalProduct;
		float blueIntensity 	= diffuseRGBComponents[2] * diffuseReflectionCoefficient * lightsource.rgbComponents[2] * lightsource.intensity * lightNormalProduct;
				
		int newRed = (int)(diffuseColor.getRed() * redIntensity);
		int newGreen = (int)(diffuseColor.getGreen() * greenIntensity);
		int newBlue = (int)(diffuseColor.getBlue() * blueIntensity);
		
		return  ((diffuseColor.getAlpha() & 0xFF) << 24) |
                ((newRed & 0xFF) << 16) |
                ((newGreen & 0xFF) << 8)  |
                ((newBlue & 0xFF) << 0);
	}
	
	
	public int shade(int diffuseColor, float diffuseReflectionCoefficient, float lightNormalProduct){							
		
										
		int diffuseAlpha = (diffuseColor >> 24) & 0xff;
		int diffuseRed = (diffuseColor >> 16) & 0xff;
		int diffuseGreen = (diffuseColor >> 8) & 0xff;
		int diffuseBlue = (diffuseColor >> 0) & 0xff;
		
		float diffuseRedIntensity = diffuseRed * iColorNorm;
		float diffuseGreenIntensity = diffuseGreen * iColorNorm;
		float diffuseBlueIntensity = diffuseBlue * iColorNorm;
		
		float redIntensity 		= diffuseRedIntensity * diffuseReflectionCoefficient * lightsource.rgbComponents[0] * lightsource.intensity * lightNormalProduct;
		float greenIntensity 	= diffuseGreenIntensity * diffuseReflectionCoefficient * lightsource.rgbComponents[1] * lightsource.intensity * lightNormalProduct;
		float blueIntensity 	= diffuseBlueIntensity * diffuseReflectionCoefficient * lightsource.rgbComponents[2] * lightsource.intensity * lightNormalProduct;
							
		
		int newDiffuseRed = (int)(diffuseRed * redIntensity);
		int newDiffuseGreen = (int)(diffuseGreen * greenIntensity);
		int newDiffuseBlue = (int)(diffuseBlue * blueIntensity);
		
		return  ((diffuseAlpha & 0xFF) << 24) |
                ((newDiffuseRed & 0xFF) << 16) |
                ((newDiffuseGreen & 0xFF) << 8)  |
                ((newDiffuseBlue & 0xFF) << 0);
	}
	
	
	
}
