package gframe.engine;

import java.awt.Color;


/**
 * Implements OIS3D to apply physics (see Space3D.simulate)
 * */
public class Particle extends Model3D implements OIS3D{
	
	private Vector3D velocity;
	
	private int rgb;
			

	public Particle(Point3D origin){
		this(origin, new Vector3D());
	}

	public Particle(Point3D origin, Vector3D velocity){
		super(origin);
		this.velocity = velocity;		
		this.rgb = Color.HSBtoRGB((float)Math.random(), 1, 1f);
	}
	

	@Override
	public Vector3D getVelocityVector() {
		return velocity;
	}
	
	
	public void setVelocityVector(Vector3D velocity){
		this.velocity = velocity;
	}

	public int getRgb() {
		return rgb;
	}

	public void setRgb(int rgb) {
		this.rgb = rgb;
	}
	
	public void setAlpha(int alpha){
		this.rgb =  ((alpha & 0xFF) << 24) | rgb;
	}
}
