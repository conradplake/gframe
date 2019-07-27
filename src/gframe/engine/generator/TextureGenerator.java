package gframe.engine.generator;

import java.awt.Color;
import java.awt.Polygon;
import java.io.File;

import gframe.engine.ImageRaster;
import gframe.engine.TextureShader;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;
import graph.Graph;
import graph.Node;

public class TextureGenerator {

	
	private static final int DEFAULT_TILE_GAP_SIZE = 4;


	public static ImageRaster generateMengerSpongeTexture(ImageRaster raster, int pos_x, int pos_y, int width,
			int cubeColor, int gapColor) {

		if (width % 3 != 0) {
			System.out.println("TextureGenerator.generateMengerSpongeTexture: Warning! Parameter width=" + width
					+ " is no multiple of 3!");
		}

		int blocksize = width / 3;

		for (int s = 0; s < 3; s++) {
			for (int t = 0; t < 3; t++) {

				int c = cubeColor;
				if (s == 1 && t == 1) {
					c = gapColor;
				}

				if (blocksize % 3 == 0 && c == cubeColor) {
					generateMengerSpongeTexture(raster, pos_x + (s * blocksize), pos_y + (t * blocksize), blocksize,
							cubeColor, gapColor);
				} else {
					for (int x = 0; x < blocksize; x++) {
						for (int y = 0; y < blocksize; y++) {
							raster.setPixel(pos_x + (s * blocksize + x), pos_y + (t * blocksize + y), c);
						}
					}
				}
			}
		}

		return raster;
	}

	public static ImageRaster generateMengerSpongeNormalMap(int width) {
		ImageRaster normalMap = generateDefaultNormalMap(width, width);
		return generateMengerSpongeNormalMap(normalMap, 0, 0, width);
	}

	public static ImageRaster generateMengerSpongeNormalMap(ImageRaster raster, int pos_x, int pos_y, int width) {

		if (width % 3 != 0) {
			System.out.println("TextureGenerator.generateMengerSpongeNormalMap: Warning! Parameter width=" + width
					+ " is no multiple of 3!");
		}

		int blocksize = width / 3;

		Polygon left = new Polygon();
		left.addPoint(0, 0);
		left.addPoint(blocksize / 5, blocksize / 5);
		left.addPoint(blocksize / 5, 4 * blocksize / 5);
		left.addPoint(0, blocksize);

		Polygon right = new Polygon();
		right.addPoint(blocksize, 0);
		right.addPoint(4 * blocksize / 5, blocksize / 5);
		right.addPoint(4 * blocksize / 5, 4 * blocksize / 5);
		right.addPoint(blocksize, blocksize);

		Polygon up = new Polygon();
		up.addPoint(0, 0);
		up.addPoint(blocksize, 0);
		up.addPoint(4 * blocksize / 5, blocksize / 5);
		up.addPoint(blocksize / 5, blocksize / 5);

		Polygon down = new Polygon();
		down.addPoint(0, blocksize);
		down.addPoint(blocksize / 5, 4 * blocksize / 5);
		down.addPoint(4 * blocksize / 5, 4 * blocksize / 5);
		down.addPoint(blocksize, blocksize);

		for (int s = 0; s < 3; s++) {
			for (int t = 0; t < 3; t++) {

				boolean isGap = false;
				if (s == 1 && t == 1) {
					isGap = true;
				}

				if (blocksize % 3 == 0 && !isGap) { // tileable?
					generateMengerSpongeNormalMap(raster, pos_x + (s * blocksize), pos_y + (t * blocksize), blocksize);
				} else if (isGap) {

					for (int x = 0; x < blocksize; x++) {
						for (int y = 0; y < blocksize; y++) {

							Vector3D normal = new Vector3D(0, 0, 1); // default
																		// normal
																		// points
																		// to z
																		// direction
							int alpha = 255;

							if (blocksize < 5) {
								// normal =
								// Toolbox.getXrotMatrix(180).transform(normal);
								normal = new Vector3D();
								alpha = 0;
							} else {
								if (left.contains(x, y)) {
									normal = Toolbox.getYrotMatrix(-90).transform(normal);
									alpha = 250;
									if (blocksize < 10) {
										normal = Toolbox.getYrotMatrix(-60).transform(normal); // vector
																								// "überdrehen"
																								// um
																								// die
																								// helligkeit
																								// zu
																								// dimmen
										alpha = 0;
									}
								} else if (right.contains(x, y)) {
									normal = Toolbox.getYrotMatrix(90).transform(normal);
									alpha = 250;
									if (blocksize < 10) {
										normal = Toolbox.getYrotMatrix(60).transform(normal);
										alpha = 0;
									}
								} else if (up.contains(x, y)) {
									normal = Toolbox.getXrotMatrix(-90).transform(normal);
									alpha = 250;
									if (blocksize < 10) {
										normal = Toolbox.getXrotMatrix(-60).transform(normal);
										alpha = 0;
									}
								} else if (down.contains(x, y)) {
									normal = Toolbox.getXrotMatrix(90).transform(normal);
									alpha = 250;
									if (blocksize < 10) {
										normal = Toolbox.getXrotMatrix(60).transform(normal);
										alpha = 0;
									}
								} else {
									// normal =
									// Toolbox.getXrotMatrix(180).transform(normal);
									normal = new Vector3D();
								}
							}

							int col = toColor(normal, alpha);
							raster.setPixel(pos_x + (s * blocksize + x), pos_y + (t * blocksize + y), col);
						}
					}
				}
			}
		}

		return raster;
	}

	/*
	 * see also:
	 * https://fgiesen.wordpress.com/2010/03/28/how-to-generate-cellular-
	 * textures/
	 * 
	 * 
	 */
	public static ImageRaster generateVoronoiTexture(int w, int h, int numberOfCells, Color[] colorPalette) {
		ImageRaster result = new ImageRaster(w, h);

		if (colorPalette == null) {
			colorPalette = new Color[numberOfCells];
			for (int i = 0; i < numberOfCells; i++) {
				int random_r = (int) (Math.random() * 256);
				int random_g = (int) (Math.random() * 256);
				int random_b = (int) (Math.random() * 256);
				colorPalette[i] = new Color(random_r, random_g, random_b);
			}
		}

		// build mesh graph
		Graph graph = new Graph(w * h);

		long distToDirectNeighbor = 1000;
		long distToDiagonalNeighbor = 1414;
		// long distToDiagonalNeighbor = manhattanDistance? 2000 : 1414; // do
		// euclidian else

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				// long noise = (long)(100 *
				// NoiseGenerator.improvedPerlinNoise((float)w/x, (float)h/y,
				// 0));
				// System.out.println("noise = "+noise);
				long noise = 0L;

				int nodeId = 1 + y * w + x; // graph node ids start counting at
											// with 1, hence: 1 + ...

				// oben
				if (y > 0) {
					int nodeIdNeighbor = 1 + (y - 1) * w + x;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDirectNeighbor + noise);
				}

				// unten
				if (y < h - 1) {
					int nodeIdNeighbor = 1 + (y + 1) * w + x;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDirectNeighbor + noise);
				}

				// links
				if (x > 0) {
					int nodeIdNeighbor = 1 + (y) * w + x - 1;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDirectNeighbor + noise);
				}

				// rechts
				if (x < w - 1) {
					int nodeIdNeighbor = 1 + (y) * w + x + 1;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDirectNeighbor + noise);
				}

				// oben links
				if (y > 0 && x > 0) {
					int nodeIdNeighbor = 1 + (y - 1) * w + x - 1;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDiagonalNeighbor + noise);
				}

				// oben rechts
				if (y > 0 && x < w - 1) {
					int nodeIdNeighbor = 1 + (y - 1) * w + x + 1;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDiagonalNeighbor + noise);
				}

				// unten links
				if (y < h - 1 && x > 0) {
					int nodeIdNeighbor = 1 + (y + 1) * w + x - 1;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDiagonalNeighbor + noise);
				}

				// unten rechts
				if (y < h - 1 && x < w - 1) {
					int nodeIdNeighbor = 1 + (y + 1) * w + x + 1;
					graph.addEdge(nodeId, nodeIdNeighbor, distToDiagonalNeighbor + noise);
				}
			}
		}

		// randomly select terminal nodes (cells)
		int[] terminalNodeIds = new int[numberOfCells];
		for (int i = 0; i < numberOfCells; i++) {
			int random_x = (int) (Math.random() * w);
			int random_y = (int) (Math.random() * h);
			int nodeId = 1 + random_y * w + random_x;
			graph.setTerminal(nodeId, true);
			terminalNodeIds[i] = nodeId;
		}

		// color each node/pixel with nearest terminal's color
		// System.out.println(">> TextureGeneraor: computing voronoi areas");
		graph.markVoronoiAreas();
		// System.out.println("<< TextureGeneraor: computing voronoi areas");

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int nodeId = 1 + y * w + x;
				Node node = graph.getNode(nodeId);
				int cellNodeId = node.getColor();

				int colorIndex = -1;
				for (int i = 0; i < terminalNodeIds.length; i++) {
					if (terminalNodeIds[i] == cellNodeId) {
						colorIndex = i;
					}
				}
				Color color = colorPalette[colorIndex];
				result.setPixel(x, y, color.getRGB());
			}
		}

		return result;
	}

	
	public static ImageRaster generateTileTexture(int w, int h, int tilesize, int tileColor, int fugenColor) {
		return generateTileTexture(w, h, tilesize, DEFAULT_TILE_GAP_SIZE, tileColor, fugenColor);
	}
	
	public static ImageRaster generateTileTexture(int w, int h, int tilesize, int tileGapSize, int tileColor, int fugenColor) {

		ImageRaster result = new ImageRaster(w, h);

		int tile_size = tilesize;

		int numberOfRows = h / tile_size;
		int tilesPerRow = w / tile_size;

		for (int r = 0; r < numberOfRows; r++) {

			for (int t = 0; t < tilesPerRow; t++) {

				for (int y = 0; y < tile_size; y++) {
					for (int x = 0; x < tile_size; x++) {

						int col = tileColor;

						// if(Math.random()<0.0005){ // flecken :)
						// col = fugenColor.getRGB();
						// }
								
						if (x > tile_size - tileGapSize) {
							col = fugenColor;
						}
						else if(x < tileGapSize){
							col = fugenColor;
						}
						if (y > tile_size - tileGapSize) { // unterer rand
							col = fugenColor;							
						} else if (y < tileGapSize) { // oberer rand
							col = fugenColor;
						}
						
						// if(x%tile_size==0 || y%tile_size==0 || x==tile_size-1
						// || y==tile_size-1){ // fugen weiß
						// if(x%tile_size==0 || y%tile_size==0){ // fugen weiß
						//
						// col = ((255 & 0xFF) << 24) |
						// ((255 & 0xFF) << 16) |
						// ((255 & 0xFF) << 8) |
						// ((255 & 0xFF) << 0);
						//
						// if(Math.random()<0.02){ // fugendreck :)
						// col = ((255 & 0xFF) << 24) |
						// ((200 & 0xFF) << 16) |
						// ((200 & 0xFF) << 8) |
						// ((200 & 0xFF) << 0);
						// }
						//
						// }

						result.setPixel((t * tile_size) + x, (r * tile_size) + y, col);
					}
				}
			}

		}

		return result;

	}
	
	public static ImageRaster generateDiscoTileTexture(int w, int h, int tilesize) {

		ImageRaster result = new ImageRaster(w, h);

		int tile_size = tilesize;

		int numberOfRows = h / tile_size;
		int tilesPerRow = w / tile_size;

		for (int r = 0; r < numberOfRows; r++) {

			for (int t = 0; t < tilesPerRow; t++) {

				int col;
				int select = (int) (Math.random() * 6);
				switch (select) {
				case 0:
					col = Color.red.getRGB();
					break;
				case 1:
					col = Color.green.getRGB();
					break;
				case 2:
					col = Color.blue.getRGB();
					break;
				case 3:
					col = Color.yellow.getRGB();
					break;
				case 4:
					col = Color.cyan.getRGB();
					break;
				case 5:
					col = Color.magenta.getRGB();
					break;
				default:
					col = Color.black.getRGB();
					break;
				}

				for (int y = 0; y < tile_size; y++) {
					for (int x = 0; x < tile_size; x++) {
						result.setPixel((t * tile_size) + x, (r * tile_size) + y, col);
					}
				}
			}

		}

		return result;

	}

	
	public static ImageRaster generateTileTextureNormalMap(int w, int h, int tilesize) {
		return generateTileTextureNormalMap(w, h, tilesize, DEFAULT_TILE_GAP_SIZE);
	}
	
	public static ImageRaster generateTileTextureNormalMap(int w, int h, int tilesize, int tileGapSize) {

		ImageRaster result = generateDefaultNormalMap(w, h);

		int tile_size = tilesize;

		int numberOfRows = h / tile_size;
		int tilesPerRow = w / tile_size;

		for (int r = 0; r < numberOfRows; r++) {

			for (int t = 0; t < tilesPerRow; t++) {

				// random tile displacement (x and y rotation)
				float tile_deg_x = (float) Math.random() * 6 - 3;
				float tile_deg_y = (float) Math.random() * 6 - 3;

				for (int y = 0; y < tile_size; y++) {
					for (int x = 0; x < tile_size; x++) {

						Vector3D normal = new Vector3D(0, 0, 1); // default
																	// normal
																	// points to
																	// z
																	// direction

						boolean gap = false;
						
						if (x > tile_size - tileGapSize) {
							normal = Toolbox.getYrotMatrix(-20).transform(normal);
							gap = true;
						}
						else if(x < tileGapSize){
							normal = Toolbox.getYrotMatrix(20).transform(normal);
							gap = true;
						}
						if (y > tile_size - tileGapSize) { // unterer rand
							normal = Toolbox.getXrotMatrix(-20).transform(normal);
							gap = true;
						} else if (y < tileGapSize) { // oberer rand
							normal = Toolbox.getXrotMatrix(20).transform(normal);
							gap = true;
						}

						// add base displacement
						if (!gap)
							normal = Toolbox.getRotMatrix(tile_deg_x, tile_deg_y, 0).transform(normal);
						else {
							// normal.scale(0.8f); // dimm the light
						}

						int col = toColor(normal, gap ? 155 : 255);
//						int col = toColor(normal, 255);

						result.setPixel((t * tile_size) + x, (r * tile_size) + y, col);
					}
				}
			}

		}

		return result;
	}

	public static ImageRaster generateWoodTexture(int width, int height, int alpha) {

		float dimension_x = 4; // x=0 -> nur längsstreifen
		float dimension_y = 4; // regelt die streifenbreite

		ImageRaster result = new ImageRaster(width, height);

		for (int x = -width / 2; x < width / 2; x++) {
			float fx = (dimension_x / (float) width) * x; // x-wert
															// normalisieren

			for (int y = -height / 2; y < height / 2; y++) {
				float fy = (dimension_y / (float) height) * y; // y-wert
																// normalisieren

				float noise = (float) NoiseGenerator.improvedPerlinNoise(fx, fy, 0);

				int[] rgb = TextureGenerator.generateWoodColor(fx, fy, noise);

				int col = ((alpha & 0xFF) << 24) | ((rgb[0] & 0xFF) << 16) | ((rgb[1] & 0xFF) << 8)
						| ((rgb[2] & 0xFF) << 0);

				result.setPixel(x + width / 2, y + height / 2, col);
			}
		}

		return result;
	}

	public static int[] generateWoodColor(float pos_x, float pos_y, float noise) {

		int brown_min_red = 70;
		int brown_min_green = 44;
		int brown_min_blue = 30;

		int brown_max_red = 185;
		int brown_max_green = 122;
		int brown_max_blue = 87;

		float value = (pos_x * pos_x + pos_y * pos_y + noise * noise);
		// float value = (pos_x*pos_x + pos_y*pos_y + noise*noise);
		// float value = (Math.abs(pos_x) + Math.abs(pos_y) + noise*noise);

		value = value % 1;

		int r = brown_min_red + (int) (value * (brown_max_red - brown_min_red));
		int g = brown_min_green + (int) (value * (brown_max_green - brown_min_green));
		int b = brown_min_blue + (int) (value * (brown_max_blue - brown_min_blue));

		return new int[] { r, g, b };
	}

	public static ImageRaster generateDefaultNormalMap(int width, int height) {
		ImageRaster normalMap = new ImageRaster(width, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int rgbCode = ((255 & 0xFF) << 24) | ((128 & 0xFF) << 16) | ((128 & 0xFF) << 8) | ((255 & 0xFF) << 0);

				normalMap.setPixel(x, y, rgbCode);
			}
		}

		return normalMap;
	}

	
	public static ImageRaster generateNoiseTexture(float xoffset, float yoffset, int width, int height) {
		ImageRaster texture = new ImageRaster(width, height);
		
		float xinc = 0.0025f;
		float yinc = 0.0025f;
		
		float xoff = xinc*xoffset;
		for(int y=0;y<height;y++){
			float yoff = yinc*yoffset;
			for(int x=0;x<width;x++){
				float noise = (float)Toolbox.map(NoiseGenerator.improvedPerlinNoise(xoff, yoff), -1, 1, 0, 1);
				//float noise = (float)Toolbox.map(NoiseGenerator.improvedPerlinNoise(yoff, xoff), -1, 1, 0, 1);
				Color c = Color.getHSBColor(noise, 1, 1);
				texture.setPixel(x, y, c.getRGB());
				
				yoff += yinc;
			}	
			xoff +=xinc;
			
		}
		return texture;
	}
	
	private static final int toColor(Vector3D normalVector, int alpha) {
		int red = (int) (normalVector.x * 127) + 128;
		int green = (int) (normalVector.y * 127) + 128;
		int blue = (int) (normalVector.z * 127) + 128;

		int col = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0);

		return col;
	}

	
	public static ImageRaster getCharacterAsBitmap(char c){
		
		ImageRaster result = null;
		
		ImageRaster characterSet = TextureShader.getRGBRaster(new File("textures/courier_character_set_1024x1024.jpg"), 1024, 1024);
		
		int characterWidth  = 55;
		int characterHeight = 60;
	
		
		int xoff = 0;
		int yoff = 0;
		
		switch(c){
		case 'A': xoff =  18; yoff = 102; break;
		case 'B': xoff =  78; yoff = 102; break;		
		case 'C': xoff = 136; yoff = 102; break;				
		case 'D': xoff = 193; yoff = 102; break;		
		case 'E': xoff = 251; yoff = 102; break;
		case 'h': xoff = 425; yoff = 430; break;
		case 'O': xoff = 832; yoff = 102; break;
		case 'o': xoff = 832; yoff = 432; break;
		case 'P': xoff = 890; yoff = 102; break;		
		case ':': xoff = 658; yoff = 550; break;
		// TODO ...
		}
		
		result = new ImageRaster(characterWidth, characterHeight);
		
		for(int x=0;x<characterWidth;x++){
			for(int y=0;y<characterHeight;y++){			
				//result.setPixel(x, characterHeight-y-1, characterSet.getPixel(xoff+x, yoff+y));
				result.setPixel(x, y, characterSet.getPixel(xoff+x, yoff+y));
			}	
		}
		
		return result;	
	}
	
	public static ImageRaster getTextAsBitmap(String text, int paddingLeft, int paddingRight, int paddingTop, int paddingBottom){
		
		ImageRaster result = null;
		
		ImageRaster characterImage = getCharacterAsBitmap(' ');
		int charImageWith = characterImage.getWidth();
		int charImageHeight = characterImage.getHeight();
		
		char[] characters = text.toCharArray();
		result = new ImageRaster(paddingLeft + (charImageWith*characters.length) + paddingRight, paddingTop + charImageHeight + paddingBottom);
		
		for(int x=0;x<result.getWidth();x++){
			for(int y=0;y<result.getHeight();y++){
				result.setPixel(x, y, Color.WHITE.getRGB());
			}	
		}
		
		for(int i=0;i<characters.length;i++){
			characterImage = getCharacterAsBitmap(text.charAt(i));
			for(int x=0;x<characterImage.getWidth();x++){
				for(int y=0;y<characterImage.getHeight();y++){
					result.setPixel(paddingLeft + (i*charImageWith) + x, paddingBottom + y, characterImage.getPixel(x, y));
				}
			}
		}			
		
		result.inverse();
		
		return result;	
	}
}
