package gframe.engine;

/**
 * A 32-bit pixel buffer.
 * Call createImage for rendering.
 * 
 * @author plake
 */
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

public class ImageRaster {

	private int w;
	private int h;
	protected int[] pixels;

	private ImageProducer producer;

	public ImageRaster(int w, int h) {
		this.w = w;
		this.h = h;
		this.pixels = new int[w*h];
		producer = new MemoryImageSource(w, h, pixels, 0, w);
	}
	
	public ImageRaster(int w, int h, int[] pixels) {
		this.w = w;
		this.h = h;
		this.pixels = pixels;
		producer = new MemoryImageSource(w, h, pixels, 0, w);
	}

	public void clear(){
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 0;
		}
	}
	
	/**
	 * Clears this buffer along with the given zBuffer.
	 * Both buffer must be of equal size.
	 * */
	public void clear(ZBuffer zBuffer){
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 0;
			zBuffer.pixels[i] = Integer.MAX_VALUE;;
		}
	}
	
	public Image createImage() {
		return Toolkit.getDefaultToolkit().createImage(producer);
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public int getPixel(int x, int y) {
		return pixels[y * w + x];
	}

	public void setPixel(int x, int y, int rgb) {
		pixels[y * w + x] = rgb;
	}

	public void setPixel(int x, int y, int rgb, int alpha) {
		pixels[y * w + x] = (alpha << 24) | rgb;
	}

	public void setAlpha(int x, int y, int alpha) {
		int rgb = pixels[y * w + x] & 0xffffff;
		pixels[y * w + x] = (alpha << 24) | rgb;
	}

	public ImageProducer getProducer() {
		return producer;
	}

	public void inverse() {
		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];

			int alpha = (pixel >> 24) & 0xff;
			int red = (pixel >> 16) & 0xff;
			int green = (pixel >> 8) & 0xff;
			int blue = (pixel) & 0xff;

			int ired = 0xff - red;
			int igreen = 0xff - green;
			int iblue = 0xff - blue;

			pixels[i] = (alpha << 24) | (ired << 16) | (igreen << 8) | iblue;
		}
	}

	public ImageRaster copy() {
		int[] pxls = new int[w * h];
		for (int i = 0; i < pixels.length; i++) {
			pxls[i] = pixels[i];
		}
		return new ImageRaster(w, h, pxls);
	}

}
