package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gframe.Space3D;
import gframe.engine.Engine3D;
import gframe.engine.ImageRaster;
import gframe.engine.KeyPosition;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.Particle;
import gframe.engine.PhongShader;
import gframe.engine.Point3D;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;
import gframe.engine.camera.Camera;
import gframe.engine.camera.TripodCamera;
import gframe.engine.generator.Model3DGenerator;
import gframe.engine.generator.NoiseGenerator;
import gframe.engine.shader.SkyShader;
import gframe.engine.timing.CamShake;
import gframe.engine.timing.CamZoom;
import gframe.engine.timing.FadeOut;
import gframe.engine.timing.FadeOutFadeIn;
import gframe.engine.timing.MusicFadeOut;
import gframe.engine.timing.Timed;
import gframe.engine.timing.Timer;
import gframe.parser.AnimationsParser;
import gframe.parser.WavefrontObjParser;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class Xmas2016 extends DoubleBufferedFrame implements MouseMotionListener {

	private static final int NUMBER_OF_SNOWFLAKES = 20000;
	
	
	public Xmas2016() {
		super();
		setBackground(Color.BLACK);
		setResizable(false);
		frame = new ImageRaster(SCREENX, SCREENY);
		snow = new ArrayList<Particle>();
		space3D = new Space3D(Space3D.MOON_G/10f);	
		new JFXPanel(); // init java fx for audio playback
		
		// ENGINE SETTIGNS		
		engine = new Engine3D(2, SCREENX, SCREENY);
		
		// LIGHT SETTINGS
		
		Lightsource.AMBIENT_LIGHT_INTENSITY = 0;
		
		lightsource = new Lightsource(0, 0, 0, Color.WHITE, Lightsource.MAX_INTENSITY);
		engine.setLightsource(lightsource);		
				
		
		// -- DISPLAY MODE SETTINGS				
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if(device.isFullScreenSupported()){
			DisplayMode newMode = new DisplayMode(SCREENX, SCREENY, 32, 60);
			this.setUndecorated(true);
			this.setResizable(false);
			device.setFullScreenWindow(this);
			device.setDisplayMode(newMode);
		}else{
			setSize(SCREENX, SCREENY);
			setLocation(20, 0);
			setLayout(null);
		}	
		this.setIgnoreRepaint(true);
		
		
		// EVENT SETTINGS
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		addMouseMotionListener(this);		
	}	
	

	public void start() {			
		startPart1();
		exit();
	}

	private void exit() {
		System.exit(0);
	}

	private void initPart1() {
		
		// SETUP AUDIO
		mediaPlayer = new MediaPlayer(new Media((new File("./audio/Twinkle_Twinkle_Little_Star.mp3")).toURI().toString()));

		// SETUP SCENE
		Color lightskyblue = new Color(135, 206, 250);
		//Color zenithColor = new Color(60, 100, 140);
		Color zenithColor = new Color(30, 50, 70);
		Color horizonColor = new Color(0, 0, 100);
		Model3D skydome = Model3DGenerator.buildSkydome(1000, lightskyblue);
		SkyShader skydomeShader = new SkyShader(engine.getLightsource(), skydome, horizonColor, zenithColor, 1000);
		engine.register(skydome, skydomeShader);				
									
		// SNOW		
		engine.setDefaultShader(null);
		for(int i=0;i<NUMBER_OF_SNOWFLAKES;i++){
			float x = -500 + (float)(Math.random()* 1000);
			float y = -500 + (float)(Math.random()* 1000);
			float z = -500 + (float)(Math.random()* 1000);			
			Particle flake = new Particle(new Point3D(x, y, z));
			
			float flakesize = 1f;
			flake.addVertex(new Point3D(-flakesize/2f, flakesize/2f, 0));
			flake.addVertex(new Point3D( flakesize/2f, flakesize/2f, 0));
			flake.addVertex(new Point3D( flakesize/2f,-flakesize/2f, 0));
			flake.addVertex(new Point3D(-flakesize/2f,-flakesize/2f, 0));
			flake.stretchFace(0, 1, 2, 3, Color.WHITE);
			
			snow.add(flake);		
			engine.register(flake);
		}
		
		Color houseColor = new Color(210, 100, 0);
		house = WavefrontObjParser.parseGeometry(new File("./models/buildings/house.obj"), houseColor);		
		house = Model3DGenerator.split(house);		
		Model3D window = (Model3D)house.getChildren().get(32);
		//Color windowColor = new Color(250, 230, 0);
		Color windowColor = Color.WHITE;
		window.setColor(windowColor);
//		window.setMaterial(Material.GOLD);	
		
		// color upper house white
		for(int i=75;i<350;i++){
			Model3D part = (Model3D)house.getChildren().get(i);			
			part.setColor(Color.WHITE);
		}
		for(int i=360;i<487;i++){
			Model3D part = (Model3D)house.getChildren().get(i);			
			part.setColor(Color.WHITE);
		}
		for(int i=9;i<14;i++){
			Model3D part = (Model3D)house.getChildren().get(i);			
			part.setColor(Color.WHITE);
		}
		Model3D roofPart = (Model3D)house.getChildren().get(494);			
		roofPart.setColor(Color.WHITE);
		roofPart = (Model3D)house.getChildren().get(2);			
		roofPart.setColor(Color.WHITE);
		roofPart = (Model3D)house.getChildren().get(3);			
		roofPart.setColor(Color.WHITE);
		roofPart = (Model3D)house.getChildren().get(19);			
		roofPart.setColor(Color.WHITE);
		
		Model3D doorPart = (Model3D)house.getChildren().get(24);			
		doorPart.setColor(Color.WHITE);
		doorPart = (Model3D)house.getChildren().get(25);
		doorPart.setColor(Color.WHITE);
		
		Model3D part = (Model3D)house.getChildren().get(23);			
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(21);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(22);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(27);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(34);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(35);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(36);		
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(37);		
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(38);		
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(39);		
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(17);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(18);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(0);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(1);
		part.setColor(Color.WHITE);
		
		part = (Model3D)house.getChildren().get(72);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(73);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(74);
		part.setColor(Color.WHITE);		
		part = (Model3D)house.getChildren().get(65);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(66);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(69);
		part.setColor(Color.WHITE);
		part = (Model3D)house.getChildren().get(70);
		part.setColor(Color.WHITE);
		
		
		house.scale(2, 2, 2);
		house.move(-100,-32,300);
		house.rotate(0, 180, 0);		
		engine.register(house, new PhongShader(lightsource));
		
		int meshX = 60;
		int meshY = 60;
		int tilesize = 50;
		Model3D terrain = Model3DGenerator.buildFlatMesh(meshX, meshY, tilesize, Color.WHITE);
		
		float yoff = 0;
		for(int y=0;y<meshY;y++){				
			//float xoff = -flyingOffset;
			float xoff = 0;
			for(int x=0;x<meshX;x++){				
				Point3D vertex = terrain.getVertices().get( y * meshX + x );
				float noise = (float)Toolbox.map(NoiseGenerator.improvedPerlinNoise(xoff, yoff), -1, 1, 0, 200);
				vertex.y = noise;
				xoff +=0.1f;
			}			
			yoff += 0.1f;
		}			
		terrain.recomputeFaceNormals();
		terrain.computeVertexNormals();
		
		terrain.move(-meshX*tilesize/2, -100, -meshY*tilesize/2);
		//engine.register(terrain, new FlatShader(engine.getLightsource()));
		engine.register(terrain, new PhongShader(lightsource));
		
		
		
		lightKeyPositions = Collections.emptyList();
		cameraKeyPositions = AnimationsParser.parse(new File("animations/xmas2016/part1_camera.ani"),
				System.currentTimeMillis(), 1f);
		events = new ArrayList<Event>();
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new FadeOutFadeIn(lightsource, 0, 10000)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+73000, new MusicFadeOut(mediaPlayer, 8000, true)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+80000, new FadeOut(lightsource, 1000)));		
				
		lightsource.x = -60;
		lightsource.y =  15;
		lightsource.z = 260;
		lightsource.setIntensity(0);
		

		// CAMERA SETTINGS
		camera = new TripodCamera();
		//camera.move(-70, 30, 200);
		camera.move(0, 30, -200);
		
		//camera.rotate(0, -90, 0);
		engine.setCamera(camera);
	}
	
	
	private void startPart1() {

		initPart1();
				
		mediaPlayer.play();
					
		Graphics graphics = this.getGraphics();
		
		long lastTime = System.currentTimeMillis();
								
		setBackground(Color.BLACK);		
		setVisible(true);
		
		while (true) {			
			update(graphics);
			
			long currentTime = System.currentTimeMillis();			
			
			// simulate snow flakes
			float deltaTimeInSeconds = (currentTime - lastTime) / 1000f;
			for (Particle snowFlake : snow) {
				space3D.simulate(snowFlake, deltaTimeInSeconds);
				
				// touchdown?
				if(snowFlake.getOrigin().y<-50){					
					Point3D flakePos = snowFlake.getOrigin();					
					flakePos.x = -500 + (float)(Math.random()* 1000);
					flakePos.y = -500 + (float)(Math.random()* 1000);
					flakePos.z = -500 + (float)(Math.random()* 1000);
					snowFlake.setVelocityVector(new Vector3D());
				}
			}			

			
			// update camera, light and other events
			boolean allKeyPositionsReached = true;
			if (!lightKeyPositions.isEmpty()) {
				allKeyPositionsReached = false;
				KeyPosition nextPosition = lightKeyPositions.get(0);
				nextPosition.updatePosition(lastTime, currentTime, lightsource);
				if (currentTime >= nextPosition.getTimestamp()) {
					lightKeyPositions.remove(0);
				}
			}
			if (!cameraKeyPositions.isEmpty()) {
				allKeyPositionsReached = false;
				KeyPosition nextPosition = cameraKeyPositions.get(0);
				nextPosition.updatePosition(lastTime, currentTime, camera);
				if (currentTime >= nextPosition.getTimestamp()) {
					cameraKeyPositions.remove(0);
				}
			}
			if (!events.isEmpty()) {
				allKeyPositionsReached = false;
				Event nextEvent = events.get(0);								
				if (currentTime >= nextEvent.getTimestamp()) {
					nextEvent.action();
					events.remove(0);
				}
			}
			
			if (allKeyPositionsReached && mediaPlayer.getStatus()==Status.STOPPED) {
				lightsource.setIntensity(0);						
				break;
			}

			lastTime = currentTime;		
		}				
	}


	public void mouseDragged(MouseEvent e) {
		if (e.isMetaDown()) {			
			int distY = e.getY() - mouseY;
			camera.move(0, distY>>1, 0);
			int distX = e.getX() - mouseX;
			camera.move(distX>>1, 0, 0);
		} else {
			int distX = e.getX() - mouseX;
			int distY = e.getY() - mouseY;
			camera.rotate(distY / 3, -distX / 3, 0);
		}		

		updateMousePointer(e);
	}

	public void mouseMoved(MouseEvent e) {
		updateMousePointer(e);
	}

	public void mousePressed(MouseEvent e) {
		updateMousePointer(e);
	}

	private void updateMousePointer(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	protected void processKeyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_PRESSED) {
			int keycode = event.getKeyCode();
			if (keycode == KeyEvent.VK_ESCAPE) {
				exit();
			} else {

				// ENGINE CONTROL
				if (keycode == KeyEvent.VK_F1) {
					
					// take camera snapshop
					FileWriter fw = null;
					try {
						fw = new FileWriter("animations/xmas2016/camera_current.txt", true);
						fw.write("0"+"\t"+camera.getOrigin().x+"\t"+camera.getOrigin().y+"\t"+camera.getOrigin().z+"\t"+camera.getMatrix().getXVector().x+"\t"+camera.getMatrix().getXVector().y+"\t"+camera.getMatrix().getXVector().z+"\t"+camera.getMatrix().getYVector().x+"\t"+camera.getMatrix().getYVector().y+"\t"+camera.getMatrix().getYVector().z+"\t"+camera.getMatrix().getZVector().x+"\t"+camera.getMatrix().getZVector().y+"\t"+camera.getMatrix().getZVector().z+"\n");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					finally{
						try {
							fw.close();
						} catch (IOException e) {							
						}
					}
					

				} else if (keycode == KeyEvent.VK_F2) {
										
					// take lightsource position snapshop
					FileWriter fw = null;
					try {
						fw = new FileWriter("animations/xmas2016/light_current.txt", true);
						fw.write("0"+"\t"+lightsource.x+"\t"+lightsource.y+"\t"+lightsource.z+"\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					finally{
						try {
							fw.close();
						} catch (IOException e) {							
						}
					}					

				} else if (keycode == KeyEvent.VK_F3) {
					engine.shadingEnabled = !engine.shadingEnabled;
				}			
				
				else if (keycode == KeyEvent.VK_N) {
					camera.rotate(0, -1, 0);
				} else if (keycode == KeyEvent.VK_M) {
					camera.rotate(0, 1, 0);
				} else if (keycode == KeyEvent.VK_P) {
					shadowsEnabled=!shadowsEnabled;
					engine.recomputeShadowMaps();
				}	
				else if (keycode == KeyEvent.VK_O) {
					engine.getDepthTestCuller().disabled = !engine.getDepthTestCuller().disabled;
				}
				else if (keycode == KeyEvent.VK_R) {
//					startPart1();
				}
				else if (keycode == KeyEvent.VK_C) {
					Timer timer = Timer.getInstance();
					timer.registerTimedObject(new CamShake(engine.getCamera(), 1500));
				}										
				else if (keycode == KeyEvent.VK_G) {
					Timer timer = Timer.getInstance();
					timer.registerTimedObject(new CamZoom(camera, 200, 5));
				}
										
				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-10, 0, 0);
				} else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(10, 0, 0);
				} else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 10, 0);
				} else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, -10, 0);
				} else if (keycode == KeyEvent.VK_PAGE_UP) {
					lightsource.move(0, 0, 10);
				} else if (keycode == KeyEvent.VK_PAGE_DOWN) {
					lightsource.move(0, 0, -10);
				} else if (keycode == KeyEvent.VK_Y) {
					camera.rotate(0, 0, 1);
				} else if (keycode == KeyEvent.VK_X) {
					camera.rotate(0, 0, -1);
				}				

				
				
				else if (keycode == KeyEvent.VK_S) {
					camera.move(-3);
//					lightsource.setCoordinates(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z);
				} else if (keycode == KeyEvent.VK_A) {
					camera.move(-3, 0, 0);
//					lightsource.setCoordinates(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z);
				} else if (keycode == KeyEvent.VK_D) {				
					camera.move(3, 0, 0);
//					lightsource.setCoordinates(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z);
				} else if (keycode == KeyEvent.VK_W) {
					camera.move(3);
//					lightsource.setCoordinates(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z);
				}
				
				engine.recomputeShadowMaps();
			}
		}
		super.processKeyEvent(event);
	}

	
	@Override
	public void paint(Graphics g) {
		long updateTime = System.currentTimeMillis();
		
		if(shadowsEnabled){
			engine.drawShadowedScene(frame);
		}else{
			engine.drawScene(frame);
		}
		
		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);
				
		updateTime = System.currentTimeMillis() - updateTime;
		if (updateTime < 33) { // 33ms ~ 30 FPS
			try {
				Thread.sleep(33 - updateTime);
				updateTime = 33;
			} catch (InterruptedException ie) {
			}
		}

//		fpsCounter++;
//		if (fpsCounter % 20 == 0) {
//			System.out.println("FPS: " + (1000 / updateTime));
//		}

	}

	public static void main(String[] args) {
		Xmas2016 xmas2016 = new Xmas2016();
		xmas2016.start();
	}
	
	
	// ------------------------------------
	
	
	
	/**
	 * 
	 * 
	 * */
	public static abstract class Event{		
		long timestamp;		
		public abstract void action();
		public long getTimestamp() {
			return timestamp;
		}		
	}

	
	public static class SparkTimedEvent extends Event{		
		private Timed timed;			
		public SparkTimedEvent(long timestamp, Timed timed) {
			this.timestamp = timestamp;
			this.timed = timed;			
		}
		@Override
		public void action() {
			Timer timer = Timer.getInstance();
			timer.registerTimedObject(timed);		
		}		
	}
	
	
	
	// -----------------------------------------------
	
	
	private Engine3D engine;

	private ImageRaster frame;

	
	private static boolean shadowsEnabled = false;
	
	private Space3D space3D;
	
	private Model3D house;
	private Collection<Particle> snow;

	List<KeyPosition> cameraKeyPositions;
	List<KeyPosition> lightKeyPositions;
	List<Event> events;

	private Camera camera;
	private Lightsource lightsource;
	private MediaPlayer mediaPlayer;

	private int mouseX;
	private int mouseY;

	public static int SCREENX = 800;
	public static int SCREENY = 600;
}