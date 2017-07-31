package gframe.engine.shader;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import gframe.engine.generator.NoiseGenerator;

public class MarbleShader extends AbstractShader {

	
	long counter;
		
	long lastTime = 0;
	long timePassed = 0;
	
	float timeFactor = 0;

	public MarbleShader(Lightsource lightsource){
		super(lightsource);
		lastTime = System.currentTimeMillis();
	}
	
	@Override
	public void preShade(RenderFace renderFace) {
		
	  long currentTime = System.currentTimeMillis();
	  
	  timePassed += (currentTime - lastTime);
	  	  	
	  while(timePassed>50){
		  counter++;
		  
//		  timeFactor += 0.1;		  
		  
		  timePassed -= 50;
	  }
	  
	  
	  //timeFactor = (float) Math.sin(currentTime-lastTime)/16f;
//	  timeFactor = (float) Math.sin(counter);
//	  timeFactor = currentTime - lastTime;;
	  
	  
	  lastTime = currentTime;
	};
	
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
					
		float x = world_x;
		float y = world_y;
		float z = world_z;
			
		float noise = (float)NoiseGenerator.improvedPerlinNoise(x*1, y*0.5, z*0.5); 
		
		float gray = 
		(	
			  Math.abs( 1f*(float)Math.sin( 0.5f* x+timeFactor+noise*0.5) ) 
		);			
		
		
		int r = (int)(gray * 127 + 128);
		int g = (int)(gray * 127 + 90);
		int b = (int)(gray * 127 + 120);
	
				
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
