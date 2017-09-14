package gframe.engine;

public class FlatShader extends AbstractShader {
	
	private int currentColor;
	
	public FlatShader(Lightsource lightsource){
		super(lightsource);
	}

	
	@Override
	public void preShade(RenderFace renderFace){
		super.preShade(renderFace);
		this.currentColor = renderFace.col.getRGB(); 
	}
	
	
	@Override
	public int shade(RenderFace renderFace) {		
		return super.shade(currentColor, renderFace.centroid.x, renderFace.centroid.y, renderFace.centroid.z, renderFace.normal_x, renderFace.normal_y, renderFace.normal_z);
	}


	@Override
	public int shade(RenderFace renderFace, float w_x, float w_y, float w_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
		return this.shade(renderFace);
	}


	@Override
	public boolean isPerPixelShader() {
		return false;
	}

}
