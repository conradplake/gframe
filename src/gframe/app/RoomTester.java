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
import gframe.engine.PhongShader;
import gframe.engine.Shader;
import gframe.engine.TextureShader;
import gframe.engine.camera.TripodCamera;
import gframe.parser.WavefrontObjParser;

public class RoomTester extends DoubleBufferedFrame implements MouseMotionListener {

	public RoomTester() {
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
		lightsource.setShadowsEnabled(true);
		engine.setLightsource(lightsource);
									
		//Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(new File("textures/chessboard.jpg"), 256, 256));
//		Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(Color.CYAN, 256, 256));
		Shader shader = new PhongShader(lightsource);
//		Shader shader = new FlatShader(lightsource);
		engine.setDefaultShader(shader);		
	}
	

	
	
	private void initWorld() {	
		
		engine.clear();
											
		Model3D room = WavefrontObjParser.parse(new File("models/rooms_and_interior/Bedroom.obj"), Color.LIGHT_GRAY);
		room.scale(3, 3, 3);
		
//		Model3D room = WavefrontObjParser.parse(new File("models/luxury house interior.obj"), Color.LIGHT_GRAY);
		engine.register(room);		
		
		// CAMERA SETTINGS	
		camera = new TripodCamera();
		camera.move(0, 200, -1200);
		engine.setCamera(camera);				
		
		
		// LIGHT SETTNGS
		lightsource.move(0, 400, -1000);
		lightsource.rotate(-20, 0, 0);
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
				} 
				
				else if (keycode == KeyEvent.VK_R) {					
					initWorld();				
				} else if (keycode == KeyEvent.VK_F) {					
					engine.filterDepthBuffer = !engine.filterDepthBuffer;
				}				
				
				else if (keycode == KeyEvent.VK_Y) {
					lightsource.rotate(0, 5, 0);
				} else if (keycode == KeyEvent.VK_X) {					
					lightsource.rotate(0, -5, 0);
				}

				
				else if (keycode == KeyEvent.VK_C) {
					lightsource.rotate(5, 0, 0);
				} else if (keycode == KeyEvent.VK_V) {					
					lightsource.rotate(-5, 0, 0);
				}
				
				
				
				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-10, 0, 0);
				}
				else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(10, 0, 0);
				}
				else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 0, 10);
				}
				else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, 0, -10);
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
		RoomTester main = new RoomTester();
		main.start();
	}

	
	private Engine3D engine;

	private ImageRaster frame;


	private TripodCamera camera;
	private Lightsource lightsource;	

	private int mouseX;
	private int mouseY;

	public static int SCREENX = 800;
	public static int SCREENY = 600;
}