package gframe.engine.shader;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;

public class MetaballShader extends AbstractShader {

	long lastTime = 0;
	long timePassed = 0;
	
	int textureWidth = 50;
	int textureHeight = 50;
	
	List<Blob> blobs;

	public MetaballShader(Lightsource lightsource, int numberOfBlobs){
		super(lightsource);
		lastTime = System.currentTimeMillis();
		this.blobs = new ArrayList<Blob>(numberOfBlobs);
		for(int i=0;i<numberOfBlobs;i++){
			blobs.add(new Blob((float)(Math.random()*textureWidth), (float)(Math.random()*textureHeight)));
		}
			
	}
	
	@Override
	public void preShade(RenderFace renderFace) {
		
	  long currentTime = System.currentTimeMillis();
	  
	  timePassed += (currentTime - lastTime);
	  	  	
	  while(timePassed>20){
		  
		  for(Blob b : blobs){			  
			b.update();
			
			// edge detection
			if(b.pos_x<0 || b.pos_x>textureWidth){
				b.vel_x *= -1;
			}
			if(b.pos_y<0 || b.pos_y>textureHeight){
				b.vel_y *= -1;
			}
			
		  }
		  
		  timePassed -= 20;
	  }
	  
	  lastTime = currentTime;
	};
	
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
				
				
		float x = Math.min(textureWidth-1, texel_u*(textureWidth));
		float y = Math.min(textureHeight-1, texel_v*(textureHeight));
		
		float sum = 0;
		for(Blob blob : blobs){			
			float dx = blob.pos_x-x;
			float dy = blob.pos_y-y;
//			float dist = (float)Math.sqrt(dx*dx + dy*dy);	
			float dist = (dx*dx + dy*dy);
//			float dist = Math.max(dx*dx , dy*dy);
			sum += blob.radius / dist;
		}
		
		float color = sum;				
		if(color>1)
			color = 1;
		
		Color c= Color.getHSBColor(color, 1, 1);
//		Color c = new Color(color, color, color);
		
		
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		return super.shade(renderFace.getColor().getAlpha(), r, g, b, world_x, world_y, world_z, normal_x, normal_y, normal_z);
		
//		return  ((renderFace.getColor().getAlpha() & 0xFF) << 24) |
//                ((c.getRed() & 0xFF) << 16) |
//                ((c.getGreen() & 0xFF) << 8)  |
//                ((c.getBlue() & 0xFF) << 0);
	}
	

	
	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}
	
	
	
	/**
	 * Basically just a point moving around
	 * 
	 **/
	public class Blob{
		
		public Blob(float x, float y) {
			this.pos_x = x;
			this.pos_y = y;
			
			this.vel_x = (float)Math.random();
			this.vel_y = (float)Math.random();
			
			this.radius = (float)Math.random() * 30;
		}
		
		
		public void update(){
			pos_x += vel_x;
			pos_y += vel_y;
		}

		
		float pos_x;
		float pos_y;
		float radius;
		
		float vel_x;
		float vel_y;					
	}

}
