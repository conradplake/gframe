package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import gframe.DoubleBufferedFrame;
import gframe.engine.Engine3D;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.Point3D;
import gframe.engine.Toolbox;
import gframe.engine.camera.TripodCamera;

public class CurveTester extends DoubleBufferedFrame implements MouseMotionListener {

	public CurveTester() {
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
		
		
		// init corner points
		Point3D p1 = new Point3D();
		
		Point3D p2 = new Point3D(0, 100, 100);
		
		Point3D p3 = new Point3D(100, 100, 100);
		
		Point3D p4 = new Point3D(150, 200, 200);
		// compute curve
		
		List pointsOnCurve = Toolbox.toCurve(new Point3D[]{p1, p2, p3, p4}, 20);
		
		// mark all points on curve with small cubes
		for (Object object : pointsOnCurve) {
			Point3D pointOnCurve = (Point3D)object;
			Model3D cube = buildSmallCube(pointOnCurve, Color.red);
			engine.register(cube);
		}
		
		
		
		// LIGHT SETTINGS
		engine.setLightsource(new Lightsource(0,0,0, Color.white,
					Lightsource.MAX_INTENSITY));


		// CAMERA SETTINGS
		if (camera == null) {
			camera = new TripodCamera();
			camera.move(0, 100, -200);
			engine.setCamera(camera);							
		}
	}

	
	public  Model3D buildSmallCube(Point3D origin, java.awt.Color col){	
		Model3D cube = new Model3D(origin);
	  	cube.addVertex(-1,-1,-1);
	  	cube.addVertex( 1,-1,-1);
	  	cube.addVertex( 1, 1,-1);
	  	cube.addVertex(-1, 1,-1);   	 	
	  	cube.addVertex(-1,-1, 1);
	  	cube.addVertex( 1,-1, 1);
	  	cube.addVertex( 1, 1, 1);
	  	cube.addVertex(-1, 1, 1);
	  	
	   	cube.stretchFace(3,2,1,0, col);
	   	cube.stretchFace(4,5,6,7, col);   	
	   	cube.stretchFace(2,6,5,1, col);  
	   	cube.stretchFace(0,4,7,3, col);     	 	
	   	cube.stretchFace(2,3,7,6, col);   	
		return cube;	
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
					
				}
				else if (keycode == KeyEvent.VK_X) {
					
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
		CurveTester main = new CurveTester();
		main.start();
	}

			
	private Engine3D engine;

	private ImageRaster frame;


	private TripodCamera camera;

	private int mouseX;
	private int mouseY;

	public static int SCREENX = 1024;
	public static int SCREENY = 768;
}