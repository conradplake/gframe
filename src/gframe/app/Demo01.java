package gframe.app;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gframe.DoubleBufferedFrame;
import gframe.Space3D;
import gframe.engine.Engine3D;
import gframe.engine.ImageRaster;
import gframe.engine.KeyPosition;
import gframe.engine.Lightsource;
import gframe.engine.Material;
import gframe.engine.NormalMappedMaterialShader;
import gframe.engine.Model3D;
import gframe.engine.NormalMappedTextureShader;
import gframe.engine.PhongShader;
import gframe.engine.Point3D;
import gframe.engine.Shader;
import gframe.engine.TextureShader;
import gframe.engine.Toolbox;
import gframe.engine.camera.Camera;
import gframe.engine.camera.TripodCamera;
import gframe.engine.generator.Model3DGenerator;
import gframe.engine.generator.NoiseGenerator;
import gframe.engine.generator.TextureGenerator;
import gframe.engine.shader.FlowFieldShader;
import gframe.engine.shader.SkyShader;
import gframe.engine.timing.CamShake;
import gframe.engine.timing.CamZoom;
import gframe.engine.timing.FadeOut;
import gframe.engine.timing.FadeOutFadeIn;
import gframe.engine.timing.MusicFadeOut;
import gframe.engine.timing.Rotate;
import gframe.engine.timing.Timed;
import gframe.engine.timing.Timer;
import gframe.parser.AnimationsParser;
import gframe.parser.WavefrontObjParser;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class Demo01 extends DoubleBufferedFrame implements MouseMotionListener {

	public Demo01() {
		super();
		setBackground(Color.lightGray);
		setResizable(false);
		frame = new ImageRaster(SCREENX, SCREENY);
		new JFXPanel(); // init java fx for audio playback
	}	

	public void start() {
				
		initEngine();
				
		isLoading = true;				
		Timed loader = new Timed() {
								
			@Override
			public void timePassedInMillis(long millis) {
				// here we load everything at once and ignore the millis
				
				// audio
				try{
				  cassius_i_love_you_so = new Media((new File("audio/Cassius-ILoveYouSoHq.mp3")).toURI().toString());
				  silent_shout = new Media((new File("audio/Silent_shout.mp3")).toURI().toString());
				}
				catch(MediaException me){
					System.out.println("Audio files missing?");
					System.exit(1);
				}
				
				//preload menger sponge into segment 1		
//				masterMengerCube = Model3DGenerator.buildMengerSponge(3, 6561, Color.blue);
//				engine.register(masterMengerCube, 1);

				oooohRaster = TextureShader.getRGBRaster(new File("./textures/height/Ooooh.png"), 400, 200);
				creditsRaster = TextureShader.getRGBRaster(new File("./textures/height/Credits.png"), 400, 200);
				fingerRaster = TextureShader.getRGBRaster(new File("./textures/diffuse/finger_400x200.png"), 400, 200);
				fingerRaster.inverse();				
				berlinRaster = TextureShader.getRGBRaster(new File("./textures/diffuse/berlin.jpg"), 160, 160);
				berlinRaster.inverse();
				graffitiRaster = TextureShader.getRGBRaster(new File("./textures/diffuse/politics_160x160.png"), 160, 160);
				graffitiRaster.inverse();							
				
				isLoading = false; // done
			}
			
			@Override
			public boolean done() {
				return !isLoading;
			}
		};
		Timer.getInstance().registerTimedObject(loader);
				
		
		// -- DISPLAY MODE SETTINGS		
//		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		if(device.isFullScreenSupported()){
//			DisplayMode newMode = new DisplayMode(SCREENX, SCREENY, 32, 60);
//			this.setUndecorated(true);
//			this.setResizable(false);
//			//this.setIgnoreRepaint(true);
//			device.setFullScreenWindow(this);			
//			device.setDisplayMode(newMode);			
//		}else{
			setSize(SCREENX, SCREENY);
			setLocation(20, 0);
			setLayout(null);
//		}
		
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		addMouseMotionListener(this);
		
		
		
		// ----
		
		showLoadingScreen();
		
		// test sequence 
		//startPart0();		
		
		// tiled wall
//		startPart1();
//		
//		// menger
//		startPart2();
		
		// metro station senefelder platz
		startPart3();
		
		// kuppel
		startPart7();
		
		// water scene
		startPart4();		
		
		// pool scene
		startPart5();

		System.exit(0);
	}

	private void exit() {
		System.exit(0);
	}

	private void initEngine() {
		engine = new Engine3D(2, SCREENX, SCREENY);

		// LIGHT SETTINGS
		Lightsource.AMBIENT_LIGHT_INTENSITY = 0;
		lightsource = new Lightsource(0, 0, 0, Color.WHITE, Lightsource.MAX_INTENSITY);
		engine.setLightsource(lightsource);

		engine.setDefaultShader(new TextureShader(lightsource,
				TextureShader.getRGBRaster(new File("textures/chessboard.jpg"), 256, 256)));		
	}

	

	private void initPart0_test_scene() {
		
		engine.register(Model3DGenerator.buildPlane(100, new Point3D(), Color.black));

		lightsource.x = 0;
		lightsource.y = 0;
		lightsource.z = -30;

		// SETUP AUDIO
		Media media = new Media((new File("audio/60_bpm.mp3")).toURI().toString());
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();

		// CAMERA SETTINGS
		camera = new Camera();
		camera.move(0, 0, -200);
		engine.setCamera(camera);

		// init camera key positions
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part0_camera.ani"),
				System.currentTimeMillis(), 1f);
	}


	private void initPart1_wall_flyby_scene() {	

		int textureWidth = 160;
		int textureHeight = 160;
		int tilesize = 20;

		Color floorTileColor = new Color(118, 206, 235);
//		Color floorTileColor = Color.BLUE;
		TextureShader wallShader = new NormalMappedTextureShader(lightsource,
				TextureGenerator.generateTileTexture(textureWidth, textureHeight, tilesize, floorTileColor.getRGB(), floorTileColor.darker().getRGB()),
				TextureGenerator.generateTileTextureNormalMap(textureWidth, textureHeight, tilesize), true);
		wallShader.setIsBilinearFiltering(false);
		
//		Shader wallShader = new MaterialShader(lightsource, TextureGenerator.generateTileTextureNormalMap(textureWidth, textureHeight, tilesize));
		
		
		
//		Color graffitiColor = new Color(220, 20, 20);		
//		TextureShader wallBerlinShader = new NormalMappedTextureShader(lightsource,
//				TextureGenerator.generateTileTexture(textureWidth, textureHeight, tilesize, floorTileColor.getRGB(), floorTileColor.darker().getRGB()),
//				TextureGenerator.generateTileTextureNormalMap(textureWidth, textureHeight, tilesize), true);		
//		wallBerlinShader.addEffect(berlinRaster, 100, graffitiColor);
//		
//		TextureShader wallGraffittiShader = new NormalMappedTextureShader(lightsource,
//				TextureGenerator.generateTileTexture(textureWidth, textureHeight, tilesize, floorTileColor.getRGB(), floorTileColor.darker().getRGB()),
//				TextureGenerator.generateTileTextureNormalMap(textureWidth, textureHeight, tilesize), true);		
//		//wallGraffittiShader.addEffect(graffitiRaster, 100, new Color(220, 220, 220));
//		wallGraffittiShader.addEffect(graffitiRaster, 100, graffitiColor);

		Model3D[] rooms = new Model3D[12];
		int roomcounter = 0;
		for (int i = 0; i < 10; i++) {
			Model3D room = Model3DGenerator.buildRoom(100, 100, 100, Color.white);			
			room = Model3DGenerator.facify(room);
			room.move(50, 0, i * (100));
			rooms[roomcounter++] = room;
		}
		Model3D extraroom1 = Model3DGenerator.buildRoom(100, 100, 100, Color.white);			
		extraroom1 = Model3DGenerator.facify(extraroom1);
		extraroom1.move(-50, 0, 9 * (100));
		rooms[roomcounter++] = extraroom1;
		Model3D extraroom2 = Model3DGenerator.buildRoom(100, 100, 100, Color.white);			
		extraroom2 = Model3DGenerator.facify(extraroom2);
		extraroom2.move(-150, 0, 9 * (100));
		rooms[roomcounter++] = extraroom2;
		
		Model3DGenerator.removeHiddenFaces(rooms);
		for (Model3D room : rooms) {
//			room.setMaterial(Material.TURQUOISE);
			engine.register(room, wallShader);
		}
		

		lightsource.x = 0;
		lightsource.y = 0;
		lightsource.z = -30;

		lightKeyPositions = AnimationsParser.parse(new File("animations/demo01/part1_light.ani"),
				System.currentTimeMillis(), 1f);
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part1_camera.ani"),
				System.currentTimeMillis(), 1f);

		events = new ArrayList<Event>();
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new FadeOutFadeIn(lightsource, 1, 8000)));
		
		// SETUP AUDIO		
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}		
		mediaPlayer = new MediaPlayer(silent_shout);

		// CAMERA SETTINGS
		camera = new Camera();
		camera.move(-30, 0, 100);
		camera.rotate(0, -90, 0);
		engine.setCamera(camera);
	}
	
	
	private void initPart2_menger_sponge_scene() {		

//		Color tileColor = new Color(118, 206, 235);
				
		//TextureShader shader = new NormalMappedTextureShader(lightsource, TextureShader.getRGBRaster(tileColor, 243, 243), TextureGenerator.generateMengerSpongeNormalMap(243));
		NormalMappedMaterialShader shader = new NormalMappedMaterialShader(lightsource, TextureGenerator.generateMengerSpongeNormalMap(243));
		shader.setAddSpecularity(false);
		shader.setIsBilinearFiltering(true);				
		masterMengerCube.setMaterial(Material.WHITE_PLASTIC);
		masterMengerCube.isVisible = false;
		
		engine.setDefaultShader(shader);
		engine.setActiveSegment(1);

		lightsource.x = 1200;
		lightsource.y = 1200;
		lightsource.z = -4000;
		lightsource.setIntensity(Lightsource.MAX_INTENSITY);

		// CAMERA SETTINGS
		camera = new Camera();
		camera.move(1200, 1200, -4000);
		engine.setCamera(camera);

//		lightKeyPositions = AnimationsParser.parse(new File("animations/demo01/part2_light.ani"),
//				System.currentTimeMillis(), 1f);
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part2_camera.ani"),
				System.currentTimeMillis(), 1f);
					

		events = new ArrayList<Event>();
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new FadeOutFadeIn(lightsource, 1, 2000)));
		events.add(new MarkVisible(System.currentTimeMillis() + 300, new Model3D[]{masterMengerCube}));
		
		events.add(new SparkTimedEvent(System.currentTimeMillis()+5000, new FadeOutFadeIn(lightsource, 500, 1000)));				
		events.add(new SparkTimedEvent(System.currentTimeMillis()+9500, new FadeOutFadeIn(lightsource, 500, 1000)));		
		events.add(new SparkTimedEvent(System.currentTimeMillis()+17500, new FadeOutFadeIn(lightsource, 500, 500)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+20500, new FadeOutFadeIn(lightsource, 500, 1000)));
		
		// clap
		events.add(new SparkTimedEvent(System.currentTimeMillis()+29000, new FadeOutFadeIn(lightsource, 500, 1800)));
		
		events.add(new SparkTimedEvent(System.currentTimeMillis()+35500, new FadeOutFadeIn(lightsource, 500, 1000)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+40000, new FadeOutFadeIn(lightsource, 500, 1000)));

		// clap
		events.add(new SparkTimedEvent(System.currentTimeMillis()+43900, new FadeOutFadeIn(lightsource, 500, 500)));
		
		events.add(new SparkTimedEvent(System.currentTimeMillis()+52000, new FadeOutFadeIn(lightsource, 500, 1000)));
		
		events.add(new SparkTimedEvent(System.currentTimeMillis()+59200, new FadeOut(lightsource, 400)));
				
	}
	
	
	private void initPart3_metro_scene() {
							
		Engine3D engine = new Engine3D(SCREENX, SCREENY);
		engine.setLightsource(lightsource);
									
		
		// floor
		int tileSize = 160;
		Color floorTileColor = new Color(118, 206, 235);
		TextureShader floorShader = new NormalMappedTextureShader(lightsource,				
				TextureGenerator.generateTileTexture(320, 320, tileSize, floorTileColor.getRGB(), floorTileColor.darker().getRGB()), TextureGenerator.generateTileTextureNormalMap(320, 320, tileSize));
		
		Model3D floor = Model3DGenerator.buildTiledFloor(10, 40, 50, Color.CYAN);	
		engine.register(floor, floorShader);
		
		
		// ceiling
		Model3D ceiling = Model3DGenerator.buildTiledFloor(10, 40, 50, Color.CYAN);
		ceiling.rotate(0, 0, 180);
		ceiling.move(-500,-500, 0);
		engine.register(ceiling, floorShader);		
		
		
		// right wall		
		TextureShader wallShader = new NormalMappedTextureShader(lightsource,				
				TextureGenerator.generateTileTexture(700, 700, 70, floorTileColor.getRGB(), Color.LIGHT_GRAY.darker().getRGB()), TextureGenerator.generateTileTextureNormalMap(700, 700, 70));
				
		wallShader.setIsBilinearFiltering(true);
		for (int i = 0; i < 10; i++) {
			Model3D wallSegment = Model3DGenerator.buildPlane(700, new Point3D(), Color.GREEN);			
			wallSegment.move(15*50, 250, i * (700));
			wallSegment.rotate(0, -90, 270);
			engine.register(wallSegment, wallShader);			
		}
		
		// station sign
		TextureShader signShader = new TextureShader(lightsource, new File("textures/senefelder_platz_1.jpg"), 703, 105);
		signShader.setIsBilinearFiltering(true);
		Model3D sign = Model3DGenerator.buildPlane(500, 70, new Point3D(), Color.GRAY);		
		sign.move(730, 150, 550);
		sign.rotate(0, -90, 0);
		engine.register(sign, signShader);
		
		
		
		// columns
		Color columnTileColor = new Color(118, 206, 235);
		TextureShader columnShader = new NormalMappedTextureShader(lightsource,
				TextureGenerator.generateTileTexture(40, 500, 10, columnTileColor.darker().getRGB(), columnTileColor.darker().getRGB()), TextureGenerator.generateTileTextureNormalMap(40, 500, 10));		
		
		Model3D column1a = Model3DGenerator.buildBlock(40, 250, 40, Color.GRAY);
		column1a = Model3DGenerator.facify(column1a);
		column1a.move(270, 125, 500);
		engine.register(column1a, columnShader);
		
		Model3D column1b = Model3DGenerator.buildBlock(40, 250, 40, Color.GRAY);
		column1b = Model3DGenerator.facify(column1b);
		column1b.move(270, 375, 500);
		engine.register(column1b, columnShader);
		
		Model3D column2 = Model3DGenerator.buildBlock(40, 500, 40, Color.GRAY);
		column2 = Model3DGenerator.facify(column2);
		column2.move(270, 250, 1000);
		engine.register(column2, columnShader);
		
		Model3D column3 = Model3DGenerator.buildBlock(40, 500, 40, Color.GRAY);
		column3 = Model3DGenerator.facify(column3);
		column3.move(270, 250, 1500);
		engine.register(column3, columnShader);
		
		
		
		// litter bin
		Model3D litterBin = WavefrontObjParser.parse(new File("models/FREE_TRASHCAN_2_OBJ.obj"), Color.ORANGE.darker());				
		float scaleFactor = (float)Space3D.ONE_METER / (2*litterBin.getBoundingSphereRadius());
		litterBin.scale(scaleFactor, scaleFactor, scaleFactor);
		litterBin.move(270, 0.5f, 465);
		
		engine.register(litterBin, false);
		Shader litterBinShader = new PhongShader(lightsource);
		engine.setModelShader(litterBin, litterBinShader);
		
		
		// bench
		Shader benchShader = new PhongShader(lightsource);
		Color benchColor = new Color(138, 84, 45);
		Model3D bench = WavefrontObjParser.parse(new File("models/outdoor/bench_v01.obj"), benchColor);
		bench.move(270, 0, 310);
		bench.rotate(0, 180, 0);
		bench.scale(0.8f, 0.8f, 0.8f);
		engine.register(bench, false);		
		engine.setModelShader(bench, benchShader);
		
		
		Model3D bench2 = WavefrontObjParser.parse(new File("models/outdoor/bench_v01.obj"), benchColor);
		bench2.move(270, 0, 1200);
		bench2.scale(0.8f, 0.8f, 0.8f);
		engine.register(bench2, false);		
		engine.setModelShader(bench2, benchShader);
				
				
		// coffee cups
		Shader cupOnBench1Shader = new PhongShader(lightsource);
		Model3D cupOnBench1 = WavefrontObjParser.parse(new File("models/Coca-Cola Cup.obj"), Color.WHITE);
		cupOnBench1.move(270, 38, 380);
		//cupOnBench1.scale(0.8f, 0.8f, 0.8f);
		engine.register(cupOnBench1, false);		
		engine.setModelShader(cupOnBench1, cupOnBench1Shader);
		
		Model3D cupNextToBin = WavefrontObjParser.parse(new File("models/Coca-Cola Cup.obj"), Color.WHITE);
		cupNextToBin.move(240, 3, 450);
		cupNextToBin.rotate(85, 0, 0);
		//cupOnBench1.scale(0.8f, 0.8f, 0.8f);
		engine.register(cupNextToBin, false);		
		engine.setModelShader(cupNextToBin, cupOnBench1Shader);
				
		
		// CAMERA & LIGHT SETTINGS
		camera = new TripodCamera();
		camera.move(50, 50, -100);
		engine.setCamera(camera);
		
		lightsource.x = 0;
		lightsource.y = 100;
		lightsource.z = -100;
		lightsource.setIntensity(Lightsource.MAX_INTENSITY);
		
		lightsource.setShadowsEnabled(true);
		
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part3_camera.ani"),
				System.currentTimeMillis(), 1f);
		
		lightKeyPositions = AnimationsParser.parse(new File("animations/demo01/part3_light.ani"), System.currentTimeMillis(), 1f);
		
		
		events = new ArrayList<Event>();
				
		events.add(new SparkTimedEvent(System.currentTimeMillis()+3500, new FadeOutFadeIn(lightsource, 500, 1800)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+6500, new FadeOutFadeIn(lightsource, 500, 1500)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+9500, new FadeOutFadeIn(lightsource, 500, 1500)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+12500, new FadeOutFadeIn(lightsource, 500, 1800)));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+15800, new FadeOutFadeIn(lightsource, 200, 1800)));	
				
		shadowsEnabled = true;
					
		this.engine = engine;		
		System.gc();
	}
	
	
	private void initPart4_infinite_water_scene(){
		
		Engine3D engine = new Engine3D(SCREENX, SCREENY);
		engine.setLightsource(lightsource);
		
		
		Model3D skydome = Model3DGenerator.buildSkydome(70000, Color.BLUE);		
		Shader skydomeShader = new SkyShader(lightsource, skydome);
		engine.register(skydome, skydomeShader);		
			
		Shader terrainShader = new PhongShader(lightsource);//				
		Model3D terrainMesh = Model3DGenerator.buildFlatMesh(new FlyoverTerrain(100, 100), 100, 100, Space3D.ONE_METER, new Color(120, 150, 255));				
		terrainMesh.move(-5000, 0, 0);		
		engine.register(terrainMesh, terrainShader);
		
		camera = new TripodCamera();
		camera.move(0, 1000, -100);
		engine.setCamera(camera);
		
		cameraKeyPositions = new ArrayList<KeyPosition>();
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part4_camera.ani"),	System.currentTimeMillis(), 1f);		
		lightKeyPositions = new ArrayList<KeyPosition>();						
		events = new ArrayList<Event>();
		
		lightsource.x = 0;
		lightsource.y = 10000;
		lightsource.z = 500;
		
		lightsource.setIntensity(Lightsource.MAX_INTENSITY);		
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new FadeOutFadeIn(lightsource, 1, 1000)));		
		lightsource.setIntensity(0);
		
		Color lightskyblue = new Color(135, 206, 250);
		events.add(new ChangeBackgroundColor(System.currentTimeMillis()+3000, lightskyblue));
		
		if(mediaPlayer!=null){
			events.add(new SparkTimedEvent(System.currentTimeMillis()+20000, new MusicFadeOut(mediaPlayer, 4000)));	
		}							
		
		shadowsEnabled = false;										
		
		this.engine = engine;
		System.gc();
	}
	
	
	
	private void initPart5_pool_scene(){
		
		Engine3D engine = new Engine3D(SCREENX, SCREENY);
		engine.setLightsource(lightsource);												
		
		int tileSize = 40;
		Color floorTileColor = new Color(118, 206, 235);
		TextureShader floorShader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateTileTexture(320, 320, tileSize, floorTileColor.getRGB(), floorTileColor.darker().getRGB()), TextureGenerator.generateTileTextureNormalMap(320, 320, tileSize));
		floorShader.setIsBilinearFiltering(false);
		//TextureShader floorShader = new NormalMappedTextureShader(lightsource, TextureGenerator.generateTileTexture(320, 320, tileSize*2, floorTileColor.getRGB(), floorTileColor.darker().getRGB()), TextureGenerator.generateTileTextureNormalMap(320, 320, tileSize*2));
		
		Model3D floor = Model3DGenerator.buildTiledFloor(4, 4, tileSize, Color.BLUE);
		engine.register(floor, floorShader);			
		
		Model3D ceiling = Model3DGenerator.buildTiledFloor(4, 4, tileSize, Color.BLUE);			
		ceiling.move(0, 2*tileSize, 4*tileSize);
		ceiling.rotate(180, 0, 0);	
		engine.register(ceiling, floorShader);
		
		Model3D backwall = Model3DGenerator.buildTiledFloor(4, 2, tileSize, Color.BLUE);
		backwall.move(0, 0, 4*tileSize);
		backwall.rotate(90, 0, 0);				
		engine.register(backwall, floorShader);
		
		Model3D rightwall = Model3DGenerator.buildTiledFloor(4, 2, tileSize, Color.BLUE);		
		rightwall.move(4*tileSize, 0, 4*tileSize);
		rightwall.rotate(90, 0, 90);				
		engine.register(rightwall, floorShader);
		
		Model3D leftwall = Model3DGenerator.buildTiledFloor(4, 2, tileSize, Color.BLUE);		
		leftwall.move(0, 0, 0);
		leftwall.rotate(90, 0, -90);				
		engine.register(leftwall, floorShader);
		

		// the wide screen
		FlowFieldShader flowFieldShader = new FlowFieldShader(lightsource, 10000);
		Model3D screen = Model3DGenerator.buildPlane(130, 35, new Point3D(80, 30, 140), Color.black);
		screen.rotate(0, 0, 180);		
		engine.register(screen, flowFieldShader);
				
		camera = new TripodCamera();
		camera.move(0, 40, -100);
		engine.setCamera(camera);
		
		
		lightsource.x = 70;
		lightsource.y = 50;
		lightsource.z = 50;
		lightsource.setIntensity(Lightsource.MAX_INTENSITY);
		
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part5_camera.ani"), System.currentTimeMillis());
				
		events = new ArrayList<Event>();
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new FadeOutFadeIn(lightsource, 1, 2000)));		
													
		
		// SETUP AUDIO
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}		
		mediaPlayer = new MediaPlayer(cassius_i_love_you_so);
		
		events.add(new SetFlowFieldParticlePositions(System.currentTimeMillis()+15900, flowFieldShader, oooohRaster));
		events.add(new SetFlowFieldParticlePositions(System.currentTimeMillis()+23500, flowFieldShader, oooohRaster));
		events.add(new SetFlowFieldParticlePositions(System.currentTimeMillis()+31050, flowFieldShader, oooohRaster));
		events.add(new SetFlowFieldParticlePositions(System.currentTimeMillis()+38750, flowFieldShader, oooohRaster));

		// fade out
		events.add(new SparkTimedEvent(System.currentTimeMillis()+47000, new MusicFadeOut(mediaPlayer, 9000)));		
		
		//credits
		events.add(new SetFlowFieldParticlePositions(System.currentTimeMillis()+50000, flowFieldShader, creditsRaster));
		events.add(new SparkTimedEvent(System.currentTimeMillis()+52000, new FadeOut(lightsource, 5000)));
		events.add(new SetFlowFieldParticlePositions(System.currentTimeMillis()+54000, flowFieldShader, fingerRaster));
		
		
		// fade in from pitch black
		lightsource.setIntensity(0);
		shadowsEnabled = false;
		
		this.engine = engine;
		System.gc();
	}
	
	
	private void initPart7_cupola_scene(){
		
		Engine3D engine = new Engine3D(SCREENX, SCREENY);
		engine.setLightsource(lightsource);
		
		Model3D cupola = WavefrontObjParser.parseGeometry(new File("./models/structures/nuraghe_inside.obj"), Color.ORANGE);
		//cupola = Model3DGenerator.split(cupola);
//		((Model3D)cupola.getChildren().get(465)).isVisible = false;
		
		cupola.scale(10, 10, 10);
		cupola.rotate(-90, 0, 0);
				
		engine.register(cupola, new PhongShader(lightsource));
		
		camera = new TripodCamera();		
		camera.move(0, 0, -100);
		engine.setCamera(camera);
		
		lightsource.x = 0;
		lightsource.y = 0;
		lightsource.z = 40;
//		lightsource.setIntensity(Lightsource.MAX_INTENSITY);
		
		
		cameraKeyPositions = AnimationsParser.parse(new File("animations/demo01/part7_camera.ani"),
				System.currentTimeMillis(), 1f);		
				
		events = new ArrayList<Event>();		
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new FadeOutFadeIn(lightsource, 1f, 1, 7000)));
		events.add(new SparkTimedEvent(System.currentTimeMillis(), new Rotate(cupola, 20000, 0.02f, Rotate.AXIS_Y)));		
		events.add(new SparkTimedEvent(System.currentTimeMillis()+18800, new FadeOut(lightsource, 1f, 1000)));
		
		shadowsEnabled = false;		
								
		this.engine = engine;
		System.gc();
	}
	
	
	
	private void showLoadingScreen(){
		
		setBackground(Color.black);
		setForeground(Color.black);
		setVisible(true);
				
		while(isLoading){				
			try {
				Thread.sleep(500);								
				Color  c = Color.getHSBColor((float)Math.random(), 1, 0.5f);
				setBackground(c);
			} catch (InterruptedException ie_ignore) {
			}	
		}	
	}
	
	
	private void startPart0() {

		initPart0_test_scene();

		setBackground(Color.black);
		setForeground(Color.black);
		setVisible(true);

		long lastTime = System.currentTimeMillis();
		while (true) {
			repaint();			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}	

			long currentTime = System.currentTimeMillis();
			if (!cameraKeyPositions.isEmpty()) {
				KeyPosition nextCameraPosition = cameraKeyPositions.get(0);

				nextCameraPosition.updatePosition(lastTime, currentTime, camera);
				nextCameraPosition.updatePosition(lastTime, currentTime, lightsource);
				if (currentTime >= nextCameraPosition.getTimestamp()) {
					cameraKeyPositions.remove(0);
				}
			} else {
				break;
			}
			lastTime = currentTime;
		}
	}
	
	
	private void startPart1() {

		initPart1_wall_flyby_scene();

		setBackground(Color.black);
		setForeground(Color.black);
		setVisible(true);

		mediaPlayer.play();
		
		long lastTime = System.currentTimeMillis();

		//Graphics graphics = this.getGraphics();
		while (true) {
			repaint();
			//update(this.getGraphics());
			//update(graphics);

			//Thread.yield();
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}

			long currentTime = System.currentTimeMillis();
			
			

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
				nextPosition.updatePosition(lastTime, currentTime, lightsource);
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
			
			if (allKeyPositionsReached) {
				lightsource.setIntensity(0);						
				break;
			}

			lastTime = currentTime;		
		}				
	}

	
	private void startPart2() {

		initPart2_menger_sponge_scene();
		
		setBackground(Color.black);
		setForeground(Color.black);
		setVisible(true);		
		
		long lastTime = System.currentTimeMillis();
		while (true) {
			repaint();			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}
			
			long currentTime = System.currentTimeMillis();
			
			boolean allKeyPositionsReached = true;
//			if (!lightKeyPositions.isEmpty()) {
//				allKeyPositionsReached = false;
//				KeyPosition nextPosition = lightKeyPositions.get(0);
//				nextPosition.updatePosition(lastTime, currentTime, lightsource);
//				if (currentTime >= nextPosition.getTimestamp()) {
//					lightKeyPositions.remove(0);
//				}
//			}			
			if (!cameraKeyPositions.isEmpty()) {
				allKeyPositionsReached = false;
				KeyPosition nextPosition = cameraKeyPositions.get(0);
				nextPosition.updatePosition(lastTime, currentTime, camera);
				nextPosition.updatePosition(lastTime, currentTime, lightsource);
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
			
			if (allKeyPositionsReached) {
//				lightsource.setIntensity(Math.max(0f, lightsource.getIntensity()-0.01f));
//				if(lightsource.getIntensity()<=0.001){
//					lightsource.setIntensity(0);
//					break;
//				}
				lightsource.setIntensity(0);						
				break;
			}
			
			lastTime = currentTime;
		}
			
	}
	
	
	private void startPart3() {
				
		initPart3_metro_scene();

		setBackground(Color.black);
		setForeground(Color.black);						
		setVisible(true);
						
		long lastTime = System.currentTimeMillis();
		
		while (true) {
			repaint();			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}			
			
			
			long currentTime = System.currentTimeMillis();
			
			boolean allKeyPositionsReached = true;
			if (!lightKeyPositions.isEmpty()) {
				allKeyPositionsReached = false;
				KeyPosition nextPosition = lightKeyPositions.get(0);
				
				nextPosition.updatePosition(lastTime, currentTime, lightsource);
				nextPosition.updatePosition(lastTime, currentTime, lightsource.getMatrix());
								
				// TODO: alle änderungen über eine generelle schnittstelle bekannt machen
				lightsource.recomputeLightZ();				
				lightsource.recomputeInverse(); 
				
				engine.recomputeShadowMaps();
				
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
			
			if (allKeyPositionsReached) {
//				speed += lightsourceMoveOutAcc;
//				lightsource.move(0, 0, speed);
//				engine.recomputeShadowMaps();
//				if(lightsource.z>2000){
//					lightsource.setIntensity(0);
//					break;
//				}
				lightsource.setIntensity(0);
				lightsource.setShadowsEnabled(false);
				break;
			}
			
			lastTime = currentTime;			
		}
	}
	
	
	private void startPart4(){
		
		initPart4_infinite_water_scene();
						
		setBackground(Color.black);
		setForeground(Color.black);
		
		setVisible(true);

						
		long lastTime = System.currentTimeMillis();
		while (true) {
			repaint();			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}			
			
			
			long currentTime = System.currentTimeMillis();
			
			boolean allKeyPositionsReached = true;
			if (!lightKeyPositions.isEmpty()) {
				allKeyPositionsReached = false;
				KeyPosition nextPosition = lightKeyPositions.get(0);
				
				nextPosition.updatePosition(lastTime, currentTime, lightsource);
				nextPosition.updatePosition(lastTime, currentTime, lightsource.getMatrix());
				
				lightsource.recomputeInverse(); // TODO: alle änderungen über eine generelle schnittstelle bekannt machen								
				engine.recomputeShadowMaps();
				
				if (currentTime >= nextPosition.getTimestamp()) {
					lightKeyPositions.remove(0);
				}
			}			
			if (!cameraKeyPositions.isEmpty()) {
				KeyPosition nextPosition = cameraKeyPositions.get(0);
				allKeyPositionsReached = false;
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
			
			if (allKeyPositionsReached) {								
				lightsource.setIntensity(0);
				break;				
			}
			
			lastTime = currentTime;			
		}
		
	}
	
	
	private void startPart5(){
		
		initPart5_pool_scene();
		
		//Color lightskyblue = new Color(135, 206, 250);		
		setBackground(Color.black);
		setForeground(Color.black);
		
		setVisible(true);

		mediaPlayer.play();
		
		long lastTime = System.currentTimeMillis();
		while (true) {
			repaint();			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}			
			
			
			long currentTime = System.currentTimeMillis();
			
			boolean allKeyPositionsReached = true;
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
	
	
	private void startPart7(){
		
		initPart7_cupola_scene();
		
			
		setBackground(Color.black);
		setForeground(Color.black); 
		
		setVisible(true);

						
		long lastTime = System.currentTimeMillis();
		while (true) {
			repaint();			
			try {
				Thread.sleep(2);
			} catch (InterruptedException ie_ignore) {
			}			
			
			
			long currentTime = System.currentTimeMillis();
			
			boolean allKeyPositionsReached = true;
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
			
			if (allKeyPositionsReached) {								
				//lightsource.setIntensity(0);
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
			
//			lightsource.setCoordinates(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z);
		} else {
			int distX = e.getX() - mouseX;
			int distY = e.getY() - mouseY;
			
			if(camera!=null)
			  camera.rotate(distY / 3, -distX / 3, 0);
			// camera.rotate(0, distX/3, 0);
			
//			if(engine.getDirectionalLight()!=null)
//			  engine.getDirectionalLight().rotate(distY / 3, -distX / 3, 0);
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
						fw = new FileWriter("animations/demo01/camera_current.txt", true);
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

				} else if (keycode == KeyEvent.VK_F3) {
					engine.shadingEnabled = !engine.shadingEnabled;
				}			
				
				else if (keycode == KeyEvent.VK_N) {
					camera.rotate(0, -1, 0);
				} else if (keycode == KeyEvent.VK_M) {
					camera.rotate(0, 1, 0);
				} else if (keycode == KeyEvent.VK_R) {
					initPart7_cupola_scene();	
				} else if (keycode == KeyEvent.VK_P) {
					shadowsEnabled=!shadowsEnabled;
					engine.recomputeShadowMaps();
				}	
				else if (keycode == KeyEvent.VK_O) {
					engine.depthTestCuller.disabled = !engine.depthTestCuller.disabled;
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

		if(isLoading){			
			// draw loading screen
		}
		else{			
			if(shadowsEnabled){
				engine.drawShadowedScene(frame);
			}else{
				engine.drawScene(frame);
			}			
		}
		
		g.drawImage(frame.createImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);
				
		updateTime = System.currentTimeMillis() - updateTime;
		if (updateTime < 33) { // cap at 33ms ~ 30 FPS
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
		Demo01 demo = new Demo01();
		demo.start();
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

	
	public class MarkVisible extends Event{				
		Model3D[] models;
		
		public MarkVisible(long timestamp, Model3D[] models) {
			this.timestamp = timestamp;
			this.models = models;
		}

		@Override
		public void action() {			
			for (Model3D model : models) {
				model.isVisible = true;
			}
		}		
	}
	
	
	public class GarbageCollect extends Event{
		public GarbageCollect(long timestamp) {
			this.timestamp = timestamp;
		}
		@Override
		public void action() {			
			System.gc();
		}		
	}
		
	
	public class ChangeBackgroundColor extends Event{
		Color c;
		public ChangeBackgroundColor(long timestamp, Color c) {
			this.timestamp = timestamp;
			this.c = c;
		}
		@Override
		public void action() {			
			setBackground(c);
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
	
	
	public class SetFlowFieldParticlePositions extends Event{

		private FlowFieldShader flowFieldShader;
		ImageRaster positionTexture;
	
		public SetFlowFieldParticlePositions(long timestamp, FlowFieldShader flowFieldShader, ImageRaster positionTexture){
			this.timestamp = timestamp;
			this.flowFieldShader = flowFieldShader;
			this.positionTexture = positionTexture;
		}
		
		@Override
		public void action() {
			flowFieldShader.setParticlePositions(positionTexture);			
		}
		
	}
	
	public class FlyoverTerrain extends Model3D{
		
		long timePassedInMillis;
		long lastTimeInMillis;
		long timestepInMillis = 30;
				
		private int meshWidth;
		private int meshHeight;
		
		private float xinc;
		private float yinc;
		
		float flyingOffset;
		
		public FlyoverTerrain(int meshWidth, int meshHeight){
			super();
			
			this.meshWidth = meshWidth;
			this.meshHeight = meshHeight;		
			
			this.xinc = 10f / meshWidth;
			this.yinc = 10f / meshHeight;
			
			lastTimeInMillis = System.currentTimeMillis();
		}
		
		
		private void setHeights(){			
			float yoff = flyingOffset;			
			for(int y=0;y<meshHeight;y++){				
				//float xoff = -flyingOffset;
				float xoff = 0;
				for(int x=0;x<meshWidth;x++){				
					Point3D vertex = getVertices().get( y * meshWidth + x );
					float noise = (float)Toolbox.map(NoiseGenerator.improvedPerlinNoise(xoff, yoff), -1, 1, 0, Space3D.ONE_METER*10);
					vertex.y = noise;
					xoff +=xinc;
				}			
				yoff += yinc;
			}			
			recomputeFaceNormals();
			computeVertexNormals();
		}
		
		
		@Override
		public void preDraw(){			
			long currentTimeInMillis = System.currentTimeMillis();
			timePassedInMillis += (currentTimeInMillis - lastTimeInMillis);		
			while (timePassedInMillis > timestepInMillis) {
				timePassedInMillis-=timestepInMillis;
				flyingOffset += 0.04;				
				setHeights();								
			}			
			lastTimeInMillis = currentTimeInMillis;
		}		
	}
	
	
	
	
	// -----------------------------------------------
	
	
	
	private boolean isLoading = false;
	
	private Engine3D engine;

	private ImageRaster frame;

	
	private static boolean shadowsEnabled = false;
	
	
	Media cassius_i_love_you_so;
	Media silent_shout;
	
	Model3D masterMengerCube;	
	ImageRaster oooohRaster;
	ImageRaster creditsRaster;
	ImageRaster fingerRaster;
	ImageRaster berlinRaster;
	ImageRaster graffitiRaster;
	
	long fpsCounter;

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