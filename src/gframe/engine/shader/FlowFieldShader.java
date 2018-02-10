package gframe.engine.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import gframe.Space3D;
import gframe.engine.AbstractShader;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Particle;
import gframe.engine.Point3D;
import gframe.engine.RenderFace;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;
import gframe.engine.generator.NoiseGenerator;

/**
 *
 * */
public class FlowFieldShader extends AbstractShader {

	private long lastTimeInMillis = 0;
	private long timePassedInMillis = 0;

	private ImageRaster texture;
	private int textureWidth = 500;
	private int textureHeight = 200;

	private Space3D space3D;

	private Vector3D[] field; // the grid of force-applying cells

	private List<Particle> particles;

	// noise increments
	float xInc = 1f / textureWidth;
	float yInc = 1f / textureHeight;
	float zInc = 0.005f; // for slicing the noise cube along the z-axis over
							// time

	// zOff increments globally as it represents the time through noise field
	float zOff;

	boolean drawForceVectors = false;

	public FlowFieldShader(Lightsource lightsource, int numberOfParticles) {
		super(lightsource);

		texture = new ImageRaster(textureWidth, textureHeight);
		space3D = new Space3D();
		particles = new ArrayList<Particle>();
		for (int i = 0; i < numberOfParticles; i++) {
			Particle p = new Particle(
					new Point3D((float) (Math.random() * textureWidth), (float) (Math.random() * textureHeight), 0));
			// Particle p = new Particle(new Point3D(textureWidth/2,
			// textureHeight/2, 0));
			particles.add(p);
		}

		field = new Vector3D[textureWidth * textureHeight];
		for (int i = 0; i < field.length; i++) {
			// field[i] = new Vector3D((float)(-0.5+Math.random()),
			// (float)(-0.5+Math.random()), (float)(-0.5+Math.random()));
			field[i] = new Vector3D();
		}

		lastTimeInMillis = System.currentTimeMillis();
	}

	/**
	 * Sets particle positions based on specified height map.
	 */
	public void setParticlePositions(ImageRaster heightmap) {

		List<int[]> positions = new ArrayList<int[]>();

		// collect all possible locations (i.e. where pixels are not-black)
		for (int x = 0; x < heightmap.getWidth(); x++) {
			for (int y = 0; y < heightmap.getHeight(); y++) {
				int pixel = heightmap.getPixel(x, y);

				// int alpha = (pixel >> 24) & 0xff;
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;

				if (red + green + blue > 0) {
					int nx = (int) Toolbox.map(x, 0, heightmap.getWidth(), 0, textureWidth);
					int ny = textureHeight - (int) Toolbox.map(y, 0, heightmap.getHeight(), 0, textureHeight);
					positions.add(new int[] { nx, ny });
				}
			}
		}

		int numberOfParticles = Math.min(particles.size(), positions.size());

		for (int i = 0; i < numberOfParticles; i++) {
			Particle particle = particles.get(i);
			int posIndex = (int) (Math.random() * positions.size());
			int[] xy = positions.remove(posIndex);
			particle.getOrigin().x = xy[0];
			particle.getOrigin().y = xy[1];

			// stop particle movement
			particle.setVelocityVector(new Vector3D());
		}
	}

	private void edgeHandling(Particle p) {

		if (p.getOrigin().x < 0) {
			p.getOrigin().x = textureWidth - 1;
		} else if (p.getOrigin().x >= textureWidth) {
			p.getOrigin().x = 0;
		}

		if (p.getOrigin().y < 0) {
			p.getOrigin().y = textureHeight - 1;
		} else if (p.getOrigin().y >= textureHeight) {
			p.getOrigin().y = 0;
		}
	}

	@Override
	public void preShade(RenderFace renderFace) {

		super.preShade(renderFace);
		
		long currentTimeInMillis = System.currentTimeMillis();
		timePassedInMillis += (currentTimeInMillis - lastTimeInMillis);

		long timestepInMillis = 30;

		while (timePassedInMillis > timestepInMillis) {

			float secondsPassed = timePassedInMillis * 0.001f;
			timePassedInMillis = 0;

			// update flow field
			float xoff = 0;
			for (int x = 0; x < textureWidth; x++) {
				float yoff = 0;
				for (int y = 0; y < textureHeight; y++) {
					Vector3D forceVector = field[x + textureWidth * y];

					float noise = (float) NoiseGenerator.improvedPerlinNoise(xoff, yoff, zOff);
					float randomAngle = (float) Toolbox.map(noise, -0.5d, 0.5d, 0, Math.PI * 2);

					forceVector.x = 0;
					forceVector.y = 1f;
					forceVector.z = 0;
					Toolbox.getZrotMatrixFromRadiant(randomAngle).transform(forceVector);

					texture.setPixel(x, y, Color.black.getRGB());

					// draw force vector every 10 cells
					if (drawForceVectors && (x % 10 == 0 && y % 10 == 0)) {
						Color c = new Color(255, 0, 0, 100);
						Vector3D pos = new Vector3D(x, y, 0);
						pos.add(forceVector);
						pos.x = (float) Toolbox.clamp(pos.x, 0, textureWidth - 1);
						pos.y = (float) Toolbox.clamp(pos.y, 0, textureHeight - 1);
						int[] forceVectorX = new int[] { x, (int) (pos.x) };
						// int[] forceVectorX = new int[]{(int)pos.x, x};
						int[] forceVectorY = new int[] { y, (int) (pos.y) };
						// int[] forceVectorY = new int[]{(int)pos.y, y};
						Toolbox.drawPolygon(texture, forceVectorX, forceVectorY, c.getRGB());
					}
					yoff += yInc;
				}
				xoff += xInc;
			}
			zOff += zInc;

			// update particles
			for (Particle particle : particles) {

				edgeHandling(particle);

				int x = (int) particle.getOrigin().x;
				int y = (int) particle.getOrigin().y;

				Vector3D forceVector = field[x + textureWidth * y];
				space3D.simulate(particle, forceVector, secondsPassed);

				texture.setPixel(x, y, particle.getRgb());
				// CirclesShader.drawCircle(texture, new Circle(x, y, 0),
				// particle.getRgb());

				// trim speed; better idea: average velocity vector with current
				// force vector
				float speed = particle.getVelocityVector().length();
				if (speed >= 1) {
					particle.getVelocityVector().scale(1 / speed);
				}
			}
		}

		lastTimeInMillis = currentTimeInMillis;
	};

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y,
			float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {

		float x = Toolbox.clamp(texel_u * (textureWidth), 0, textureWidth - 1);
		float y = Toolbox.clamp(texel_v * (textureHeight), 0, textureHeight - 1);
		
		int texel = texture.getPixel((int) x, (int) y);

		return super.shade(texel, world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}

	@Override
	public boolean isPerPixelShader() {
		return true;
	}

}
