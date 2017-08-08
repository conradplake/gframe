package gframe.engine;

public class PhongShader extends AbstractShader {

	public PhongShader(Lightsource lightsource) {
		super(lightsource);		
	}

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
		// just return diffuse color from interpolated normal vector for now
		// TODO: add phong specular highlighting
		return super.shade(renderFace.col, world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

}
