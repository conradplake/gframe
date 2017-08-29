package gframe.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import gframe.engine.camera.Camera;
import imaging.ImageHelper;
import imaging.ImageRaster;

/**
 * 
 * This class ties together the main ingredients needed for 3D software
 * rendering:
 * 
 * - a list of 3d models / objects that exist - a polygon heap that contains all
 * triangles/quads/etc. to be rendered to the screen - a z-Buffer for pixel
 * accurate depth tests - a rasterizer that "writes" polygons onto a color
 * buffer - a lightsource for shading and shadowing - a camera through which the
 * scenery is observed
 * 
 * @author conrad plake
 */
public class Engine3D {

	public Engine3D(int screenx, int screeny) {
		this(1, screenx, screeny);
	}

	public Engine3D(int segments, int screenx, int screeny) {

		this.polyHeap = new PriorityQueue<RenderFace>();
		this.unsortedPolyHeap = new ArrayList<RenderFace>();
		this.rasterizer = new DefaultConvexPolygonRasterizer(screenx / 2, screeny / 2, screenx, screeny);
		this.zBuffer = rasterizer.createZBuffer();
		this.depthBuffer = rasterizer.createZBuffer();
		this.depthTestCuller = new DepthTestCuller(zBuffer);
		this.camera = new Camera();
		this.lightsource = DEFAULTLIGHTSOURCE;
		this.defaultShader = new PhongShader(lightsource);
		this.modelShaders = new HashMap<Model3D, Shader>();
		this.segments = new List[segments];
		this.initSegments();
	}

	public void setRasterizer(Rasterizer rasterizer) {
		this.rasterizer = rasterizer;
	}

	public void setActiveSegment(int seg) {
		if (seg >= 0 && seg < segments.length) {
			activeSegment = seg;
		}
	}

	public int getActiveSegment() {
		return activeSegment;
	}

	public Shader getDefaultShader() {
		return defaultShader;
	}

	public void setDefaultShader(Shader shader) {
		this.defaultShader = shader;
	}

	public void register(Model3D model, Shader shader) {
		this.register(model);
		this.setModelShader(model, shader);
	}

	public void register(Model3D model, Shader shader, boolean computeVertexNormals) {
		this.register(model, computeVertexNormals);
		this.setModelShader(model, shader);
	}

	public void register(Model3D model) {
		this.register(model, activeSegment, true);
	}

	public void register(Model3D model, boolean computeVertexNormals) {
		this.register(model, activeSegment, computeVertexNormals);
	}

	public void register(Model3D model, int seg) {
		this.register(model, seg, true);
	}

	public void register(Model3D model, int seg, boolean computeVertexNormals) {

		if (computeVertexNormals) {
			model.computeVertexNormals();
		}

		if (seg >= 0 && seg < segments.length) {
			segments[seg].add(model);
		} else {
			segments[activeSegment].add(model);
		}
	}

	public void setModelShader(Model3D model, Shader shader) {
		modelShaders.put(model, shader);
		for (Object object : model.getChildren()) {
			setModelShader((Model3D) object, shader);
		}
	}

	public void removeModelShader(Model3D model) {
		modelShaders.remove(model);
		for (Object object : model.getChildren()) {
			removeModelShader((Model3D) object);
		}
	}

	public void deregister(Model3D model) {
		if (model == null)
			return;

		for (int i = 0; i < segments.length; i++) {
			if (segments[i].remove(model)) {
				break;
			}
		}
		removeModelShader(model);
	}

	public void clear() {
		for (int i = 0; i < segments.length; i++) {
			segments[i].clear();
		}
		modelShaders.clear();
	}

	public void resetCamera() {
		camera.reset();
	}

	public void setCamera(Camera cam) {
		this.camera = cam;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setLightsource(Lightsource ls) {
		this.lightsource = ls;
		this.defaultShader.setLightsource(ls);
	}

	public void recomputeShadowMaps() {
		lightsource.recomputeDepthMap();
	}

	
	/**
	 * Returns a vector in world space coordinates that points from the screen at the given position
	 * towards the viewing direction of the camera. Returned vector is normalized to unit length.
	 */
	public Vector3D getWorldspaceRay(int screenPosX, int screenPosY) {
		// do all the reverse transformations from screen space to world space

		int xoffset = rasterizer.getXOffset();
		int yoffset = rasterizer.getYOffset();

		// ray to cam/eye space
		Vector3D ray = new Vector3D(screenPosX-xoffset, yoffset-screenPosY, 1);				

		// ray to world space
		// since we have a vector rather than a 3D-position, no invert of the perspective divide is necessary
		Matrix3D camToWorldMatrix = camera.getMatrix();
		camToWorldMatrix.transform(ray);

		ray.normalize();

		return ray;
	}
	

	public Lightsource getLightsource() {
		return lightsource;
	}

	public List<Model3D> getActiveModels() {
		return segments[activeSegment];
	}

	public void drawScenes(Graphics g) {
		ImageRaster frameBuffer = rasterizer.createEmptyImageRaster();
		int oldsegidx = activeSegment;
		for (int i = 0; i < segments.length; i++) {
			activeSegment = i;
			drawScene(frameBuffer);
		}
		activeSegment = oldsegidx;
		g.drawImage(frameBuffer.createImage(), 0, 20, frameBuffer.getWidth(), frameBuffer.getHeight(), null);
	}

	public void drawScene(Graphics g) {
		ImageRaster colorBuffer = rasterizer.createEmptyImageRaster();
		drawScene(colorBuffer);
		g.drawImage(colorBuffer.createImage(), 0, 20, colorBuffer.getWidth(), colorBuffer.getHeight(), null);
	}

	public void drawScenes(ImageRaster colorBuffer) {
		int oldsegidx = activeSegment;
		for (int i = 0; i < segments.length; i++) {
			activeSegment = i;
			drawScene(colorBuffer);
		}
		activeSegment = oldsegidx;
	}

	public void drawShadowedScene(ImageRaster colorBuffer) {

		if (lightsource.isShadowsEnabled() == false) {
			this.drawScene(colorBuffer);
			return;
		}

		// umwelt geändert? dann neuen snapshot der tiefenkarte erzeugen
		if (lightsource.isRecomputeDepthMap()) {

			Point3D lightOrigin = lightsource;
			Matrix3D iLightMat = lightsource.getInverseMatrix();
			Vector3D light_z = lightsource.getZVector();

			// we can cull with depthBuffer as well
			depthTestCuller.setZBuffer(depthBuffer);
			for (Model3D model : segments[activeSegment]) {
				if (model.isVisible)
					fillPolyBuffers(model, new Point3D(), new Matrix3D(), lightOrigin, light_z, iLightMat, false);
			}

			depthBuffer.clear();
			for (RenderFace renderFace : unsortedPolyHeap) {
				rasterizer.rasterize(renderFace, null, depthBuffer, null); // z-path
																			// only
			}
			unsortedPolyHeap.clear();

			for (RenderFace renderFace : depthTestCuller.getOccluded()) {
				if (!depthTestCuller.isOccluded(renderFace)) { // re-check for
																// occlusion
																// with updated
																// zBuffer
																// (otherwise
																// shadow
																// flickering
																// will occur
																// during
																// animations)
					rasterizer.rasterize(renderFace, null, depthBuffer, null);
				}
			}

			if (filterDepthBuffer) {
				depthBuffer.filter(ImageHelper.TPFILTER33); // 3x3 box filter
			}

			lightsource.setDepthMap(depthBuffer);

			// put back the original zBuffer
			depthTestCuller.setZBuffer(zBuffer);
			depthTestCuller.clearOccluded();
		}

		// depthBuffer ist gefüllt, jetzt ganz normal drawScene ausführen:
		Point3D camOrigin = camera.getOrigin();
		Matrix3D icammat = camera.getMatrix().getInverse();
		Vector3D cam_z = camera.getZVector();

		for (Model3D model : segments[activeSegment]) {
			if (model.isVisible) {
				model.preDraw();
				fillPolyBuffers(model, new Point3D(), new Matrix3D(), camOrigin, cam_z, icammat, true);
			}
		}

		zBuffer.clear();
		while (!polyHeap.isEmpty()) {
			RenderFace renderFace = polyHeap.remove();
			Shader shader = shadingEnabled ? defaultShader : null;
			if (shadingEnabled && renderFace.shader != null) {
				shader = renderFace.shader; // override with model specific
											// shader
			}

			if (shader != null) {
				shader.preShade(renderFace);
			}

			rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader);
		}

		// re-check with updated zBuffer all polys previously marked as
		// occluded (this prevents visual glitches when camera moves fast)
		for (RenderFace renderFace : depthTestCuller.getOccluded()) {
			// re-check for occlusion with updated zBuffer
			if (!depthTestCuller.isOccluded(renderFace)) {
				Shader shader = shadingEnabled ? defaultShader : null;
				if (shadingEnabled && renderFace.shader != null) {
					shader = renderFace.shader; // override with model specific
												// shader
				}

				if (shader != null) {
					shader.preShade(renderFace);
				}

				rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader);
			}
		}
		depthTestCuller.clearOccluded();

	}

	public void drawScene(ImageRaster colorBuffer) {

		Point3D camOrigin = camera.getOrigin().copy();
		Matrix3D icammat = camera.getMatrix().getInverse();
		Vector3D cam_z = camera.getZVector().copy();

		for (Model3D model : segments[activeSegment]) {

			if (model.isVisible) {
				model.preDraw();
				fillPolyBuffers(model, new Point3D(), new Matrix3D(), camOrigin, cam_z, icammat, true);
			}
		}

		// now clear zBuffer AFTER everything is put into scene so the
		// information can be used during this stage for occlusion culling
		zBuffer.clear();

		while (!polyHeap.isEmpty()) {
			RenderFace renderFace = polyHeap.remove();
			Shader shader = shadingEnabled ? defaultShader : null;
			if (shadingEnabled && renderFace.shader != null) {
				shader = renderFace.shader; // override with model specific
											// shader
			}

			if (shader != null) {
				shader.preShade(renderFace);
			}
			rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader);
		}

		// re-check all polys previously marked as occluded (this prevents
		// visual glitches when camera moves fast)
		for (RenderFace renderFace : depthTestCuller.getOccluded()) {
			// re-check for occlusion with updated zBuffer (here is the trick:
			// since zBuffer is filled with current scene, less pixels need to
			// be shaded if any
			if (!depthTestCuller.isOccluded(renderFace)) {
				Shader shader = shadingEnabled ? defaultShader : null;
				if (shadingEnabled && renderFace.shader != null) {
					shader = renderFace.shader; // override with model specific
												// shader
				}

				if (shader != null) {
					shader.preShade(renderFace);
				}

				rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader);
			}
		}
		depthTestCuller.clearOccluded();

	}

	/**
	 * 
	 * Here is where all the transformation takes place. Results in polygons of
	 * specified model added to a poly heap. This method is invoked recursively
	 * for all sub models.
	 * 
	 * * /
	 * 
	 * @param model
	 * @param parentOrigin
	 * @param parentMatrix
	 * @param camOrigin
	 * @param cam_z
	 * @param icammat
	 * @param zSortPolyHeap
	 */
	protected void fillPolyBuffers(Model3D model, Point3D parentOrigin, Matrix3D parentMatrix, Point3D camOrigin,
			Vector3D cam_z, Matrix3D icammat, boolean zSortPolyHeap) {
		Point3D modelOrigin = parentMatrix.transform(model.getOrigin().copy());
		modelOrigin.add(parentOrigin);
		Matrix3D modelMatrix = parentMatrix.getTransformed(model.getMatrix());
		Shader modelShader = modelShaders.get(model);

		Point3D camPosInObjectSpace = null;
		Vector3D camZInObjectSapce = null;
		Matrix3D modelInverse = modelMatrix.getInverse();
		camPosInObjectSpace = modelInverse.transform(camOrigin.copy().subtract(modelOrigin));
		camZInObjectSapce = modelInverse.transform(cam_z.copy());

		for (Iterator<Face> it = model.getFaces().iterator(); it.hasNext();) {

			Face face = (Face) it.next();

			// 3d clipping should take place in object space!
			// if new face is temporarily created due to clipping then nothing
			// needs to change along the pipeline (createRenderFace etc)
			face = Clipper3D.clip(face, camPosInObjectSpace, camZInObjectSapce);
			if (face == null) {
				continue; // face is out of view
			}

			RenderFace renderFace = face.createRenderFace(model.getMaterial());

			// put in world space
			renderFace.transform(modelMatrix, modelOrigin);

			// put in camera space
			renderFace.transformToCamSpace(camOrigin, icammat, true);

			if (!rasterizer.isOutsideScreen(renderFace)) {

				renderFace.setShader(modelShader);

				if (!depthTestCuller.test(renderFace)) {
					if (zSortPolyHeap) {
						polyHeap.add(renderFace);
					} else {
						unsortedPolyHeap.add(renderFace);
					}
				}
			}
		}

		for (Iterator it = model.getChildren().iterator(); it.hasNext();) {
			Model3D subModel = (Model3D) it.next();
			if (subModel.isVisible)
				fillPolyBuffers(subModel, modelOrigin, modelMatrix, camOrigin, cam_z, icammat, zSortPolyHeap);
		}
	}

	/**
	 * A perspective division factor to apply for perspective correction.
	 * 
	 * Returns a factor depending on the z-value so that bigger z-values make
	 * Objects appear smaller --> factor decreases
	 * 
	 * Works best with a scaling value set to screen width.
	 */
	public static final float zFactor(final float z) {
		if (z <= 1) {
			return 800f;
		}
		return 800f / z;
	}

	/**
	 * invZf = 1/zf
	 * 
	 * Returns the original z-value
	 */
	public static final float inverseZFactor(final float zf, final float invZf) {
		if (zf == 800) {
			return 1f;
		} else {
			return 800f * invZf; // invZf = 1/zf;
		}
	}

	private void initSegments() {
		for (int i = 0; i < segments.length; i++) {
			segments[i] = new LinkedList<Model3D>();
			// segments[i] = Collections.synchronizedList(new
			// ArrayList<Model3D>());
		}
	}

	private final Lightsource DEFAULTLIGHTSOURCE = new Lightsource(0, 0, 0, Color.white, Lightsource.NORM_INTENSITY);

	protected int activeSegment = 0;

	protected List<Model3D>[] segments;
	protected Camera camera;

	protected Lightsource lightsource;

	// protected List<RenderFace> polyHeap;
	protected PriorityQueue<RenderFace> polyHeap;
	protected Collection<RenderFace> unsortedPolyHeap;

	Rasterizer rasterizer;

	ZBuffer zBuffer;
	ZBuffer depthBuffer;

	// takes advantage of frame coherence for occlusion culling
	public DepthTestCuller depthTestCuller;

	public boolean filterDepthBuffer = false;
	public boolean shadingEnabled = true;

	protected Shader defaultShader;

	Map<Model3D, Shader> modelShaders;
}
