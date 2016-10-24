package gframe.engine;

public class FlatShader extends AbstractShader {
	
	public FlatShader(Lightsource lightsource){
		super(lightsource);
	}

	
	@Override
	public int shade(RenderFace renderFace) {		
		return super.shade(renderFace.col, renderFace.centroid.x, renderFace.centroid.y, renderFace.centroid.z, renderFace.normal_x, renderFace.normal_y, renderFace.normal_z);
	}


	@Override
	public int shade(RenderFace renderFace, float w_x, float w_y, float w_z, float pcorr_world_x, float pcorr_world_y, float pcorr_world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
		return this.shade(renderFace);
	}


	@Override
	public boolean isPerPixelShader() {
		return false;
	}

}
