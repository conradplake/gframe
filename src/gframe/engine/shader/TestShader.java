package gframe.engine.shader;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import gframe.engine.generator.NoiseGenerator;

public class TestShader extends AbstractShader {

	
	long counter;
		
	long lastTime = 0;
	long timePassed = 0;
	
	float timeFactor = 0;

	public TestShader(Lightsource lightsource){
		super(lightsource);
		lastTime = System.currentTimeMillis();
	}
	
	@Override
	public void preShade(RenderFace renderFace) {
		
	  long currentTime = System.currentTimeMillis();
	  
	  timePassed += (currentTime - lastTime);
	  	  	
	  while(timePassed>50){
		  counter++;
		  
		  timeFactor += 0.02;		  
		  
		  timePassed -= 50;
	  }
	  
	  
	  //timeFactor = (float) Math.sin(currentTime-lastTime)/16f;
//	  timeFactor = (float) Math.sin(counter);
//	  timeFactor = currentTime - lastTime;;
	  
	  
	  lastTime = currentTime;
	};
	
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float pcorr_world_x, float pcorr_world_y, float pcorr_world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
				
				
//		int[] woodColor = TextureGenerator.generateWoodColor(texel_u*3, texel_v*4, 0);							
//		int r = woodColor[0];
//		int g = woodColor[1];
//		int b = woodColor[2];
		

//		float x = texel_u;
//		float y = texel_v;			
		float x = pcorr_world_x;
		float y = pcorr_world_y;
		float z = pcorr_world_z;
		
		/*
		 * f(x, y) = (1 + sin( (x + noise(x * 5 , y * 5 ) / 2 ) * 50) ) / 2
		 * */		
		float noise = (float)NoiseGenerator.improvedPerlinNoise(x*0.3, y*0.1, z*0.3); 
//		float noise = (float)NoiseGenerator.improvedPerlinNoise(texel_u*1, texel_v*1, texel_u*texel_v*1);
//		float noise = 1;		
//		float gray = (1 + (float)Math.sin( ((x + noise) / 2f)*10)) / 2f;
//		float gray = (1 + (float)Math.sin( ((x + noise) / 2f)*(50+timeFactor))) / 2f;
		
//		float gray = x*y;
//		float gray = (float)NoiseGenerator.improvedPerlinNoise(x*10, 0, 0);
		
//		float gray = (float)Math.sin(Math.abs(noise*1));
		float gray = ( (float)Math.sin(noise+y+timeFactor*2) );
//		Math.si
		
//		float gray = 
//		(	
//			 ( 1f*(float)Math.sin( 1f* x+timeFactor+noise*1) ) 
////			+ Math.abs( 1f*(float)Math.sin( 1* y+timeFactor+noise*0.5) )
////			+ Math.abs( 1f*(float)Math.sin( 1* z+timeFactor+noise*0.5) )
////			+ 1*(float)Math.sin( 0.5* y+timeFactor+noise*1)
////			+ 1*(float)Math.sin( 0.5* z+timeFactor+noise*1)
////			+ (texel_u/texel_v)
//		)
//		/1f;
		
		
		
//		float gray = (float)Math.sin(-world_x+timeFactor*5+noise*2);
		
//		gray = Math.abs(gray);
		
		
//		int r = (int)(gray * 127 + 40);
		int r = 10;
//		int g = (int)(gray * 127 + 40);
		int g = 120;
		int b = (int)(gray * 127 + 90);
//		int r = (int)(gray * 255);
//		int g = (int)(gray * 255);
//		int b = (int)(gray * 255);
		
		
//		float noise_r = (float)NoiseGenerator.improvedPerlinNoise(texel_u*8, texel_v*8, 0);		
//		float noise_g = (float)NoiseGenerator.improvedPerlinNoise(0, texel_u*8, texel_v*8);
//		float noise_b = (float)NoiseGenerator.improvedPerlinNoise(texel_u*8, 0, texel_v*8);
//		int r = (int)(noise_r * 127 + 128);
//		int g = (int)(noise_g * 127 + 128);
//		int b = (int)(noise_b * 127 + 128);
		
		
		
		
//		return super.shade(renderFace.getColor().getAlpha(), r, g, b, world_x, world_y, world_z, normal_x, normal_y, normal_z);
		
		return  ((renderFace.getColor().getAlpha() & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
	}
	

	
	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}

}
