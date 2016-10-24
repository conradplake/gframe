package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;

import gframe.DoubleBufferedFrame;
import gframe.engine.DirectionalLight;
import gframe.engine.Engine3D;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.NormalMappedTextureShader;
import gframe.engine.PhongShader;
import gframe.engine.Point3D;
import gframe.engine.Shader;
import gframe.engine.TextureShader;
import gframe.engine.camera.TripodCamera;
import gframe.engine.generator.Model3DGenerator;
import gframe.engine.generator.TextureGenerator;
import gframe.engine.shader.TestShader;
import gframe.engine.timing.Rotate;
import gframe.engine.timing.Timer;
import gframe.parser.WavefrontObjParser;
import imaging.ImageRaster;

public class ShadowMapTester extends DoubleBufferedFrame implements MouseMotionListener {

	public ShadowMapTester() {
		super();
		setBackground(Color.lightGray);
		frame = new ImageRaster(SCREENX, SCREENY);
	}

	public void start() {
		initEngine();
		initWorld();
		start(10);
	}
	
	
	private void exit() {
		System.exit(0);
	}

	
	private void initEngine() {
		engine = new Engine3D(SCREENX, SCREENY);			
					
		// LIGHT SETTINGS
		lightsource = new Lightsource(0, 0, 0, Color.white, Lightsource.MAX_INTENSITY);				
		engine.setLightsource(lightsource);
				
					
		//Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(new File("textures/chessboard.jpg"), 256, 256));
//		Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(Color.CYAN, 256, 256));
		Shader shader = new PhongShader(lightsource);
//		Shader shader = new FlatShader(lightsource);
		engine.setDefaultShader(shader);
		
		
		directionalLight = new DirectionalLight(lightsource);
		engine.setDirectionalLight(directionalLight);
	}
	

	
	
	private void initWorld() {	
		
		engine.clear();
								
		// floor
		Model3D floor = Model3DGenerator.buildPlane(200, new Point3D(), Color.GREEN);
		floor.rotate(-90, 0, 0);
		engine.register(floor);;
		
		// wall
		//Model3D wall = Model3DGenerator.buildPlane(200, new Point3D(), Color.BLUE.brighter().brighter().brighter().brighter().brighter().brighter());
		Model3D wall = Model3DGenerator.buildPlane(200, new Point3D(), new Color(119, 181, 254));
		wall.move(0, 100, 100);
		engine.register(wall);;				
		
////		// cubes
//		Model3D cube1 = Model3DGenerator.buildCube(20, Color.blue);
//		cube1.move(0, 10, 0);
////		cube1.rotate(0, 45, 0);
//		engine.register(cube1);
//			
//		Model3D cube2 = Model3DGenerator.buildCube(20, Color.blue);
//		cube2.move(20, 10, 40);
////		cube2.rotate(0, 45, 0);
//		engine.register(cube2);
//			
//		Model3D cube3 = Model3DGenerator.buildCube(20, Color.blue);
//		cube3.move(-20, 10, 80);
////		cube3.rotate(0, 45, 0);
//		engine.register(cube3);

		
		final Model3D model = WavefrontObjParser.parse(new File("models/transportation/toyplane.obj"), Color.RED);
		model.move(0, 40, 0);
		engine.register(model);
		
//		final Model3D model2 = WavefrontObjParser.parse(new File("models/cart.obj"), Color.GRAY);
//		model2.scale(0.2f, 0.2f, 0.2f);
//		model2.move(-40, 0, -40);
//		engine.register(model2);
		
//		Model3D sphere = WavefrontObjParser.parse(new File("models/structures/sphere.obj"), Color.ORANGE);
		Model3D sphere = Model3DGenerator.buildSphere(1, Color.ORANGE);
		sphere.scale(10, 10, 10);
		
		sphere.move(0,10,0);
		engine.register(sphere);
		
		
		Rotate rotate = new Rotate(sphere, 100000000L, 0.05f, Rotate.AXIS_Y);
		Timer.getInstance().registerTimedObject(rotate);
		
//		Model3D stone = Model3DGenerator.buildRandomStone(Color.LIGHT_GRAY);
//		stone.move(30, 10, 0);
//		engine.register(stone);
		
//		final Model3D model = WavefrontObjParser.parse(new File("models/Ford F-250 US Forest Service.obj"), Color.LIGHT_GRAY);		
//		model.rotate(90, 0, 0);
//		engine.register(model);
			
		
		
		// CAMERA SETTINGS	
		camera = new TripodCamera();
		camera.move(0, 20, -300);
		engine.setCamera(camera);				
		
		
		// LIGHT SETTNGS
		lightsource.move(0, 100, -300);
		directionalLight.rotate(-20, 0, 0);
	}

	
	private void start(long millidelay) {
		setSize(SCREENX, SCREENY);
		setLocation(20, 0);
		setBackground(Color.black);
		setForeground(Color.black);
		setLayout(null);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		addMouseMotionListener(this);
		setVisible(true);

		/** MAIN LOOP */
		while (true) {
			repaint();
			if (millidelay > 0) {
				try {
					Thread.sleep(millidelay);
				} catch (InterruptedException ie_ignore) {
				}
			}
			
//			List<Model3D> modelList = engine.getActiveModels();
//			for (Model3D model3d : modelList) {
//				model3d.rotate(new Point3D(), 0, 0, 0.01f);	
//			}					
		}
	}

	
	public void mouseDragged(MouseEvent e) {
		if (e.isMetaDown()) {
			int distY = e.getY() - mouseY;
			camera.move(0, distY, 0);
			
			int distX = e.getX() - mouseX;
			camera.move(distX, 0, 0);
		} else {
			int distX = e.getX() - mouseX;
			int distY = e.getY() - mouseY;
			camera.rotate(distY / 3, -distX / 3, 0);
			// camera.rotate(0, distX/3, 0);
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

				}
				else if (keycode == KeyEvent.VK_F2) {	

				} 
				else if (keycode == KeyEvent.VK_F3) {
					engine.shadingEnabled = !engine.shadingEnabled;
				}
				// CAM CONTROL
				else if (keycode == KeyEvent.VK_1) {					
					//Shader shader = new TextureShader(lightsource);
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.MAGENTA, 4096, 4096), TextureShader.getRGBRaster(new File("textures/Normal_map_example.png"), 4096, 4096));
					//Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(Color.MAGENTA, 4096, 4096));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				} 
				else if (keycode == KeyEvent.VK_2) {				
					Shader shader = new TextureShader(lightsource, TextureGenerator.generateWoodTexture(512, 512, 255));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				} 
				else if (keycode == KeyEvent.VK_3) {	
					Shader shader = new TextureShader(lightsource, TextureGenerator.generateTileTexture(512, 512, 40, Color.blue.getRGB(), Color.BLUE.getRGB()));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_4) {
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.BLUE, 512, 512), TextureShader.getRGBRaster(new File("textures/9677-normal.jpg"), 512, 512));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_5) {				
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.GRAY, 800, 600), TextureShader.getRGBRaster(new File("textures/stonerock_ps_01b.jpg"), 800, 600));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_6) {
					//Shader shader = new TextureShader(lightsource, TextureGenerator.generateTileTextureNormalMap(512, 512, 40));
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.blue, 2187, 2187), TextureGenerator.generateMengerSpongeNormalMap(2187));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_7) {
					Shader shader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateTileTexture(160, 160, 20, Color.BLUE.getRGB(), Color.BLUE.getRGB()),  TextureGenerator.generateTileTextureNormalMap(160, 160, 20));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_8) {
					Shader shader = new TestShader(lightsource);
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_9) {
					Shader shader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateDiscoTileTexture(320, 320, 20), TextureGenerator.generateTileTextureNormalMap(320, 320, 20));
					shader.setDirectionalLight(engine.getDirectionalLight());
					engine.setDefaultShader(shader);
				}

				else if (keycode == KeyEvent.VK_S) {
					camera.move(-10);
				} else if (keycode == KeyEvent.VK_A) {
					camera.move(-5, 0, 0);
				} else if (keycode == KeyEvent.VK_D) {
					camera.move(5, 0, 0);
				} else if (keycode == KeyEvent.VK_W) {
					camera.move(10);
				} else if (keycode == KeyEvent.VK_N) {
					camera.rotate(0, -7, 0);
				} else if (keycode == KeyEvent.VK_M) {
					camera.rotate(0, 7, 0);
				} else if (keycode == KeyEvent.VK_R) {					
					initWorld();				
				} else if (keycode == KeyEvent.VK_F) {					
					engine.filterDepthBuffer = !engine.filterDepthBuffer;
				}
				
				
				else if (keycode == KeyEvent.VK_Y) {
					directionalLight.rotate(0, 5, 0);
				} else if (keycode == KeyEvent.VK_X) {					
					directionalLight.rotate(0, -5, 0);
				}							

				
				else if (keycode == KeyEvent.VK_C) {
					directionalLight.rotate(5, 0, 0);
				} else if (keycode == KeyEvent.VK_V) {					
					directionalLight.rotate(-5, 0, 0);
				}
				
				
				
				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-1, 0, 0);
				}
				else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(1, 0, 0);
				}
				else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 0, 1);
				}
				else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, 0, -1);
				}
				else if (keycode == KeyEvent.VK_PAGE_UP) {
					lightsource.move(0, 1, 0);
				}
				else if (keycode == KeyEvent.VK_PAGE_DOWN) {
					lightsource.move(0, -1, 0);
				}
				else if (keycode == KeyEvent.VK_B) {
					Shader shader = engine.getDefaultShader();
					if(shader!=null && shader instanceof TextureShader){
						TextureShader textureShader = (TextureShader)shader;
						boolean isBiliinearFiltering = textureShader.isBilinearFilteringEnabled();
						textureShader.setIsBilinearFiltering(!isBiliinearFiltering);
					}
				}
				
				engine.recomputeShadowMaps();
			}
		}
		super.processKeyEvent(event);
	}

	public void paint(Graphics g) {				
		long updateTime = System.currentTimeMillis();
		
		frame.clear();
		engine.drawShadowedScene(frame);
//		engine.drawScene(frame);
		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);		
		
		
		
		updateTime = System.currentTimeMillis() - updateTime;		
		if(updateTime<16){ // 33ms ~ 30 FPS
			try {
				Thread.sleep(16-updateTime);	
				updateTime = 16;
			} catch (InterruptedException ie) {
			}
		}
		
		counter++;
		if(counter%20==0){
			System.out.println("FPS: "+(1000/updateTime));	
		}
		
	}
	
	
	int counter;

	public static void main(String[] args) {
		ShadowMapTester main = new ShadowMapTester();
		main.start();
	}

	
	private Engine3D engine;

	private ImageRaster frame;


	private TripodCamera camera;
	private Lightsource lightsource;
	DirectionalLight directionalLight;

	private int mouseX;
	private int mouseY;

	public static int SCREENX = 800;
	public static int SCREENY = 600;
}