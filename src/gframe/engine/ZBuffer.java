package gframe.engine;

public class ZBuffer {

	int w;
	int h;
	int xoffset;
	int yoffset;
	float[] pixels;


	public ZBuffer(int w, int h) {
		this.w = w;
		this.h = h;
		this.xoffset = w/2;
		this.yoffset = h/2;
		this.pixels = new float[w*h];
	}
		

	public void clear(){
		for (int i = 0; i < pixels.length; i++) {				
			pixels[i] = Integer.MAX_VALUE;		
		}
	}
		

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public float getValue(int x, int y) {
		return pixels[y * w + x];
	}

	public void setValue(int x, int y, float value) {
		pixels[y * w + x] = value;
	}

	
	public float getValue(float x, float y, boolean doBilinearFiltering){
		
		if(!doBilinearFiltering){
			int light_x = (int) Math.floor(x + 0.5f);
			int light_y = (int) Math.floor(y + 0.5f);
			return this.pixels[light_y*w + light_x];
		}
		else{
			int x_int = (int)x;
			int y_int = (int)y;
			float x_fract = x - x_int;
			float y_fract = y - y_int;
				
			float c0 = pixels[y_int*w + x_int];			
			float c1 = x_int+1<w? pixels[y_int*w + x_int+1] : c0;				
			float c2 = y_int+1<h? pixels[(y_int+1)*w + x_int] : c0;				
			float c3 = x_int+1<w && y_int+1<h? pixels[(y_int+1)*w + (x_int+1)] : c0;		
														
			float c0_weight = (1-x_fract) * (1-y_fract);
			float c1_weight = x_fract * (1-y_fract);
			float c2_weight = (1-x_fract) * y_fract;
			float c3_weight = x_fract * y_fract;
				
			float result = ( c0 * c0_weight
						   + c1 * c1_weight
						   + c2 * c2_weight
						   + c3 * c3_weight );
									

			return result;	
		}
				
	}
	
	
	/**
	 * Runs the given matrix over the grid of pixels.
	 *
	 *
	 * Possible filter matrices: see e.g. imaging.ImageHelper.TPFILTER33
	 * */
	public void filter(float[][] filterMtx) {
		
		// TODO: split into hblur + vblur!
		
		int fmLX = filterMtx.length;
		int fmLX2 = fmLX / 2;

		int fmLY = filterMtx[0].length;
		int fmLY2 = fmLY / 2;

		float[] filtered = new float[w*h];

		for (int x = fmLX2; x < w - fmLX2; x++) {
			for (int y = fmLY2; y < h - fmLY2; y++) {
				for (int fx = 0; fx < fmLX; fx++) {
					for (int fy = 0; fy < fmLY; fy++) {
						filtered[y*w + x] += pixels[(y+fmLY2-fy)*w + x + fmLX2 - fx] * filterMtx[fx][fy];
					}
				}
			}
		}

		pixels = filtered;
	}

	
	public int getXoffset() {
		return xoffset;
	}

	public int getYoffset() {
		return yoffset;
	}


	/**
	 * Tests if the specified value draw_z at position draw_x,draw_y is less than the current value at this position.
	 * If so, the current value is overwritten and boolean true is returned.
	 * Otherwise, boolean false is returned. 
	 * */
	public boolean update(int draw_x, int draw_y, float draw_z) {			
		int index = draw_y * w + draw_x;				
		if (draw_z < pixels[index]) {
			pixels[index] = draw_z;
			return true;
		}
		return false;
	}


}
