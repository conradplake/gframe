package gframe.engine.shader;

import java.awt.Color;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import imaging.ImageRaster;

/**
 * See reference: http://paulbourke.net/geometry/supershape/
 */
public class SuperEllipseShader extends AbstractShader {

	int textureWidth = 512;
	int textureHeight = 512;

	long lastTime = 0;
	long timePassed = 0;

	ImageRaster texture;

	
	int pos_x = textureWidth/2;
	int pos_y = textureHeight/2;
	int r = 50;
	
	float n1=1;
	float n2=1;
	float n3=1;
	float m = 5;
	float a = 1;
	float b = 1;	
	
	// superellipse radii:
//	float a = 100;
//	float b = 100;

	float total = 1000;
	float increment = (float)(Math.PI*2 / total);
	
	float noiseOffset;
	
	Color circleColor = new Color(255, 0, 150, 100);
	

	public SuperEllipseShader(Lightsource lightsource) {
		super(lightsource);
		texture = new ImageRaster(textureWidth, textureHeight);	
		
		// preset color to white
		for (int x = 0; x < textureWidth; x++) {
			for (int y = 0; y < textureHeight; y++) {
				texture.setPixel(x, y, Color.WHITE.getRGB());
			}
		}

		lastTime = System.currentTimeMillis();
	}
	
	private void setBackground(Color c) {
		int rgb = c.getRGB();
		for (int x = 0; x < textureWidth; x++) {
			for (int y = 0; y < textureHeight; y++) {
				texture.setPixel(x, y, rgb);
			}
		}
	}

	@Override
	public void preShade(RenderFace renderFace) {

		long currentTime = System.currentTimeMillis();

		timePassed += (currentTime - lastTime);

		long timestepInMillis = 30;
		
		while (timePassed > timestepInMillis) {
							
			setBackground(Color.white);
			
//			m++;
//			if(m==1000)
//				m=1;
			
			for(float angle=0; angle<=Math.PI*2; angle+=increment){
				
//				float noise = (float)NoiseGenerator.improvedPerlinNoise(noiseOffset+=increment);
//				float randomAngle = (float)Toolbox.map(noise, -1, 1, 0, Math.PI/20);			
				
				//supershape:
				float s = supershape(angle);					
				int x = (int)(r * s * Math.cos(angle));
				int y = (int)(r * s * Math.sin(angle));

				
				// superellipse:
				//int x = (int)( Math.pow(Math.abs(Math.cos(angle)), 2/n1) * a * Math.signum(Math.cos(angle)) );				
				//int y = (int)( Math.pow(Math.abs(Math.sin(angle)), 2/n1) * b * Math.signum(Math.sin(angle)) );
														
				
				texture.setPixel(pos_x+x, pos_y+y, Color.red.getRGB());			
			}
		
			timePassed -= timestepInMillis;
		}

		lastTime = currentTime;
	}
	
	
	private float supershape(float angle) {
			
		double part1  = (1/a)*Math.cos(angle * m/4);
		part1 = Math.abs(part1);
		part1 = Math.pow(part1, n2);
		
		
		double part2  = (1/b)*Math.sin(angle * m/4);
		part2 = Math.abs(part2);
		part2 = Math.pow(part2, n3);
		
		double part3 = Math.pow( part1 + part2, 1/n1 );
						
		return (float)(1 / part3);
	}
	
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u,
			float texel_v, int screen_x, int screen_y) {

		float x = Math.min(textureWidth - 1, texel_u * (textureWidth));
		float y = Math.min(textureHeight - 1, texel_v * (textureHeight));

		int texel = texture.getPixel((int) x, (int) y);

		int red = (texel >> 16) & 0xff;
		int green = (texel >> 8) & 0xff;
		int blue = (texel) & 0xff;

//		return super.shade(255, red, green, blue, pcorr_world_x, pcorr_world_y, pcorr_world_z, normal_x, normal_y,
//				normal_z);

		 return ((renderFace.getColor().getAlpha() & 0xFF) << 24) |
		 ((red & 0xFF) << 16) |
		 ((green & 0xFF) << 8) |
		 ((blue & 0xFF) << 0);
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

}
