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
import gframe.engine.Material;
import gframe.engine.Model3D;
import gframe.engine.NormalMappedTextureShader;
import gframe.engine.Shader;
import gframe.engine.TextureShader;
import gframe.engine.camera.TripodCamera;
import gframe.engine.generator.Model3DGenerator;
import gframe.engine.generator.TextureGenerator;
import gframe.parser.WavefrontObjParser;

public class TexturedModelViewer extends DoubleBufferedFrame implements MouseMotionListener {

	public TexturedModelViewer() {
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

		// TextureShader shader = new TextureShader(lightsource,
		// TextureShader.getRGBRaster(new File("textures/chessboard.jpg"), 256,
		// 256));
		// Shader shader = new NormalMappedTextureShader(lightsource,
		// TextureShader.getRGBRaster(Color.MAGENTA, 4096, 4096),
		// TextureShader.getRGBRaster(new
		// File("textures/normal/Normal_map_example.png"), 4096, 4096));

		// Shader shader = new TestShader(lightsource);
		// Shader shader = new MetaballShader(lightsource, 2);

		// Shader shader = new NormalMappedTextureShader(lightsource,
		// TextureShader.getRGBRaster(Color.white, 324, 324),
		// TextureGenerator.generateTileTextureNormalMap(324, 324, 30), true);
		// Shader shader = new NormalMappedTextureShader(lightsource,
		// TextureShader.getRGBRaster(Color.white, 324, 324),
		// TextureGenerator.generateMengerSpongeNormalMap(324), true);
		// Shader shader = new NormalMappedTextureShader(lightsource,
		// TextureGenerator.generateDiscoTileTexture(320, 320, 20),
		// TextureGenerator.generateTileTextureNormalMap(320, 320, 20), true);
		// Shader shader = new NormalMappedTextureShader(lightsource,
		// TextureShader.getRGBRaster(Color.white, 324, 324),
		// TextureGenerator.generateDefaultNormalMap(324, 324), true);

		// ((TextureShader)shader).setIsBilinearFiltering(true);

		// Shader shader = new RocketEvolutionShader(lightsource);
		// Shader shader = new PhyllotaxisShader(lightsource);

		// shader.setIsDithering(false);

		// Shader shader = new FlatShader(lightsource);

		// engine.setDefaultShader(shader);

	}

	public void initWorld() {

		engine.clear();

//		 model = WavefrontObjParser.parse(new
//		 File("models/texturemapped/Book/objBook.obj"), Color.GRAY);
//		 Shader shader = new TextureShader(lightsource,
//		 TextureGenerator.getRGBRaster(new
//		 File("models/texturemapped/Book/libro.jpg"), 1024,
//		 829));

//		model = WavefrontObjParser.parse(new File("models/texturemapped/Barrel/Gunpowder_Keg.obj"), Color.GRAY);
//		Shader shader = new NormalMappedTextureShader(lightsource,
//				TextureGenerator.getRGBRaster(new File("models/texturemapped/Barrel/Gunpowder_Keg_Diffuse.png"), 4096,
//						4096),
//				TextureGenerator.getRGBRaster(new File("models/texturemapped/Barrel/Gunpowder_Keg_Normal.png"), 4096,
//						4096),
//				TextureGenerator.getRGBRaster(new File("models/texturemapped/Barrel/Gunpowder_Keg_Specular.png"), 4096,
//						4096));

//		model.setMaterial(Material.PEARL);
		
//		engine.register(model, shader, false);
		// engine.register(model);

		lightsource.x = 0;
		lightsource.y = 0;
		lightsource.z = -300;

		// CAMERA SETTINGS
		camera = new TripodCamera();
		camera.move(0, 0, -30);
		engine.setCamera(camera);
	}

	private void start(long millidelay) {
		setSize(SCREENX, SCREENY);
		setLocation(20, 0);
		setBackground(Color.WHITE);
		setForeground(Color.black);
		setLayout(null);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		addMouseMotionListener(this);
		setVisible(true);

		/** MAIN LOOP */
		while (true) {
			repaint();

			if(model!=null)
			  model.rotate(0, 0.1f, 0);

			if (millidelay > 0) {
				try {
					Thread.sleep(millidelay);
				} catch (InterruptedException ie_ignore) {
				}
			}

			// List<Model3D> modelList = engine.getActiveModels();
			// for (Model3D model3d : modelList) {
			// model3d.rotate(new Point3D(), 0, 0, 0.01f);
			// }
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

				} else if (keycode == KeyEvent.VK_F2) {

				} else if (keycode == KeyEvent.VK_F3) {
					engine.shadingEnabled = !engine.shadingEnabled;
				}

				// CAM CONTROL
				else if (keycode == KeyEvent.VK_S) {
					camera.move(-1);
				} else if (keycode == KeyEvent.VK_A) {
					camera.move(-1, 0, 0);
				} else if (keycode == KeyEvent.VK_D) {
					camera.move(1, 0, 0);
				} else if (keycode == KeyEvent.VK_W) {
					camera.move(1);
				} else if (keycode == KeyEvent.VK_N) {
					camera.rotate(0, -7, 0);
				} else if (keycode == KeyEvent.VK_M) {
					camera.rotate(0, 7, 0);
				}

				else if (keycode == KeyEvent.VK_R) {
					initWorld();
				}

				else if (keycode == KeyEvent.VK_Y) {
					model.rotate(0, 1, 0);
				} else if (keycode == KeyEvent.VK_X) {
					model.rotate(0, -1, 0);
				}

				else if (keycode == KeyEvent.VK_C) {
					model.rotate(0, 0, 1);
				} else if (keycode == KeyEvent.VK_V) {
					model.rotate(0, 0, -1);
				}

				else if (keycode == KeyEvent.VK_O) {
					model.rotate(1, 0, 0);
				} else if (keycode == KeyEvent.VK_P) {
					model.rotate(-1, 0, 0);
				}

				else if (keycode == KeyEvent.VK_1) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Sodacan/soda.obj"), Color.GRAY);
					Shader shader = new NormalMappedTextureShader(lightsource, 
							TextureGenerator.getRGBRaster(new File("models/texturemapped/Sodacan/soda_can_1.jpg"), 1200, 1200),
							TextureGenerator.generateDefaultNormalMap(1200, 1200),
							TextureGenerator.getRGBRaster(new File("models/texturemapped/Sodacan/soda_can_spec.jpg"), 1200, 1200)							
							);
					engine.register(model, shader, false);
				} else if (keycode == KeyEvent.VK_2) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/House/JF.obj"), Color.GRAY);
					Model3DGenerator.normalizeOrigin(model);
					Shader shader = new TextureShader(lightsource, TextureGenerator
							.getRGBRaster(new File("models/texturemapped/House/house.jpg"), 4096, 4096));
					model.scale(5, 5, 5);					
					engine.register(model, shader, false);
				} else if (keycode == KeyEvent.VK_3) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Can/Can.obj"), Color.GRAY);
					Shader shader = new NormalMappedTextureShader(lightsource,
							TextureGenerator.getRGBRaster(new File("models/texturemapped/Can/can_CM.jpg"), 1024, 1024),
							TextureGenerator.getRGBRaster(new File("models/texturemapped/Can/can_NM.jpg"), 1024, 1024),
							TextureGenerator.getRGBRaster(new File("models/texturemapped/Can/can_SM.jpg"), 1024, 1024));
					engine.register(model, shader, false);
				} else if (keycode == KeyEvent.VK_4) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Radio/Radio.obj"), Color.GRAY);
					Shader shader = new TextureShader(lightsource, TextureGenerator
							.getRGBRaster(new File("models/texturemapped/Radio/TextureRadio.png"), 4096, 4096));
					engine.register(model, shader, false);
				} else if (keycode == KeyEvent.VK_5) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Barrel/Gunpowder_Keg.obj"),
							Color.GRAY);
					Shader shader = new NormalMappedTextureShader(lightsource,
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Barrel/Gunpowder_Keg_Diffuse.png"), 4096, 4096),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Barrel/Gunpowder_Keg_Normal.png"), 4096, 4096),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Barrel/Gunpowder_Keg_Specular.png"), 4096, 4096));
					model.setMaterial(Material.PEARL);
					engine.register(model, shader, false);
				} else if (keycode == KeyEvent.VK_6) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Turtle/Sea_Turtle_Stemcell.obj"),
							Color.GRAY);
					Shader shader = new NormalMappedTextureShader(lightsource,
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Turtle/Sea_Turtle_Diffuse.png"), 4096, 4096),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Turtle/Sea_Turtle_Normal.png"), 4096, 4096),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Turtle/Sea_Turtle_Specular.png"), 4096, 4096));
//					Shader shader = new TextureShader(lightsource,
//							TextureGenerator.getRGBRaster(
//									new File("models/texturemapped/Turtle/Sea_Turtle_Diffuse.png"), 4096, 4096));
					model.setMaterial(Material.PEARL);
					engine.register(model, shader, false);
				}
				else if (keycode == KeyEvent.VK_7) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Rock/Rock_6.obj"),
							Color.GRAY);
					Shader shader = new NormalMappedTextureShader(lightsource,
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Rock/Rock_6_d.png"), 2048, 2048),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Rock/Rock_6_n.png"), 2048, 2048),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Rock/Rock_6_s.png"), 2048, 2048));				
					engine.register(model, shader, false);
				}
				else if (keycode == KeyEvent.VK_8) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Woman/SYLT_Business_Wom-13_lowpoly.obj"),
							Color.GRAY);
					Shader shader = new NormalMappedTextureShader(lightsource,
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Woman/SYLT_Business_Wom-13_diffuse.jpg"), 4096, 4096),
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Woman/SYLT_Business_Wom-13_normal.tif"), 4096, 4096));
					Model3DGenerator.normalizeOrigin(model);
					engine.register(model, shader, false);
				}
				else if (keycode == KeyEvent.VK_9) {
					engine.deregister(model);
					model = WavefrontObjParser.parse(new File("models/texturemapped/Pistol/tt.obj"),
							Color.GRAY);
					Shader shader = new TextureShader(lightsource,
							TextureGenerator.getRGBRaster(
									new File("models/texturemapped/Pistol/tt_textures.jpg"), 512, 256));
					Model3DGenerator.normalizeOrigin(model);
					model.setMaterial(Material.PEARL);
					engine.register(model, shader, false);
				}
				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-5, 0, 0);
				} else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(5, 0, 0);
				} else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 5, 0);
				} else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, -5, 0);
				} else if (keycode == KeyEvent.VK_PAGE_UP) {
					lightsource.move(0, 0, 1);
				} else if (keycode == KeyEvent.VK_PAGE_DOWN) {
					lightsource.move(0, 0, -1);
				} else if (keycode == KeyEvent.VK_B) {
					Shader shader = engine.getDefaultShader();
					if (shader != null && shader instanceof TextureShader) {
						TextureShader textureShader = (TextureShader) shader;
						boolean isBiliinearFiltering = textureShader.isBilinearFilteringEnabled();
						textureShader.setIsBilinearFiltering(!isBiliinearFiltering);
					}
				}
			}
		}
		super.processKeyEvent(event);
	}

	public void paint(Graphics g) {
		long updateTime = System.currentTimeMillis();

		frame.clear();
		engine.drawScenes(frame);
		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);

		updateTime = System.currentTimeMillis() - updateTime;
		if (updateTime < 16) { // 33ms ~ 30 FPS
			try {
				Thread.sleep(16 - updateTime);
				updateTime = 16;
			} catch (InterruptedException ie) {
			}
		}

		counter++;
		if (counter % 20 == 0) {
			System.out.println("FPS: " + (1000 / updateTime));
		}

	}

	int counter;

	public static void main(String[] args) {
		TexturedModelViewer main = new TexturedModelViewer();
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