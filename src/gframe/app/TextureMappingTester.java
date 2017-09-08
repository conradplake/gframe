package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;

import gframe.DoubleBufferedFrame;
import gframe.engine.Engine3D;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.NormalMappedTextureShader;
import gframe.engine.Shader;
import gframe.engine.TextureShader;
import gframe.engine.camera.TripodCamera;
import gframe.engine.generator.Model3DGenerator;
import gframe.engine.generator.TextureGenerator;
import gframe.engine.shader.SkyShader;
import gframe.engine.shader.TestShader;

public class TextureMappingTester extends DoubleBufferedFrame implements MouseMotionListener {

	public TextureMappingTester() {
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
		lightsource.setIsDirectional(true);
		lightsource.rotate(-90, 0, 0);
		engine.setLightsource(lightsource);
					
		Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(new File("textures/chessboard.jpg"), 256, 256));
		engine.setDefaultShader(shader);		
	}
	

	public void initWorld() {	

		int tileSize = 40;
//		Color floorTileColor = new Color(118, 206, 235);
		Color floorTileColor = Color.BLUE;
		
		TextureShader roadShader = new TextureShader(lightsource, TextureShader.getRGBRaster(new File("./textures/diffuse/street-diffuse.jpg"), 512, 512));		
//		Shader floorShader = new TestShader(lightsource); 
//		TextureShader floorShader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateTileTexture(320, 320, tileSize, floorTileColor.getRGB(), floorTileColor.darker().getRGB()), TextureGenerator.generateTileTextureNormalMap(320, 320, tileSize));
		
		Model3D road = Model3DGenerator.buildTiledFloor(1, 100, 512, Color.BLUE);		
		engine.register(road, roadShader);
		
		
		Model3D skydome = Model3DGenerator.buildSkydome(100000, Color.BLUE);
		Shader skydomeShader = new SkyShader(lightsource, skydome);
		engine.register(skydome, skydomeShader);
		
		lightsource.x=500;
		lightsource.y=1500;
		lightsource.z=1000;

		// CAMERA SETTINGS	
		camera = new TripodCamera();
		camera.move(40, 10, -20);
		engine.setCamera(camera);
	}

	
	private void start(long millidelay) {
		setSize(SCREENX, SCREENY);
		setLocation(20, 0);
		setBackground(Color.GREEN);
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
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.MAGENTA, 4096, 4096), TextureShader.getRGBRaster(new File("textures/normal/Normal_map_example.png"), 4096, 4096));
					//Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(Color.MAGENTA, 4096, 4096));
					engine.setDefaultShader(shader);
				} 
				else if (keycode == KeyEvent.VK_2) {				
					//Shader shader = new TextureShader(lightsource, TextureGenerator.generateWoodTexture(512, 512, 255));
					Shader shader = new TextureShader(lightsource, TextureGenerator.generateVoronoiTexture(128, 128, 1000, null));
					engine.setDefaultShader(shader);
				} 
				else if (keycode == KeyEvent.VK_3) {	
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.blue, 512, 512), TextureShader.getRGBRaster(new File("textures/normal/Hand-Normal-Map.png"), 512, 512));
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_4) {
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.blue, 1024, 1024), TextureShader.getRGBRaster(new File("textures/normal/chesterfield_normal.png"), 1024, 1024));
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_5) {
//					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.BLUE, 512, 512), TextureShader.getRGBRaster(new File("textures/normal/639-normal_scifi.jpg"), 512, 512));
					Shader shader = new TextureShader(lightsource, TextureGenerator.generateWoodTexture(512, 512, 255));
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_6) {
					//Shader shader = new TextureShader(lightsource, TextureGenerator.generateTileTextureNormalMap(512, 512, 40));
					//Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.blue, 2187, 2187), TextureGenerator.generateMengerSpongeNormalMap(2187));
					Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.blue, 243, 243), TextureGenerator.generateMengerSpongeNormalMap(243));
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_7) {
					Shader shader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateTileTexture(160, 160, 20, Color.BLUE.getRGB(), Color.BLUE.getRGB()),  TextureGenerator.generateTileTextureNormalMap(160, 160, 20));						
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_8) {
					Shader shader = new TestShader(lightsource);
					engine.setDefaultShader(shader);
				}
				else if (keycode == KeyEvent.VK_9) {
					Shader shader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateDiscoTileTexture(320, 320, 20), TextureGenerator.generateTileTextureNormalMap(320, 320, 20));
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
				}
				
				else if (keycode == KeyEvent.VK_Y) {					
					model.rotate(0, 1, 0);
				}
				else if (keycode == KeyEvent.VK_X) {					
					model.rotate(0, -1, 0);
				}
				
				else if (keycode == KeyEvent.VK_C) {					
					model.rotate(0, 0, 1);
				}
				else if (keycode == KeyEvent.VK_V) {					
					model.rotate(0, 0, -1);
				}
				
				else if (keycode == KeyEvent.VK_O) {					
					model.rotate(1, 0, 0);
				}
				else if (keycode == KeyEvent.VK_P) {					
					model.rotate(-1, 0, 0);
				}
				
				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-5, 0, 0);
				}
				else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(5, 0, 0);
				}
				else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 5, 0);
				}
				else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, -5, 0);
				}
				else if (keycode == KeyEvent.VK_PAGE_UP) {
					lightsource.move(0, 0, 1);
				}
				else if (keycode == KeyEvent.VK_PAGE_DOWN) {
					lightsource.move(0, 0, -1);
				}
				else if (keycode == KeyEvent.VK_B) {
					Shader shader = engine.getDefaultShader();
					if(shader!=null && shader instanceof TextureShader){
						TextureShader textureShader = (TextureShader)shader;
						boolean isBiliinearFiltering = textureShader.isBilinearFilteringEnabled();
						textureShader.setIsBilinearFiltering(!isBiliinearFiltering);
					}
				}
				else if (keycode == KeyEvent.VK_H) {
					Shader shader = engine.getDefaultShader();
					if(shader!=null && shader instanceof TextureShader){
						TextureShader textureShader = (TextureShader)shader;
						boolean isDithering = textureShader.isDitheringEnabled();
						textureShader.setIsDithering(!isDithering);
					}
				}
				
			}
		}
		super.processKeyEvent(event);
	}

	public void paint(Graphics g) {				
		long updateTime = System.currentTimeMillis();
		
		engine.drawScenes(frame);			
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
		TextureMappingTester main = new TextureMappingTester();
		main.start();
	}

	
	private Model3D model;	
	
	private Engine3D engine;

	private ImageRaster frame;


	private TripodCamera camera;
	private Lightsource lightsource;

	private int mouseX;
	private int mouseY;

	public static int SCREENX = 800;
	public static int SCREENY = 600;
}