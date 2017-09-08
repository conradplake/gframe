package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import gframe.DoubleBufferedFrame;
import gframe.engine.Engine3D;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.Point3D;
import gframe.engine.camera.TripodCamera;

public class ZBufferTester extends DoubleBufferedFrame implements MouseMotionListener {

	public ZBufferTester() {
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
		
		frontModel = new Model3D(new Point3D(-100,0,100));
		frontModel.addVertex(-100, 0, 0);
		frontModel.addVertex(0, 100, 0);
		frontModel.addVertex(100, 0, 0);		
		frontModel.stretchFace(new int[]{0,1,2}, Color.green);
		engine.register(frontModel);
		
		backModel = new Model3D(new Point3D(100,0,120));
		backModel.addVertex(-100, 0, 0);
		backModel.addVertex(0, 100, 0);
		backModel.addVertex(100, 0, 0);
		backModel.stretchFace(new int[]{0,1,2}, Color.red);			
		engine.register(backModel);
		
		backModel.rotate(0, 45, 0);
		
		// LIGHT SETTINGS
		engine.setLightsource(new Lightsource(50,20,-20, Color.white,
					Lightsource.MAX_INTENSITY));


		// CAMERA SETTINGS
		if (camera == null) {
			camera = new TripodCamera();
			camera.move(0, 0, -500);
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
				
				else if (keycode == KeyEvent.VK_Y) {
					frontModel.move(-1, 0, 0);
				}
				else if (keycode == KeyEvent.VK_X) {
					frontModel.move(1, 0, 0);
				}


			}
		}
		super.processKeyEvent(event);
	}

	public void paint(Graphics g) {
		frame.clear();
		engine.drawScenes(frame);			
		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);		
	}

	public static void main(String[] args) {
		ZBufferTester main = new ZBufferTester();
		main.start();
	}

	
	private Model3D frontModel;
	private Model3D backModel;
	private Model3D inBetweenModel;
	
	private Engine3D engine;

	private ImageRaster frame;


	private TripodCamera camera;

	private int mouseX;
	private int mouseY;

	public static int SCREENX = 1024;
	public static int SCREENY = 768;
}