package gframe.engine;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

public class TextureShader extends AbstractShader {

	ImageRaster texture;

	ImageRaster[] textureLODs;

	final int originalTextureWidth;
	final int originalTextureHeight;

	int textureWidth;
	int textureHeight;

	// texture normal coordinates
	float u;
	float v;
	
	boolean isBilinearFilteringEnabled = true;

	boolean isDithering = false;
	
	public static final double LOG_4_INV = 1d / Math.log10(4);

	/*
	 * Dither kernel as in Unreal's software renderer (a.k.a. poor man's
	 * bilinear)
	 * 
	 * (X&1)==0 (X&1==1) +--------+------------------------------ (Y&1)==0 |
	 * u+=.25,v+=.00 u+=.50,v+=.75 (Y&1)==1 | u+=.75,v+=.50 u+=.00,v+=.25
	 */

	static final float[][][] DITHER_KERNEL = new float[2][2][2];
	static {
		DITHER_KERNEL[0][0][0] = 0.25f;
		DITHER_KERNEL[0][0][1] = 0.00f;

		DITHER_KERNEL[0][1][0] = 0.75f;
		DITHER_KERNEL[0][1][1] = 0.50f;

		DITHER_KERNEL[1][0][0] = 0.50f;
		DITHER_KERNEL[1][0][1] = 0.75f;

		DITHER_KERNEL[1][1][0] = 0.00f;
		DITHER_KERNEL[1][1][1] = 0.25f;
	}

	public TextureShader(Lightsource lightsource) {
		this(lightsource, new File("textures/chessboard.jpg"), 256, 256); // load
																			// default
																			// test
																			// image
	}

	public TextureShader(Lightsource lightsource, File textureFile, int w, int h) {
		this(lightsource, getRGBRaster(textureFile, w, h));
	}

	public TextureShader(Lightsource lightsource, ImageRaster texture) {
		super(lightsource);
		this.textureWidth = texture.getWidth();
		this.textureHeight = texture.getHeight();
		this.texture = texture;

		this.originalTextureWidth = this.textureWidth;
		this.originalTextureHeight = this.textureHeight;
		
		textureLODs = mipmaps(texture);		
	}

	void setEffectPixel(int x, int y, int c) {
		texture.setPixel(x, y, c);
	}

	public void addEffect(ImageRaster heightmap, int threshold, Color c) {

		int rgb = c.getRGB();

		int hw = heightmap.getWidth() - 1;

		for (int x = 0; x < heightmap.getWidth(); x++) {
			for (int y = 0; y < heightmap.getHeight(); y++) {
				int pixel = heightmap.getPixel(x, y);
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;

				if (red + green + blue > threshold) {
					setEffectPixel(hw - x, y, rgb);
				}
			}
		}

		// recompute LOD textures
		recomputeMipmaps();
	}

	void recomputeMipmaps() {
		this.textureLODs = mipmaps(textureLODs[0]);
	}

	public void setIsBilinearFiltering(boolean value) {
		this.isBilinearFilteringEnabled = value;
	}

	public void setIsDithering(boolean value) {
		this.isDithering = value;
	}

	public int getTexelFast(float x, float y) {
		return texture.getPixel((int) x, (int) y);
	}

	public int getTexel(float x, float y) {

		int pixel;

		if (!isBilinearFilteringEnabled) {
			// int x_int = (int)Math.floor(x + 0.5f);
			// int y_int = (int)Math.floor(y + 0.5f);
			int x_int = (int) x;
			int y_int = (int) y;
			pixel = texture.getPixel(x_int, y_int);
		} else {
			int x_int = (int) x;
			int y_int = (int) y;
			float x_fract = x - x_int;
			float y_fract = y - y_int;

			pixel = texture.getPixel(x_int, y_int);
			int c0_alpha = (pixel >> 24) & 0xff;
			int c0_red = (pixel >> 16) & 0xff;
			int c0_green = (pixel >> 8) & 0xff;
			int c0_blue = (pixel >> 0) & 0xff;

			int c1 = x_int + 1 < textureWidth ? texture.getPixel(x_int + 1, y_int) : pixel;
			int c1_alpha = (c1 >> 24) & 0xff;
			int c1_red = (c1 >> 16) & 0xff;
			int c1_green = (c1 >> 8) & 0xff;
			int c1_blue = (c1 >> 0) & 0xff;

			int c2 = y_int + 1 < textureHeight ? texture.getPixel(x_int, y_int + 1) : pixel;
			int c2_alpha = (c2 >> 24) & 0xff;
			int c2_red = (c2 >> 16) & 0xff;
			int c2_green = (c2 >> 8) & 0xff;
			int c2_blue = (c2 >> 0) & 0xff;

			int c3 = x_int + 1 < textureWidth && y_int + 1 < textureHeight ? texture.getPixel(x_int + 1, y_int + 1)
					: pixel;
			int c3_alpha = (c3 >> 24) & 0xff;
			int c3_red = (c3 >> 16) & 0xff;
			int c3_green = (c3 >> 8) & 0xff;
			int c3_blue = (c3 >> 0) & 0xff;

			float c0_weight = (1 - x_fract) * (1 - y_fract);
			float c1_weight = x_fract * (1 - y_fract);
			float c2_weight = (1 - x_fract) * y_fract;
			float c3_weight = x_fract * y_fract;

			int newAlpha = (int) (c0_alpha * c0_weight + c1_alpha * c1_weight + c2_alpha * c2_weight
					+ c3_alpha * c3_weight);

			int newRed = (int) (c0_red * c0_weight + c1_red * c1_weight + c2_red * c2_weight + c3_red * c3_weight);

			int newGreen = (int) (c0_green * c0_weight + c1_green * c1_weight + c2_green * c2_weight
					+ c3_green * c3_weight);

			int newBlue = (int) (c0_blue * c0_weight + c1_blue * c1_weight + c2_blue * c2_weight + c3_blue * c3_weight);

			pixel = ((newAlpha & 0xFF) << 24) | ((newRed & 0xFF) << 16) | ((newGreen & 0xFF) << 8)
					| ((newBlue & 0xFF) << 0);

		}

		return pixel;
	}

	@Override
	public void preShade(RenderFace renderFace) {
		super.preShade(renderFace);
		adjustLOD(renderFace);
	}

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		// dither delta
		float du = 0;
		float dv = 0;

		if (isDithering) {
			int x_index = screen_x & 1;
			int y_index = screen_y & 1;
			du = DITHER_KERNEL[x_index][y_index][0];
			dv = DITHER_KERNEL[x_index][y_index][1];
		}
				
		u = Toolbox.clamp(texel_u * textureWidth + du, 0, textureWidth - 1);
		v = Toolbox.clamp(texel_v * textureHeight + dv, 0, textureHeight - 1);
		return super.shade(getTexel(u, v), world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}

	public static ImageRaster getRGBRaster(File imagefile, int w, int h) {

		if (!imagefile.canRead()) {
			throw new RuntimeException("Cannot read from file: " + imagefile.getAbsolutePath());
		}

		Toolkit tk = Toolkit.getDefaultToolkit();
		Image img = null;
		try {
			img = tk.getImage(imagefile.toURI().toURL());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return Toolbox.getImageRaster(img, 0, 0, w, h);
	}


	public static ImageRaster getRGBRaster(Color c, int w, int h) {
		ImageRaster raster = new ImageRaster(w, h);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				raster.setPixel(x, y, c.getRGB(), c.getAlpha());
			}
		}
		return raster;
	}

	public boolean isBilinearFilteringEnabled() {
		return isBilinearFilteringEnabled;
	}

	public boolean isDitheringEnabled() {
		return isDithering;
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

	public void adjustLOD(RenderFace renderFace) {

		int lod = getLOD(renderFace);

		ImageRaster lodTexture = textureLODs[lod];

		this.texture = lodTexture;
		this.textureWidth = lodTexture.getWidth();
		this.textureHeight = lodTexture.getHeight();
	}

	/**
	 * Returns the best level of detail for texturing the specified face.
	 * 0 - highest level (original texture)
	 * 
	 */
	public int getLOD(RenderFace renderFace) {
		
		float areaScreen = 0;
		float areaTexture = 0;
		boolean computeTextureArea = renderFace.getTextureArea() == null;		
		

		// Shoe lace formula
		for (int i = 0; i < renderFace.vertices.length; i++) {

			int next = i + 1;
			if (next == renderFace.vertices.length)
				next = 0;

			float x_i0 = renderFace.cam_X[i];
			float y_i0 = renderFace.cam_Y[i];
			float x_i1 = renderFace.cam_X[next];
			float y_i1 = renderFace.cam_Y[next];

			areaScreen += ((x_i0 * y_i1) - (y_i0 * x_i1));
			
			if(computeTextureArea){
				float u_i0 = renderFace.vertices[i].u * originalTextureWidth;
				float v_i0 = renderFace.vertices[i].v * originalTextureHeight;
				float u_i1 = renderFace.vertices[next].u * originalTextureWidth;
				float v_i1 = renderFace.vertices[next].v * originalTextureHeight;
				areaTexture += ((u_i0 * v_i1) - (v_i0 * u_i1));
			}
		}

		areaScreen = 0.5f * Math.abs(areaScreen);
		if(computeTextureArea){
			areaTexture = 0.5f * Math.abs(areaTexture);	
			renderFace.setTextureArea(areaTexture);
		}else{
			areaTexture = renderFace.getTextureArea();
		}
		

		float mmFactor = areaTexture / areaScreen;
		
		
		int lod = 0;
		if(mmFactor>1){
			lod = (int) (Math.log10(mmFactor) * LOG_4_INV);
		}	
		if(lod>=textureLODs.length){
			lod = textureLODs.length-1;
		}

//		System.out.println("TextureShader.getLOD(): LOD="+lod);

		return lod;
	}

	
	public static ImageRaster[] mipmaps(ImageRaster texture){
		int min = Math.min(texture.getWidth(), texture.getHeight());
		int lods = 1 + (int)(Math.log(min) / Math.log(2));
		
		ImageRaster[] result = new ImageRaster[lods];
		ImageRaster tmp = texture;
		
		result[0] = tmp;
		for(int i=1;i<lods;i++){
			tmp = mipmap(tmp);
			result[i] = tmp;
		}
		
		return result;		
	}

	/**
	 * Returns a new texture scaled down by half the width and height of the
	 * original texture. Each new pixel thus represents 4 original pixels by
	 * averaging their values.
	 */
	public static ImageRaster mipmap(ImageRaster texture) {
		ImageRaster mipmapped = new ImageRaster(texture.getWidth() / 2, texture.getHeight() / 2);

		for (int x = 0; x < mipmapped.getWidth(); x++) {
			for (int y = 0; y < mipmapped.getHeight(); y++) {

				// pixel = avg über alle 4 nachbarn im original
				int pixel_a = texture.getPixel(x * 2, y * 2);
				int pixel_b = texture.getPixel(x * 2 + 1, y * 2);
				int pixel_c = texture.getPixel(x * 2, y * 2 + 1);
				int pixel_d = texture.getPixel(x * 2 + 1, y * 2 + 1);

				int a_alpha = (pixel_a >> 24) & 0xff;
				int b_alpha = (pixel_b >> 24) & 0xff;
				int c_alpha = (pixel_c >> 24) & 0xff;
				int d_alpha = (pixel_d >> 24) & 0xff;
				int new_alpha = (a_alpha + b_alpha + c_alpha + d_alpha) / 4;

				int a_red = (pixel_a >> 16) & 0xff;
				int b_red = (pixel_b >> 16) & 0xff;
				int c_red = (pixel_c >> 16) & 0xff;
				int d_red = (pixel_d >> 16) & 0xff;
				int new_red = (a_red + b_red + c_red + d_red) / 4;

				int a_green = (pixel_a >> 8) & 0xff;
				int b_green = (pixel_b >> 8) & 0xff;
				int c_green = (pixel_c >> 8) & 0xff;
				int d_green = (pixel_d >> 8) & 0xff;
				int new_green = (a_green + b_green + c_green + d_green) / 4;

				int a_blue = (pixel_a >> 0) & 0xff;
				int b_blue = (pixel_b >> 0) & 0xff;
				int c_blue = (pixel_c >> 0) & 0xff;
				int d_blue = (pixel_d >> 0) & 0xff;
				int new_blue = (a_blue + b_blue + c_blue + d_blue) / 4;

				int pixel = ((new_alpha & 0xFF) << 24) | ((new_red & 0xFF) << 16) | ((new_green & 0xFF) << 8)
						| ((new_blue & 0xFF) << 0);

				mipmapped.setPixel(x, y, pixel);
			}
		}

		return mipmapped;
	}
	
	public static void copySpecularMapToAlphaChannel(ImageRaster specularMap, ImageRaster target) {
		// specular map als alpha-channel in die normal map kopieren
		for (int x = 0; x < specularMap.getWidth(); x++) {
			for (int y = 0; y < specularMap.getHeight(); y++) {
				int specRgb = specularMap.getPixel(x, y);
				int grayValue = Toolbox.toGray(specRgb);
				target.setAlpha(x, y, grayValue);
			}
		}
	}
}
