package gframe.engine.timing;

import gframe.engine.Camera;
import gframe.engine.Point3D;
import gframe.engine.generator.NoiseGenerator;

/**
 * Effekt: wakeln der camera in zufällige (smooth noise!) richtungen
 * */
public class CamShake implements Timed{
	
	Point3D camOrigin;	
	
	long shakeTimeInMillisLeft;
	
	float delta_x;
	float delta_y;
	float delta_z;
	
	float shakeIntensity = 10;
	
	public CamShake(Camera camera, long shakeTimeInMillis){
		this.camOrigin = camera.getOrigin();	
		this.shakeTimeInMillisLeft = shakeTimeInMillis;						
	}
	
	@Override
	public void timePassedInMillis(long millis) {
		
		// idea: add some (perlin) noise to cam's origin		
		float shake_x = shakeIntensity * (float)NoiseGenerator.improvedPerlinNoise(camOrigin.x, 0,0);
		float shake_y = shakeIntensity * (float)NoiseGenerator.improvedPerlinNoise(0, camOrigin.y, 0);
		float shake_z = shakeIntensity * (float)NoiseGenerator.improvedPerlinNoise(0, 0, camOrigin.z);				
//		float shake = shakeIntensity * (float)NoiseGenerator.improvedPerlinNoise(camOrigin.x, camOrigin.y, camOrigin.z);
		
		delta_x+=shake_x;
		delta_y+=shake_y;
		delta_z+=shake_z;
							
		camOrigin.move(shake_x, shake_y, shake_z);
		
		shakeTimeInMillisLeft -= millis;			
		if(shakeTimeInMillisLeft<=0){
			camOrigin.move(-delta_x, -delta_y, -delta_z); // reset cam position
		}
	}

	@Override
	public boolean done() {
		return shakeTimeInMillisLeft<=0;
	}
	
}