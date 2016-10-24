package gframe.engine;

import java.awt.Color;

public class Lightsource extends Point3D {

	public Lightsource(float x, float y, float z, Color col, float intensity) {
		super(x, y, z);
		setColor(col);
		setIntensity(intensity);
		rgbComponents = col.getRGBComponents(new float[4]);
	}

	public Lightsource(Point3D origin, Color col, float intensity) {
		super(origin.x, origin.y, origin.z);
		setColor(col);
		setIntensity(intensity);
		
		rgbComponents = col.getRGBComponents(new float[4]);
	}


	public float getIntensity() {
		return intensity;
	}

	public void setIntensity(float i) {
		intensity = i;
	}

	public Color getColor() {
		return col;
	}

	public void setColor(Color c) {
		col = c;
		col.getRGBComponents(rgbComponents);
	}
	
	public Lightsource copy(){
		return new Lightsource(super.copy(), col, intensity);
	}

	public static final float MIN_INTENSITY = 0.01f;
	public static final float NORM_INTENSITY = 0.66f;
	public static final float MAX_INTENSITY = 1f;

	float intensity;
	Color col;
	float[] rgbComponents;
}