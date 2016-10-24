package gframe.engine.shader;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import gframe.engine.generator.NoiseGenerator;

public class WaterShader extends AbstractShader {

	
	long counter;
		
	long lastTime = 0;
	long timePassed = 0;
	
	float timeFactor = 0;

	public WaterShader(Lightsource lightsource){
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
		
		float noise = (float)NoiseGenerator.improvedPerlinNoise(pcorr_world_x*0.001, pcorr_world_y*0.001, pcorr_world_z*0.001); 
		
		float gray = 
		(	  Math.abs( 1*(float)Math.sin( 0.001* pcorr_world_x+timeFactor+noise*1) ) 
//			+ 1*(float)Math.sin( 100000* y+timeFactor+noise*10)
//			+ 1*(float)Math.sin( 1* z+timeFactor+noise*10)
		)
		;
		
		
		int r = 0;
		int g = 0;
		int b = (int)(gray * 127 + 128);
	
				
//		return super.shade(renderFace.col.getAlpha(), r, g, b, world_x, world_y, world_z, normal_x, normal_y, normal_z);
		
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
