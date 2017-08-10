package gframe.engine;

public enum Material {
	
	DEFAULT(0.01f, 0.01f, 0.01f,  1f, 1f, 1f,  1f, 1f, 1f, 10f), // pure whiteness
	
	GOLD(0.24725f, 0.1995f, 0.0745f,  0.75164f, 0.60648f, 0.22648f,  0.628281f, 0.555802f, 0.366065f, 51.2f),
	SILVER(0.19225f, 0.19225f, 0.19225f,  0.50754f, 0.50754f, 0.50754f, 0.508273f, 0.508273f, 0.508273f, 51.2f),
	COPPER(0.19125f, 0.0735f, 0.0225f,  0.7038f, 0.27048f, 0.0828f, 0.256777f, 0.137622f, 0.086014f, 12.8f),
	CHROME(0.25f, 0.25f, 0.25f, 0.4f, 0.4f, 0.4f, 0.774597f, 0.774597f, 0.774597f, 76.8f),
	
	PEARL(0.25f, 0.20725f, 0.20725f, 1f, 0.829f, 0.829f, 0.296648f, 0.296648f, 0.296648f, 11.264f),
	EMERALD(0.0215f, 0.1745f, 0.0215f, 0.07568f, 0.61424f, 0.07568f, 0.633f, 0.727811f, 0.633f, 76.8f),
	RUBY(0.1745f, 0.01175f, 0.01175f, 0.61424f, 0.04136f, 0.04136f, 0.727811f, 0.626959f, 0.626959f, 76.8f),
	TURQUOISE(0.1f, 0.18725f, 0.1745f, 0.396f, 0.74151f, 0.69102f, 0.297254f, 0.30829f, 0.306678f, 12.8f),
	
	BLACK_RUBBER(0.02f, 0.02f, 0.02f, 0.01f, 0.01f, 0.01f, 0.4f, 0.4f, 0.4f, 100f),	
	BLACK_PLASTIC(0f, 0f, 0f, 0.01f, 0.01f, 0.01f, 0.5f, 0.5f, 0.5f, 32f),
	WHITE_PLASTIC(0f, 0f, 0f, 0.55f, 0.55f, 0.55f, 0.7f, 0.7f, 0.7f, 32f),
	RED_PLASTIC(0f, 0f, 0f, 0.5f, 0f, 0f, 0.7f, 0.6f, 0.6f, 32f);
	
	
	Material(float ar, float ag, float ab, float dr, float dg, float db, float sr, float sg, float sb, float shininess){
		this.ambientCoefficientRed = ar;
		this.ambientCoefficientGreen = ag;
		this.ambientCoefficientBlue = ab;
		
		this.ambientCoefficient = (ar+ag+ab)/3f;
		
		this.diffuseCoefficientRed = dr;
		this.diffuseCoefficientGreen = dg;
		this.diffuseCoefficientBlue = db;
		
		this.diffuseCoefficient = (dr+dg+db)/3f;
		
		this.specularCoefficientRed = sr;
		this.specularCoefficientGreen = sg;
		this.specularCoefficientBlue = sb;
		
		this.specularCoefficient = (sr+sg+sb)/3f;
		
		this.shininess = shininess;
	};
	
	
	float ambientCoefficient;	
	float ambientCoefficientRed;
	float ambientCoefficientGreen;
	float ambientCoefficientBlue;
	
	float diffuseCoefficient;
	float diffuseCoefficientRed;
	float diffuseCoefficientGreen;
	float diffuseCoefficientBlue;
	
	float specularCoefficient;
	float specularCoefficientRed;
	float specularCoefficientGreen;
	float specularCoefficientBlue;
	
	float shininess;
}
