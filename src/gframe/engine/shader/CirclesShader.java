package gframe.engine.shader;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import imaging.ImageRaster;


/**
 * Uses a static texture of non-overlapping circles of varying sizes which looks nice ;)
 * */
public class CirclesShader extends AbstractShader {
	
	int textureWidth = 1024;
	int textureHeight = 1024;
	
	ImageRaster texture;
			
	List<Circle> circles;
	
	Color circleColor = new Color(255, 0, 150, 100);
	
	public CirclesShader(Lightsource lightsource){
		super(lightsource);
		texture = new ImageRaster(textureWidth, textureHeight);
		
		// preset color to white
		for (int x = 0; x < textureWidth; x++) {
			for (int y = 0; y < textureHeight; y++) {
				texture.setPixel(x, y, Color.WHITE.getRGB());
			}
		}
		
		this.circles = new ArrayList<Circle>();
		while(circles.size()<600){
			
			float x = (float)(Math.random()*textureWidth);
			float y = (float)(Math.random()*textureHeight);
			
			Circle c = new Circle(x, y, 10+(float)(Math.random()*50));
			boolean overlapping = false;
			
			for(int j=0;j<circles.size();j++){
				Circle other = circles.get(j);
				if(other.dist(c) < c.radius+other.radius){
					overlapping = true;
					break;
				}
			}
			
			if(!overlapping)
				circles.add(c);			
		}
		
		// draw all the circles to the texture		
		for(Circle circle : circles){
			drawCircle(texture, circle, circleColor.getRGB());				
		}
	}
	

	
	/**
	 * check all pxiels inside bbox: 
	 * if dist to circle origin is less than radius 
	 * than fill pixel with color c
	 * */
	public static void drawCircle(ImageRaster raster, Circle circle, int rgb){
		
		final float r = circle.radius;
		final float d = 2*r;
			
		
		int topleft_x = (int)(circle.x-r);
		int topleft_y = (int)(circle.y-r);			
				
		
		
		for (int x=topleft_x;x<topleft_x+d;x++){
			
			if(x<0){
				continue;
			}
			if(x>=raster.getWidth()){
				break;
			}
			
			
			for (int y=topleft_y;y<topleft_y+d;y++){
				
				if(y<0){
					continue;
				}
				if(y>=raster.getHeight()){
					break;
				}
				
				if(circle.isInside(x, y)){
					raster.setPixel(x, y, rgb);
				}
			}	
		}				
	}
	
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
				
				
		float x = Math.min(textureWidth-1, texel_u*(textureWidth));
		float y = Math.min(textureHeight-1, texel_v*(textureHeight));
		
		int texel = texture.getPixel((int)x, (int)y);		

		return super.shade(texel, world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}
	

	
	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}

	
	public static class Circle{
		Circle(float x, float y, float r){
			this.x = x;
			this.y = y;
			this.radius = r;
		}
		
		public float dist(Circle c) {
			float dx = c.x - this.x;
			float dy = c.y - this.y;		
			float dist = (float)( Math.sqrt(dx*dx + dy*dy) );
			return dist;
		}
		
		public boolean isInside(float otherX, float otherY) {
			float dx = otherX - this.x;
			float dy = otherY - this.y;		
			float dist = (float)( Math.sqrt(dx*dx + dy*dy) );
			return dist <= radius;
		}
		
		float x, y, radius;		
	}

}
