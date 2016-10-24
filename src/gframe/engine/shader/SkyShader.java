package gframe.engine.shader;

import java.awt.Color;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.RenderFace;
import gframe.engine.Toolbox;

public class SkyShader extends AbstractShader {
		
	private float iMaxY;
	
	private int horizontal_red;
	private int horizontal_green;
	private int horizontal_blue;
	
	private int zenith_red;
	private int zenith_green;
	private int zenith_blue;
	
	public SkyShader(Lightsource lightsource, Model3D skydome) {		
		this(lightsource, skydome, new Color(135, 206, 250), new Color(0, 0, 140)); // default sky: lightblue -> darkblue 
	}
	
	public SkyShader(Lightsource lightsource, Model3D skydome, Color horizonColor, Color zenithColor) {
		super(lightsource);
		float[] skydomebbox = skydome.getBBox();		
		this.iMaxY = 1f/skydomebbox[3];
		
		this.horizontal_red = horizonColor.getRed();
		this.horizontal_green = horizonColor.getGreen();
		this.horizontal_blue = horizonColor.getBlue();
		
		this.zenith_red = zenithColor.getRed();
		this.zenith_green = zenithColor.getGreen();
		this.zenith_blue = zenithColor.getBlue();
	}
	
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float pcorr_world_x, float pcorr_world_y, float pcorr_world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
		
		// anhand von world_y die farbe bestimmen via interpolation zwischen horizont color (y=0) und zenith-color (y=skydome.maxY)		
						
		float relativeHeight = world_y * iMaxY;		
		
		int r = Toolbox.lerp(horizontal_red, zenith_red, relativeHeight);
		int g = Toolbox.lerp(horizontal_green, zenith_green, relativeHeight);
		int b = Toolbox.lerp(horizontal_blue, zenith_blue, relativeHeight);			
		
		return  ((255 & 0xFF) << 24) |
				((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

}
