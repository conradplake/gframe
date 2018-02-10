package gframe.engine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Marks faces as occluded if they fail the depth test against a specified
 * z-Buffer.
 * 
 */
public class DepthTestCuller {

	private ZBuffer zBuffer;
	private int width;
	private int height;
	private int xoffset;
	private int yoffset;

	Collection<RenderFace> markedOccluded;

	public boolean disabled;

	public DepthTestCuller(ZBuffer zBuffer) {
		this.markedOccluded = new ArrayList<RenderFace>();
		setZBuffer(zBuffer);
	}

	/**
	 * The sets a zBuffer against which to perform depth tests. We hold just a
	 * reference to the buffer, so changes made to it elsewhere will directly
	 * effect the test outcome.
	 */
	public void setZBuffer(ZBuffer zBuffer) {
		this.zBuffer = zBuffer;
		this.width = zBuffer.getWidth();
		this.height = zBuffer.getHeight();
		this.xoffset = zBuffer.getXoffset();
		this.yoffset = zBuffer.getYoffset();
	}

	/**
	 * Returns true if given face is considered occluded. If true, the face is
	 * added to an internal buffer (markedOccluded) which can be accessed later,
	 * e.g. during a final testing stage
	 */
	public boolean test(RenderFace renderFace) {
		// if(isOccluded(renderFace)){
		if (isOccludedFAST(renderFace)) {
			markedOccluded.add(renderFace);
			return true;
		}
		return false;
	}

	public boolean isOccluded(RenderFace renderFace) {

		if (zBuffer == null || disabled) {
			return false;
		}

		int bbox_minx = renderFace.getScreen_bbox_minx();
		int bbox_maxx = renderFace.getScreen_bbox_maxx();
		int bbox_miny = renderFace.getScreen_bbox_miny();
		int bbox_maxy = renderFace.getScreen_bbox_maxy();

		// determine BBox
		if (bbox_minx == -1) {
			bbox_minx = xoffset + (int) Math.floor(renderFace.minX() + 0.5f);
			if (bbox_minx < 0)
				bbox_minx = 0;
			if (bbox_minx >= width)
				bbox_minx = width - 1;

			bbox_maxx = xoffset + (int) Math.floor(renderFace.maxX() + 0.5f);
			if (bbox_maxx < 0)
				bbox_maxx = 0;
			if (bbox_maxx >= width)
				bbox_maxx = width - 1;

			bbox_miny = yoffset - (int) Math.floor(renderFace.minY() + 0.5f);
			if (bbox_miny < 0)
				bbox_miny = 0;
			if (bbox_miny >= height)
				bbox_miny = height - 1;

			bbox_maxy = yoffset - (int) Math.floor(renderFace.maxY() + 0.5f);
			if (bbox_maxy < 0)
				bbox_maxy = 0;
			if (bbox_maxy >= height)
				bbox_maxy = height - 1;

			renderFace.setScreen_bbox_minx(bbox_minx);
			renderFace.setScreen_bbox_maxx(bbox_maxx);
			renderFace.setScreen_bbox_miny(bbox_miny);
			renderFace.setScreen_bbox_maxy(bbox_maxy);
		}

		float bbox_minz = renderFace.minZ(); // take closest z-Value (we are
												// most conservative here)

		// perform depth test of frontside bbox against zBuffer
		for (int y = bbox_miny; y >= bbox_maxy; y--) {
			for (int x = bbox_minx; x <= bbox_maxx; x++) {			
				if (zBuffer.getValue(x, y) > bbox_minz) {
					return false;
				}
			}
		}
//		 System.out.println("bbox completely covered");

		// bbox is completely covered
		return true;
	}

	/**
	 * 
	 * Testet nur die eckpunkte der bounding box
	 */
	public boolean isOccludedFAST(RenderFace renderFace) {

		if (zBuffer == null || disabled) {
			return false;
		}

		int bbox_minx = renderFace.getScreen_bbox_minx();
		int bbox_maxx = renderFace.getScreen_bbox_maxx();
		int bbox_miny = renderFace.getScreen_bbox_miny();
		int bbox_maxy = renderFace.getScreen_bbox_maxy();

		// determine BBox
		if (bbox_minx == -1) {
			bbox_minx = xoffset + (int) Math.floor(renderFace.minX() + 0.5f);
			if (bbox_minx < 0)
				bbox_minx = 0;
			if (bbox_minx >= width)
				bbox_minx = width - 1;

			bbox_maxx = xoffset + (int) Math.floor(renderFace.maxX() + 0.5f);
			if (bbox_maxx < 0)
				bbox_maxx = 0;
			if (bbox_maxx >= width)
				bbox_maxx = width - 1;

			bbox_miny = yoffset - (int) Math.floor(renderFace.minY() + 0.5f);
			if (bbox_miny < 0)
				bbox_miny = 0;
			if (bbox_miny >= height)
				bbox_miny = height - 1;

			bbox_maxy = yoffset - (int) Math.floor(renderFace.maxY() + 0.5f);
			if (bbox_maxy < 0)
				bbox_maxy = 0;
			if (bbox_maxy >= height)
				bbox_maxy = height - 1;

			renderFace.setScreen_bbox_minx(bbox_minx);
			renderFace.setScreen_bbox_maxx(bbox_maxx);
			renderFace.setScreen_bbox_miny(bbox_miny);
			renderFace.setScreen_bbox_maxy(bbox_maxy);
		}

		float bbox_minz = renderFace.minZ(); // take closest z-Value (we are
												// most conservative here)

		// nur die vier ecken testen
		if (zBuffer.getValue(bbox_minx, bbox_miny) > bbox_minz) {
			return false;
		}
		if (zBuffer.getValue(bbox_maxx, bbox_miny) > bbox_minz) {
			return false;
		}
		if (zBuffer.getValue(bbox_minx, bbox_maxy) > bbox_minz) {
			return false;
		}
		if (zBuffer.getValue(bbox_maxx, bbox_maxy) > bbox_minz) {
			return false;
		}
		// plus center?
		// if(zBuffer.getValue(bbox_minx+(bbox_maxx-bbox_minx)>>1,
		// bbox_miny+(bbox_maxy-bbox_miny)>>1) > bbox_minz){
		// return false;
		// }

		// System.out.println("bbox completely covered");

		// bbox is completely covered
		return true;
	}

	public Collection<RenderFace> getOccluded() {
		return this.markedOccluded;
	}

	public void clearOccluded() {
		markedOccluded.clear();
	}

}
