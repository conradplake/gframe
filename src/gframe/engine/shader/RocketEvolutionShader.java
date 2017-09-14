package gframe.engine.shader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eve.ga.Gene;
import eve.ga.GeneEvaluater;
import eve.ga.Generation;
import eve.ga.PopulationListener;
import eve.ga.PopulationManager;
import eve.ga.RandomMutationOnePntCrossOver;
import gframe.OIS3D;
import gframe.Space3D;
import gframe.engine.AbstractShader;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Object3D;
import gframe.engine.Point3D;
import gframe.engine.RenderFace;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;
import gframe.engine.shader.CirclesShader.Circle;


/**
 * 
 * see also: http://www.blprnt.com/smartrockets/
 * */
public class RocketEvolutionShader extends AbstractShader {

	private long lastTimeInMillis = 0;
	private long timePassedInMillis = 0;

	private ImageRaster texture;
	private int textureWidth = 500;
	private int textureHeight = 500;

	private Space3D space3D; // apply gravit to particles

	private PopulationManager populationManager;

	private int poolsize = 200;

	private Map<Gene, Float> gene2fitness;

	// limit acceleration values (increase values with gravity!)
	private float minAcc= -0.2f;
	private float maxAcc=  0.2f;
		
	private Collection<Rocket> rockets;

	private int maxRocketLifespan = 600;

	private Point3D startPos;
	private Point3D target;		
	private Collection<Obstacle> obstacles;
		
	private final float maxDist = (float)Math.sqrt(textureWidth*textureWidth + textureHeight*textureHeight);
	
	public RocketEvolutionShader(Lightsource lightsource) {
		super(lightsource);

		texture = new ImageRaster(textureWidth, textureHeight);
		startPos = new Point3D(textureWidth / 2, 10, 0);
		target = new Point3D(textureWidth/2, textureHeight - 10, 0);
		
		obstacles = new ArrayList<Obstacle>();
		obstacles.add( new Obstacle(textureWidth/2, textureHeight/2 - 50) );
		
		obstacles.add( new Obstacle(110, textureHeight/2+textureHeight/6, 220, 10) );
		obstacles.add( new Obstacle(textureWidth-110, textureHeight/2+textureHeight/6, 220, 10) );
		
		obstacles.add( new Obstacle(textureWidth/2, textureHeight-40, 50, 10) );
		
		rockets = new ArrayList<Rocket>();
		gene2fitness = new HashMap<Gene, Float>();
		space3D = new Space3D(Space3D.EARTH_G);

		RandomMutationOnePntCrossOver evolver = new RandomMutationOnePntCrossOver();
		evolver.setSelectionRate(0.2f);
		evolver.setMutationRate(0.01f);

		populationManager = new PopulationManager(new GeneEvaluater() {
			@Override
			public void init() {				
			}
			@Override
			public Float getFitness(Gene g) {
				return gene2fitness.get(g);
			}
			@Override
			public void generationComplete() {
				gene2fitness.clear();
				rockets.clear();
			}
			@Override
			public void evaluate(Gene g) {
				Rocket rocket = new Rocket(g);
				rocket.start(startPos.copy());
				rockets.add(rocket);
			}
		}, evolver);

		populationManager.attachPopulationListener(new PopulationListener() {
			@Override
			public void newGeneration(Generation gen) {
				System.out.println("gen no = " + gen.getIndex());
				System.out.println("best = " + gen.bestFitness());
				System.out.println("avg = " + gen.averageFitness());
			}
		});

		populationManager.setMaxGenerations(1000);
		populationManager.setPool(getInitialPool());
		populationManager.startEvolution();

		lastTimeInMillis = System.currentTimeMillis();
	}
	
	
	public void stopEvolution(){
		this.populationManager.stopEvolution();
	}

	private Gene[] getInitialPool() {
		Gene[] initialPool = new Gene[poolsize];
		for (int i = 0; i < initialPool.length; i++) {
			initialPool[i] = createRandomGene(i);
		}
		return initialPool;
	}

	private Gene createRandomGene(int geneId) {
		float[] seq = new float[maxRocketLifespan * 2]; // one 2d acc vector for
														// each live cycle
		for (int i = 0; i < seq.length; i++) {
			seq[i] = (float) (Math.random());
		}
		return new Gene(geneId, seq);
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

		long timestepInMillis = 20;

		while (timePassedInMillis > timestepInMillis) {
						
			float secondsPassed = timePassedInMillis * 0.001f;
			timePassedInMillis = 0;

			setBackground(Color.white);

			// draw the target
			CirclesShader.drawCircle(texture, new Circle(target.x, target.y, 10), Color.blue.getRGB());

			// draw the obstacles
			for (Obstacle obstacle : obstacles) {
				Toolbox.fillPolygon(texture, obstacle.getX(), obstacle.getY(), Color.red.getRGB());
			}				
			
			for (Rocket rocket : rockets) {

				if (rocket.isDone()) {
					continue;
				}

				rocket.step();

//				Vector3D acc = space3D.getGravityVector().add(rocket.getAccerleration());
				Vector3D acc = rocket.getAccerleration();
				space3D.simulate(rocket, acc, secondsPassed);

				// screen coordinates of the rocket
				int x = (int) (rocket.getOrigin().x);
				int y = (int) (rocket.getOrigin().y);

				float distanceToTarget = Point3D.distance(target.x, target.y, 0, x, y, 0);

				// update fitness
				//float fitness = 1f / distanceToTarget + (float)rocket.lifespan/(float)maxRocketLifespan;
				float fitness = 1f / distanceToTarget + (maxDist-distanceToTarget)/maxDist;
				
				
				if(Toolbox.isOutisde(rocket.getOrigin().x, 0, textureWidth-1)){
					fitness *= 0.1;
					rocket.done = true;
				}
				else if(Toolbox.isOutisde(rocket.getOrigin().y, 0, textureHeight-1)){
					fitness *= 0.1;
					rocket.done = true;
				}
				
				for (Obstacle obstacle : obstacles) {
					if(obstacle.hit(rocket)){
						fitness *= 0.1;
						rocket.done = true; 
						break;
					}
				}
				
				if (distanceToTarget < 10) {
					fitness = 1f;
					rocket.done = true;
				}
				
				
				rocket.setFitness(fitness);
				
				// draw the rocket
				CirclesShader.drawCircle(texture, new Circle(x, y, 4), Color.BLACK.getRGB());

				if (rocket.isDone()) {
					gene2fitness.put(rocket.getGene(), new Float(rocket.getFitness()));
				}
			}
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
	
	
	/**
	 * A rectangle on screen blocking rockets
	 * */
	public class Obstacle{
		public Obstacle(int x, int y) {
			this.pos_x = x;
			this.pos_y = y;
		}
		
		public Obstacle(int x, int y, int w, int h) {
			this(x, y);
			this.width = w;
			this.height = h;
		}

		int[] getX(){
			return new int[]{pos_x-width/2, pos_x+width/2, pos_x+width/2, pos_x-width/2};
		}
		
		int[] getY(){
			return new int[]{pos_y-height/2, pos_y-height/2, pos_y+height/2, pos_y+height/2};
		}
		
		
		boolean hit(Rocket rocket){
			if(rocket.getOrigin().x >= pos_x-width/2 && rocket.getOrigin().x<=pos_x+width/2){
				if(rocket.getOrigin().y >= pos_y-height/2 && rocket.getOrigin().y<=pos_y+height/2){
					return true;
				}	
			}
			return false;
		}
		
		int pos_x;
		int pos_y;
		
		int width = textureWidth/2;
		int height = 10;;		
	}
	

	/**
	 * A rocket is seeded with a gene and can update its state
	 * by step.
	 * 
	 **/
	public class Rocket extends Object3D implements OIS3D {

		private Vector3D velocity;
		private Vector3D acceleration;
		private float fitness;
		private int lifespan;
		private int seqCounter = 0;
		private boolean done = false;
		private Gene gene;

		public Rocket(Gene g) {
			super();
			
			this.gene = g;				
			this.velocity = new Vector3D();
			this.acceleration = new Vector3D();
		}

		public Gene getGene() {
			return gene;
		}

		public Vector3D getAccerleration() {
			return this.acceleration;
		}

		public void applyForce(Vector3D force) {
			this.acceleration.add(force);
		}

		public void setFitness(float fitness) {
			this.fitness = fitness;
		}

		public float getFitness() {
			return fitness;
		}

		public void step() {

			if (isDone())
				return;

			// apply next acc vector from gene seq array
			float acc_x = (float) Toolbox.map(gene.getSequence()[seqCounter++], 0, 1, minAcc, maxAcc);
			float acc_y = (float) Toolbox.map(gene.getSequence()[seqCounter++], 0, 1, minAcc, maxAcc);

			applyForce(new Vector3D(acc_x, acc_y, 0));

			// each step the life span is increased by one
			lifespan++;
		}

		public void start(Point3D startPos) {
			setOrigin(startPos);
			velocity = new Vector3D();
			acceleration = new Vector3D();

			fitness = 0;
			seqCounter = 0;
			lifespan = 0;

			done = false;
		}

		public boolean isDone() {
			return done || lifespan >= maxRocketLifespan;
		}

		@Override
		public Vector3D getVelocityVector() {
			return velocity;
		}
	}
}
