package gframe.app;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import gframe.DoubleBufferedFrame;
import gframe.engine.Engine3D;
import gframe.engine.FlatShader;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.Point3D;
import gframe.engine.Shader;
import gframe.engine.camera.Camera;
import gframe.engine.camera.TripodCamera;
import gframe.engine.camera.ViewCone;
import gframe.engine.generator.Model3DGenerator;
import gframe.parser.Model3DParser;
import gframe.parser.WavefrontObjParser;

public class Model3DViewer extends DoubleBufferedFrame implements MouseMotionListener, ItemListener {

	public static void main(String[] args) {
		Model3DViewer viewer = new Model3DViewer();
		viewer.start(2);
	}

	public Model3DViewer() {
		super("model viewer");
		setSize(SCREENX, SCREENY);
		setLocation(20, 0);
		setIgnoreRepaint(true);

		setLayout(null);
		selectList = new Choice();		
		selectList.addItemListener(this);
		selectList.setBounds(10, 30, 500, 500);
		add(selectList);
		

		setBackground(Color.lightGray);
		setForeground(Color.black);
		
		addMouseMotionListener(this);
		enableEvents(AWTEvent.KEY_EVENT_MASK);		
		
		

		loadConfig();
		init();
		setupWorld();
	}
	
	
	private void fillWithModelNames(File file, Collection<String> names){
		for(File child : file.listFiles()){
			if(child.isDirectory()){
				fillWithModelNames(child, names);
			}
			else{
				names.add(child.getPath());
			}
		}
		
	}

	public void init() {
		File graphdir = new File(MODELDIR);
		
		Collection<String> modelNames = new LinkedList<String>();
		fillWithModelNames(graphdir, modelNames);
		for (String modelName : modelNames) {
			selectList.add(modelName);
		}		
		
//		String[] modArr = graphdir.list();
//		if (modArr != null) {
//			for (int i = 0; i < modArr.length; i++) {
//				selectList.add(modArr[i]);
//			}
//		}

		engine = new Engine3D(SCREENX, SCREENY);		

		lightsource = new Lightsource(0, 0, -2000, Color.white, Lightsource.NORM_INTENSITY);
		engine.setLightsource(lightsource);
		engine.setCamera(new TripodCamera());
				
//		Shader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(Color.GRAY, 800, 600), TextureShader.getRGBRaster(new File("textures/stonerock_ps_01b.jpg"), 800, 600));
//		engine.setDefaultShader(shader);
		
//		Shader shader = new FlatShader(lightsource);
//		engine.setDefaultShader(shader);			
		
//		Shader shader = new TextureShader(lightsource, TextureShader.getRGBRaster(Color.CYAN, 256, 256));
//		engine.setDefaultShader(shader);
		
		
		ViewCone viewCone = new ViewCone(40, 100, 4000);
		engine.getCamera().setViewCone(viewCone);
		
		
//		engine.occlusionCullingEnabled = false;
		
	}

	private void loadConfig() {
		props = new Properties();
		try {
			FileInputStream fis = new FileInputStream(PROPERTYFILE);
			props.load(fis);
		} catch (Exception any) {
			System.out.println("Failed to load properties from file!");
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (e.isMetaDown()) {
			int distX = e.getX() - mouseX;
			int distY = e.getY() - mouseY;
			engine.getCamera().move(distY<<2);
			engine.getCamera().move(distX, 0, 0);
		} else {
			if (model != null) {
				int distX = e.getX() - mouseX;
				int distY = e.getY() - mouseY;
				model.rotate(-distY, -distX, 0);
			}
		}
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void itemStateChanged(ItemEvent event) {
		engine.deregister(model);
		String nextModel = selectList.getSelectedItem();
		model = loadModel(nextModel);
//		model.rotate(0, 90, 0);
		if (model != null) {
			// System.out.println("registering new model: "+model+", BR =
			// "+model.getBoundingSphereRadius()+", vertices:
			// "+model.numberOfVertices()+", faces: "+model.numberOfFaces());
			System.out.println("Registering new model: " + nextModel + " with vertices: " + model.numberOfVertices()
					+ ", faces: " + model.numberOfFaces());
			engine.register(model, false);
		}
	}

	public void processKeyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_PRESSED) {
			if (event.getKeyCode() == KeyEvent.VK_F1) {
				engine.resetCamera();
			} else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			} else {
				if (event.getKeyCode() == KeyEvent.VK_LEFT) {

				}
				if (event.getKeyCode() == KeyEvent.VK_RIGHT) {

				}
				if (event.getKeyCode() == KeyEvent.VK_UP) {
					engine.getCamera().move(0, -10, 0);
				}
				if (event.getKeyCode() == KeyEvent.VK_DOWN) {
					engine.getCamera().move(0, 10, 0);
				}			
				if (event.getKeyCode() == KeyEvent.VK_PLUS) {
					model.scale(10, 10, 10);
				}
				if (event.getKeyCode() == KeyEvent.VK_MINUS) {
					model.scale(.1f, .1f, .1f);
				}
				
				
							
			}
			repaint();
		}
		super.processKeyEvent(event);
	}

	private void start(long millidelay) {
		setVisible(true);	
		
		Graphics g = this.getGraphics();
		while (true) {
			update(g);
					
			// next iterate through next house part and print out indexs
//			if(counter%20==0 && partCounter<model.getChildren().size()){
//				
//				if(partCounter>0){
//					// reset current part color
//					Model3D part = (Model3D)model.getChildren().get(partCounter-1);
//					part.setColor(Color.BLACK);
//				}
//				Model3D part = (Model3D)model.getChildren().get(partCounter);
//				part.setColor(Color.YELLOW);
//				
//				System.out.println("Current part index: "+partCounter);
//				
//				partCounter++;
//			}
//			counter++;
			
			if(model!=null)
				   model.rotate(0, 0.05f, 0);					

			if(millidelay>0){
				try {
					Thread.sleep(millidelay);
				} catch (InterruptedException ie) {
				}	
			}			
		}
	}

	private void setupWorld() {
		engine.clear();
		Camera cam = new Camera(new Point3D(0, 0, -400));
		engine.setCamera(cam);
		
		model = loadModel("./models/structures/Plane.m3d");
	}

	private Model3D loadModel(String filename) {
		return loadModel(new Point3D(0, 0, 0), filename);
	}

	private Model3D loadModel(Point3D origin, String filename) {
		Model3D mod = null;
		try {
			if (filename.endsWith(".m3d")) {
				mod = Model3DParser.parseModel3D(filename);
			} else if (filename.endsWith(".obj")) {
				mod = WavefrontObjParser.parse(new File(filename), Color.white);
			}
			
			
			Model3DGenerator.normalizeOrigin(mod);
			
//			mod = Model3DGenerator.split(mod);
//			for(Object part : mod.getChildren()){
//				
//				int r = (int)(Math.random()*255);
//				int g = (int)(Math.random()*255);
//				int b = (int)(Math.random()*255);				
//				int	t = r+g+b;				
//				while (t<300){
//					r = (int)(Math.random()*255);
//					g = (int)(Math.random()*255);
//					b = (int)(Math.random()*255);				
//					t = r+g+b;
//				}
//																			
//				((Model3D)part).setColor(new Color(r, g, b));
//			}
			
			mod.setOrigin(origin);
			mod.move(0, 0, 10000);			
			
			if(filename.endsWith(".obj")){
				mod.scale(5, 5, 5);	
			}
			
//			mod.recomputeFaceNormals();			
			
			counter = 0;
			
		} catch (java.io.IOException ioe) {
			System.out.println("could not load model from file: " + filename);
			mod = null;
		} catch (NumberFormatException nfe) {
			System.out.println("invalid file: " + filename);
			mod = null;
		}
				
		return mod;
	}

	public void paint(Graphics g) {
		long updateTime = System.currentTimeMillis();
		engine.drawScene(g);		
		updateTime = System.currentTimeMillis() - updateTime;		
		if(updateTime<16){ // 33ms ~ 30 FPS
			try {
				Thread.sleep(16-updateTime);	
				updateTime = 16;
			} catch (InterruptedException ie) {
			}
		}
		
		counter++;
		if(counter%50==0){
			System.out.println("FPS: "+(1000/updateTime));	
		}			
	}

	
	int counter=0;

	private final String PROPERTYFILE = "./model3DViewer.properties";
	private final String MODELDIR = "./models/";
	private final int SCREENX = 1024, SCREENY = 768;

	private int mouseX, mouseY;

	private Properties props;
	private Engine3D engine;
	private Model3D model;
	int partCounter = 0;
	private Choice selectList;
	private Lightsource lightsource;
}