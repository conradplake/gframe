package gframe.engine;

import gframe.ImageRaster;

public interface Rasterizer {

	
	public void rasterize(RenderFace renderFace, ImageRaster colorBuffer, ZBuffer zBuffer, Shader shader);
		
	public boolean isOutsideScreen(RenderFace renderFace);
		
	public ImageRaster createEmptyImageRaster();
	
	public ZBuffer createZBuffer();
	
	public void setScreenSize(int xoffset, int yoffset, int frameX, int frameY);
	
	public int getXOffset();
	
	public int getYOffset();

}