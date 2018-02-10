package gframe.app;

import java.awt.Graphics;
import java.awt.Image;

/**
 * Adds double buffering to an awt.Frame.
 * 
 */
public class DoubleBufferedFrame extends java.awt.Frame {

	public DoubleBufferedFrame() {
		super();
	}

	public DoubleBufferedFrame(String title) {
		super(title);
	}

	public void update(Graphics g) {
		if (offscreen == null) {
			offscreen = createImage(this.getSize().width, this.getSize().height);
		}
		Graphics dbGraphics = offscreen.getGraphics();
		dbGraphics.setColor(getBackground());
		dbGraphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
		dbGraphics.setColor(getForeground());
		paint(dbGraphics);
		g.drawImage(offscreen, 0, 0, this);
	}
	
	
	public void clearDoubleBuffer(){
		offscreen = null; 	
	}
	
	public Image getOffscreen(){
		return offscreen;
	}

	private Image offscreen;
}