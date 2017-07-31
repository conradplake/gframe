package gframe.engine.shader;


import java.awt.Color;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.RenderFace;
import imaging.ImageRaster;


/**
 * See tutorial at: http://www.karlsims.com/rd.html
 * 
 * */
public class ReactionDiffusionShader extends AbstractShader {

	long lastTime = 0;
	long timePassed = 0;
	
	int textureWidth = 200;
	int textureHeight = 200;
	
	ImageRaster texture;
		
	Cell[][] grid;
	Cell[][] next;
	
	
	float dA = 1.0f; // diffusion rate of chemical A
	float dB = 0.5f; // diffusion rate of chemical B
	
	float feed = 0.055f; // feed rate of B
	float kill = 0.062f; // kill rate of B
	
	// mitosis simulation
//	float feed = 0.0367f; 
//	float kill = 0.0649f;
	
	// coral growth (needs circle seed)
//	float feed = 0.0545f; 
//	float kill = 0.062f; 
	
	// have it hard coded as a function
//	float[][] laplaceKernel = new float[][]{{0.05f, 0.2f, 0.05f},
//											{0.2f, -1, 0.2f},
//											{0.05f, 0.2f, 0.05f}};
	

	public ReactionDiffusionShader(Lightsource lightsource){
		super(lightsource);
		texture = new ImageRaster(textureWidth, textureHeight);
		lastTime = System.currentTimeMillis();
		
		this.grid = new Cell[textureWidth][textureHeight];
		this.next = new Cell[textureWidth][textureHeight];
		
		// make a start grid with chemical A only
		for(int x=0;x<textureWidth;x++){
			for(int y=0;y<textureHeight;y++){
				grid[x][y] = new Cell(1, 0);
				next[x][y] = new Cell(1, 0);
			}	
		}		
		
		// put some seed Bs		
		//for(int i=0;i<10;i++){
		
			int mx = textureWidth/2;
			int my = textureHeight/2;
//			int mx = (int)(Math.random()*textureWidth);
//			int my = (int)(Math.random()*textureHeight);					
			
			for(int x=mx-10;x<mx+10;x++){
				for(int y=my-10;y<my+10;y++){
					grid[x][y] = new Cell(0, 1);				
				}	
			}
//		}
		
			
	}
	
	
	@Override
	public void preShade(RenderFace renderFace) {
		
		long currentTime = System.currentTimeMillis();

		timePassed += (currentTime - lastTime);

		while (timePassed > 20) {
			
			// generate next grid state
			for (int x = 1; x < textureWidth-1; x++) {
				for (int y = 1; y < textureHeight-1; y++) {
															
					Cell cell = grid[x][y];
					
					float a = cell.chemicalA;
					float b = cell.chemicalB;
					
					float newA = a + (dA * laplaceA(x, y)) - (a*b*b) + (feed*(1-a));
					float newB = b + (dB * laplaceB(x, y)) + (a*b*b) - ((feed+kill)*b); 
					
					next[x][y].chemicalA = newA;
					next[x][y].chemicalB = newB; 		
					
					// update color
					int r = (int)(newA*255);					
					if(r<0)r=0;
					if(r>255)r=255;
					
					int g = 0;
					
					int blue = (int)(newB*255);					
					if(blue<0)blue=0;
					if(blue>255)blue=255;
					
					Color c = new Color(r, g, blue);					
					texture.setPixel(x, y, c.getRGB());	
				}
			}
			
			//swap grids
			Cell[][] temp = grid;
			grid = next;
			next = temp;
			
			timePassed -= 20;
		}

		lastTime = currentTime;
	};


	private float laplaceA(int x, int y) {
		float sumA = 0;
		
		// center
		sumA += grid[x][y].chemicalA * -1;
		
		// neighbors
		sumA += grid[x-1][y].chemicalA * 0.2f;
		sumA += grid[x+1][y].chemicalA * 0.2f;
		sumA += grid[x][y-1].chemicalA * 0.2f;
		sumA += grid[x][y+1].chemicalA * 0.2f;
		
		// diagonals
		sumA += grid[x+1][y+1].chemicalA * 0.05f;
		sumA += grid[x+1][y-1].chemicalA * 0.05f;
		sumA += grid[x-1][y+1].chemicalA * 0.05f;
		sumA += grid[x-1][y-1].chemicalA * 0.05f;
		
		return sumA;
	}

	
	private float laplaceB(int x, int y) {
		float sumB = 0;		
		
		// center
		sumB += grid[x][y].chemicalB * -1;
		
		// neighbors
		sumB += grid[x-1][y].chemicalB * 0.2f;
		sumB += grid[x+1][y].chemicalB * 0.2f;
		sumB += grid[x][y-1].chemicalB * 0.2f;
		sumB += grid[x][y+1].chemicalB * 0.2f;
		
		// diagonals
		sumB += grid[x+1][y+1].chemicalB * 0.05f;
		sumB += grid[x+1][y-1].chemicalB * 0.05f;
		sumB += grid[x-1][y+1].chemicalB * 0.05f;
		sumB += grid[x-1][y-1].chemicalB * 0.05f;
		
		return sumB;
	}
	

	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, float texel_u, float texel_v, int screen_x, int screen_y) {
				
				
		float x = Math.min(textureWidth-1, texel_u*(textureWidth));
		float y = Math.min(textureHeight-1, texel_v*(textureHeight));
		
		int texel = texture.getPixel((int)x, (int)y);		
				
		int red = (texel >> 16) & 0xff;
		int green = (texel >> 8) & 0xff;
		int blue = (texel) & 0xff;
		
		return  ((renderFace.getColor().getAlpha() & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8)  |
                ((blue & 0xFF) << 0);
	}
	

	
	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}
	
	
	
	public class Cell{
		
		public Cell(float a, float b){
			this.chemicalA = a;
			this.chemicalB = b;
		}
		
		float chemicalA;
		float chemicalB;
	}
	


}
