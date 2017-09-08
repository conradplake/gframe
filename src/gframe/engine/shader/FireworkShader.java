package gframe.engine.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
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

public class FireworkShader extends AbstractShader {

	long lastTimeInMillis = 0;
	long timePassedInMillis = 0;

	ImageRaster texture;

	int textureWidth = 300;
	int textureHeight = 300;

	Space3D space3D; // apply gravity to particles

	Collection<Firework> fireworks;

	public FireworkShader(Lightsource lightsource) {
		super(lightsource);

		texture = new ImageRaster(textureWidth, textureHeight);

		space3D = new Space3D(Space3D.EARTH_G);

		fireworks = new ArrayList<Firework>();
		fireworks.add(new Firework(new Point3D(textureWidth / 2, 0, 0)));

		lastTimeInMillis = System.currentTimeMillis();
	}

	
	private void drawParticle(Particle particle){		
		if (particle.getOrigin().y < 0) {		
			return;
		} else if (particle.getOrigin().y > textureHeight - 1) {
			return;
		}
		if (particle.getOrigin().x < 0) {
			return;
		} else if (particle.getOrigin().x > textureWidth - 1) {		
			return;
		}
		texture.setPixel((int) particle.getOrigin().x, (int) particle.getOrigin().y, particle.getRgb());
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

		long currentTimeInMillis = System.currentTimeMillis();
		timePassedInMillis += (currentTimeInMillis - lastTimeInMillis);

		long timestepInMillis = 10;

		while (timePassedInMillis > timestepInMillis) {

			float secondsPassed = timestepInMillis * 0.001f;
			
			setBackground(Color.black);

			for (Firework firework : fireworks) {
				
				if(firework == null || firework.done()){
					// remove from list
					continue;
				}
											
				if(!firework.exploded()){
					space3D.simulate(firework, secondsPassed);
					drawParticle(firework);					
					if(firework.getVelocityVector().y < 0){
						firework.explode();
					}
				}
											
				for (Particle particle : firework.getParticles()) {
					space3D.simulate(particle, secondsPassed);
					drawParticle(particle);
				}
			}

			timePassedInMillis -= timestepInMillis;
		}

		lastTimeInMillis = currentTimeInMillis;
	};
		
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u,
			float texel_v, int screen_x, int screen_y) {

		float x = Math.min(textureWidth - 1, texel_u * (textureWidth));
		float y = Math.min(textureHeight - 1, texel_v * (textureHeight));

		int texel = texture.getPixel((int) x, (int) y);

		int red = (texel >> 16) & 0xff;
		int green = (texel >> 8) & 0xff;
		int blue = (texel) & 0xff;

		return ((renderFace.getColor().getAlpha() & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8)
				| ((blue & 0xFF) << 0);

		// return super.shade(renderFace.getColor().getAlpha(), r, g, b,
		// world_x, world_y, world_z, normal_x, normal_y, normal_z);
	}

	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}

	public class Firework extends Particle {

		private List<Particle> particles;
		private boolean exploded = false;
		
		public Firework(Point3D origin) {
			super(origin);
			this.particles = new ArrayList<Particle>();
			this.setVelocityVector(new Vector3D(0, 6, 0)); // shoot upwards
		}
		
		
		public boolean done(){
			return false;
		}
		
		
		public boolean exploded(){
			return exploded;
		}
		
		public List<Particle> getParticles() {
			return this.particles;
		}

		public Particle addParticle(float x, float y) {
			return addParticle(new Point3D(x, y, 0));			
		}
				
		public Particle addParticle(Point3D origin) {
			Particle p = new Particle(origin);
			this.particles.add(p);
			return p;
		}

		public void explode() {
			exploded = true;
			for (int i = 0; i < 100; i++) {
				
				Point3D particlePos = getOrigin().copy();
				
				// add random displacement
				float randomAngle = (float)Toolbox.map(Math.random(), 0, 1, 0, Math.PI*2);
				Point3D displacement = new Point3D(0, (float)Math.random()*50, 0);				
				Toolbox.getZrotMatrixFromRadiant(randomAngle).transform(displacement);									
				particlePos.add(displacement);
				
				addParticle(particlePos);				
			}
		}
	}

}
