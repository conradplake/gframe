package gframe.engine.shader;

import java.awt.Color;

import gframe.engine.AbstractShader;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import gframe.engine.shader.CirclesShader.Circle;

public class PhyllotaxisShader extends AbstractShader {

	long lastTime = 0;
	long timePassed = 0;

	int textureWidth = 300;
	int textureHeight = 300;

	ImageRaster texture;

	int n = 0;
	int c = 2;

	public PhyllotaxisShader(Lightsource lightsource) {
		super(lightsource);
		texture = new ImageRaster(textureWidth, textureHeight);
		setBackground(Color.white);
		lastTime = System.currentTimeMillis();
	}

	private void setBackground(Color c) {
		int rgb = c.getRGB();
		for (int x = 0; x < textureWidth; x++) {
			for (int y = 0; y < textureHeight; y++) {
				texture.setPixel(x, y, rgb);
			}
		}
	}

	@Override
	public void preShade(RenderFace renderFace) {

		super.preShade(renderFace);
		
		long currentTime = System.currentTimeMillis();

		timePassed += (currentTime - lastTime);

		while (timePassed > 20) {

			float angle = n * 137.3f;
			// float angle = n * 1;
			float r = (float) (c * Math.sqrt(n));

			float x = r * (float) Math.cos(Math.toRadians(angle)) + textureWidth / 2;
			float y = r * (float) Math.sin(Math.toRadians(angle)) + textureHeight / 2;

			if (x >= 0 && x < textureWidth && y >= 0 && y < textureHeight) {

				float color = angle % 360 / 360;
				Color c = Color.getHSBColor(color, 1, 1);
				int rgb = c.getRGB();
				// texture.setPixel((int)x, (int)y, rgb);
				CirclesShader.drawCircle(texture, new Circle(x, y, 4), rgb);
			}
			n++;

			timePassed -= 20;
		}

		// timeFactor = (float) Math.sin(currentTime-lastTime)/16f;
		// timeFactor = (float) Math.sin(counter);
		// timeFactor = currentTime - lastTime;;

		lastTime = currentTime;
	};

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		float x = Math.min(textureWidth - 1, texel_u * (textureWidth));
		float y = Math.min(textureHeight - 1, texel_v * (textureHeight));

		int texel = texture.getPixel((int) x, (int) y);

		return super.shade(texel, world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

}
