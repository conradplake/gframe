package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import gframe.DoubleBufferedFrame;
import gframe.engine.Engine3D;
import gframe.engine.FlatShader;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.PhongShader;
import gframe.engine.Point3D;
import gframe.engine.camera.TripodCamera;
import gframe.engine.generator.Model3DGenerator;
import gframe.engine.shader.SkyShader;
import gframe.engine.timing.Rotate;
import gframe.engine.timing.Timer;
import imaging.ImageRaster;

public class SkydomeTester extends DoubleBufferedFrame implements MouseMotionListener {

	public SkydomeTester() {
		super();
		setBackground(Color.lightGray);
		frame = new ImageRaster(SCREENX, SCREENY);
	}

	public void start() {
		initEngine();
		initWorld();
		start(5);
	}
	
	
	private void exit() {
		System.exit(0);
	}

	
	private void initEngine() {
		engine = new Engine3D(2, SCREENX, SCREENY);			
	}
	

	public void initWorld() {	
				
		// LIGHT SETTINGS
		engine.setLightsource(new Lightsource(100,10000, 100, Color.white,
							Lightsource.MAX_INTENSITY));
		
		
		Color lightskyblue = new Color(135, 206, 250);
		Color darkblue = new Color(0, 0, 140);
						
		Model3D skydome = Model3DGenerator.buildSkydome(40000, lightskyblue);
		SkyShader skydomeShader = new SkyShader(engine.getLightsource(), skydome, Color.orange, darkblue);				
		engine.register(skydome, skydomeShader);
	
		int meshX=20;
		int meshY=20;
		int tilesize=4000;
		
		Model3D terrain = Model3DGenerator.buildFlatMesh(meshX, meshY, tilesize, Color.GREEN);
//		Model3D terrain = Model3DGenerator.buildRandomVoronoiTerrainMesh(meshX, meshY, tilesize, 100, Color.GREEN);
//		terrain.scale(1, 20, 1);
		
		Model3DGenerator.laplacianSmooth(meshX, meshY, terrain);		
		Model3DGenerator.laplacianSmooth(meshX, meshY, terrain);
		terrain.move(-meshX*tilesize/2, 0, -meshY*tilesize/2);
		engine.register(terrain, new FlatShader(engine.getLightsource()));
		
		water = Model3DGenerator.buildTile(15000, new Point3D(), Color.BLUE);
//		Model3D water = Model3DGenerator.buildFlatMesh(4, 4, 4000, Color.BLUE);
//		Model3DGenerator.normalizeOrigin(water);
		
		water.move(0, 500, 7500);
		engine.register(water, new FlatShader(engine.getLightsource()));
		
		Rotate rotate = new Rotate(water, 100000000L, 0.05f, Rotate.AXIS_Y);
		Timer.getInstance().registerTimedObject(rotate);

		// CAMERA SETTINGS
		if (camera == null) {
			camera = new TripodCamera();
			camera.move(0, 4000, -5000);
			camera.rotate(-20, 0);
			engine.setCamera(camera);							
		}
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
		}
	}

	
	public void mouseDragged(MouseEvent e) {
		if (e.isMetaDown()) {
			int distY = e.getY() - mouseY;
			camera.move(0, distY, 0);
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
				if (keycode == KeyEvent.VK_F2) {				
				} else if (keycode == KeyEvent.VK_F3) {
					engine.shadingEnabled = !engine.shadingEnabled;
				}
				// CAM CONTROL
				else if (keycode == KeyEvent.VK_1) {
					engine.setCamera(camera);
				} else if (keycode == KeyEvent.VK_2) {
	
				} else if (keycode == KeyEvent.VK_3) {
	
				}

				else if (keycode == KeyEvent.VK_S) {
					camera.move(-100);
				} else if (keycode == KeyEvent.VK_A) {
					camera.move(-50, 0, 0);
				} else if (keycode == KeyEvent.VK_D) {
					camera.move(50, 0, 0);
				} else if (keycode == KeyEvent.VK_W) {
					camera.move(100);
				} else if (keycode == KeyEvent.VK_N) {
					camera.rotate(0, -7, 0);
				} else if (keycode == KeyEvent.VK_M) {
					camera.rotate(0, 7, 0);
				}
				
				else if (keycode == KeyEvent.VK_Y) {
					water.move(0, 10, 0);
				}
				else if (keycode == KeyEvent.VK_X) {
					water.move(0, -10, 0);
				}
				
				else if (keycode == KeyEvent.VK_C) {
					water.scale(1.2f, 1.2f, 1.2f);
				}
				else if (keycode == KeyEvent.VK_V) {
					water.scale(0.8f, 0.8f, 0.8f);
				}
				


			}
		}
		super.processKeyEvent(event);
	}

	public void paint(Graphics g) {
//		frame.clear();
//		engine.drawScenes(frame);			
//		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);
		
		
		long updateTime = System.currentTimeMillis();
		
		frame.clear();
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

	public static void main(String[] args) {
		SkydomeTester main = new SkydomeTester();
		main.start();
	}

	
	private Model3D skydome;
	private Model3D water;
	
	private Engine3D engine;

	private ImageRaster frame;


	private TripodCamera camera;

	private int mouseX;
	private int mouseY;
	
	private int counter;

	public static int SCREENX = 1024;
	public static int SCREENY = 768;
}