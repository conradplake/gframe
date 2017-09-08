package gframe.engine.shader;


import java.awt.Color;

import gframe.engine.AbstractShader;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import gframe.engine.Toolbox;


/**
 * See https://en.wikipedia.org/wiki/Mandelbrot_set
 * 
 * */
public class MandelbrotShader extends AbstractShader {

	long lastTime = 0;
	long timePassed = 0;
	
	int textureWidth = 640;
	int textureHeight = 480;
	
	ImageRaster texture;
		
	
	float minValue = -2;
	float maxValue =  2;
	
	// julia set constant
	float ca = -0.8f;
	float cb = 0.156f;

	public MandelbrotShader(Lightsource lightsource){
		super(lightsource);
		texture = new ImageRaster(textureWidth, textureHeight);
		lastTime = System.currentTimeMillis();
	
	}
	
	
	@Override
	public void preShade(RenderFace renderFace) {
		
		long currentTime = System.currentTimeMillis();

		timePassed += (currentTime - lastTime);

		while (timePassed > 40) {
			
//			minValue = 0.99f*minValue;
//			maxValue = 0.99f*maxValue;
			
			// generate next grid state
			for (int x = 0; x < textureWidth; x++) {
				for (int y = 0; y < textureHeight; y++) {
					
					// the complex number at x,y
					// map between -2,2 (use smaller numbers to zoom in)
					float a = (float)Toolbox.map(x, 0, textureWidth,  minValue, maxValue);
					float b = (float)Toolbox.map(y, 0, textureHeight, minValue, maxValue);
					
					// remember original values
					float xa = a;
					float xb = b;
					
					// do the mandelbrot diverge test for a, b
					int n = 0;
					int maxIterations = 100;
					for(;n<maxIterations;n++){
						
						// compute (a+bi)^2 
						float aa = a*a;
						float bb = b*b;
						float twoab = 2*a*b;
						
						// bound test
						if(Math.abs(aa+bb)>4){
							break;
						}
												
						a = aa-bb + xa;
						b = twoab + xb;
												
						// julia set
//						a = aa-bb + ca;
//						b = twoab + cb;
						
												
					}
					
					float color = (float)Toolbox.map(n, 0, maxIterations, 0, 1);
					color = (float)Toolbox.map(Math.sqrt(color), 0, 1, 0, 1);
					
					if(n==maxIterations){
						// bounded!
						color = 0; // make bounded positions black
					}
					
//					Color c = new Color(color, color, color);
					Color c = Color.getHSBColor(color, 1, 1);
					texture.setPixel(x, y, c.getRGB());	
				}
			}
						
			timePassed -= 40;
		}

		lastTime = currentTime;
	};

	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
				
				
		float x = Math.min(textureWidth-1, texel_u*(textureWidth));
		float y = Math.min(textureHeight-1, texel_v*(textureHeight));
		
		int texel = texture.getPixel((int)x, (int)y);		
				
		int red = (texel >> 16) & 0xff;
		int green = (texel >> 8) & 0xff;
		int blue = (texel) & 0xff;
		
		return  ((renderFace.getColor().getAlpha() & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8)  |
                ((blue & 0xFF) << 0);
	}
	

	
	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}


}
