package gframe.engine;

/**
 * 	Gives a color result for every position in 3d space (see Methods shade).
 *  
 *  Instances of this class are used by the 3d-engine when rasterizing polygons (see RenderFace) to a color buffer.
 * 
 * */
public interface Shader {
	
	
	/**
	 * Called before rasterization to inform this shader about the next face in the render pipeline.
	 * */
	public void preShade(RenderFace renderFace);
	
	/**
	 * Returns a color as argb code for the specified face;
	 * */
	public int shade(RenderFace renderFace);	
	
		
	/**
	 * Returns a color as argb code for the specified face and position within the face;
	 * */
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y);


	public void setLightsource(Lightsource ls);
	public Lightsource getLightsource();

	public boolean isPerPixelShader();
	
}
