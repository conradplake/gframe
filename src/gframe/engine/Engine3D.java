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
 * This class ties together the main ingredients needed for 3D software rendering:
 *  
 * - a list of 3d models / objects that exist
 * - a polygon heap that contains all triangles/quads/etc. to be rendered to the screen
 * - a z-Buffer for pixel accurate depth tests
 * - a rasterizer that "writes" polygons onto a color buffer
 * - a lightsource for shading and shadowing
 * - a camera through which the scenery is observed 
 *  
 *  @author conrad plake
 * */
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
	
	
	public void setRasterizer(Rasterizer rasterizer){
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
	
	public void register(Model3D model) {
		this.register(model, activeSegment);	
	}

	public void register(Model3D model, int seg) {
		model.computeVertexNormals();
		if (seg >= 0 && seg < segments.length) {
			segments[seg].add(model);
		} else {
			segments[activeSegment].add(model);
		}
	}	
	
	
	public void setModelShader(Model3D model, Shader shader){		
		modelShaders.put(model, shader);
		for (Object object : model.getChildren()) {
			setModelShader((Model3D)object, shader);
		}
	}
	
	public void removeModelShader(Model3D model){
		modelShaders.remove(model);
		for (Object object : model.getChildren()) {
			removeModelShader((Model3D)object);
		}
	}
	
	public void deregister(Model3D model) {
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
		
	
	public void setDirectionalLight(DirectionalLight directionalLight){
		this.directionalLight = directionalLight;
		this.defaultShader.setDirectionalLight(directionalLight);
		
		for(Shader shader : modelShaders.values()){
			shader.setDirectionalLight(null); // will be overwritten during next draw call with the new directional light
		}
	}
	
	public DirectionalLight getDirectionalLight(){
		return directionalLight;			
	}
	
	
	public void recomputeShadowMaps(){
		if(directionalLight!=null)
			directionalLight.recomputeDepthMap();
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
	
	public void drawShadowedScene(ImageRaster colorBuffer){			
		
		if(directionalLight==null){
			drawScene(colorBuffer);
			return;
		}		
		
		
		if(directionalLight.isRecomputeDepthMap()){
			
			Point3D lightOrigin = directionalLight.getOrigin();
			Matrix3D iLightMat = directionalLight.getInverseMatrix();
			Vector3D light_z = directionalLight.getZVector();
								
			// we can cull with depthBuffer as well
			depthTestCuller.setZBuffer(depthBuffer);
			for(Model3D model : segments[activeSegment]){
				if(model.isVisible)
				  fillPolyBuffers(model, new Point3D(), new Matrix3D(), lightOrigin, light_z, iLightMat, false);
			}
			
			depthBuffer.clear();	
			for(RenderFace renderFace : unsortedPolyHeap){
				rasterizer.rasterize(renderFace, null, depthBuffer, null); // z-path only
			}
			unsortedPolyHeap.clear();
			
			for(RenderFace renderFace : depthTestCuller.getOccluded()){							
				if (!depthTestCuller.isOccluded(renderFace)) { // re-check for occlusion with updated zBuffer (otherwise shadow flickering will occur during animations)
					rasterizer.rasterize(renderFace, null, depthBuffer, null);
				}
			}

			
			if(filterDepthBuffer){
			  depthBuffer.filter(ImageHelper.TPFILTER33); // 3x3 box filter
			}
			
			directionalLight.setDepthMap(depthBuffer);
			
			// put back the original zBuffer 
			depthTestCuller.setZBuffer(zBuffer);
			depthTestCuller.clearOccluded();
		}		
		
		
		// depthBuffer ist gefüllt, jetzt ganz normal drawScene ausführen:
		Point3D camOrigin = camera.getOrigin();
		Matrix3D icammat = camera.getMatrix().getInverse();
		Vector3D cam_z = camera.getZVector();							
		
		for (Model3D model : segments[activeSegment]) {
			if(model.isVisible){
				model.preDraw();
				fillPolyBuffers(model, new Point3D(), new Matrix3D(), camOrigin, cam_z, icammat, true);
			}
		}
						
		zBuffer.clear();
		while(!polyHeap.isEmpty()){
			RenderFace renderFace = polyHeap.remove();
			Shader shader = shadingEnabled ? defaultShader : null;
			if(shadingEnabled && renderFace.shader!=null){
				shader = renderFace.shader; // override with model specific shader
			}			
			
			if(shader!=null){
				shader.setDirectionalLight(directionalLight);
				shader.preShade(renderFace);
			}
			
			rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader);
		}
		
		// re-check with updated zBuffer all polys previously marked as
		// occluded (this prevents visual glitches when camera moves fast)		
		for(RenderFace renderFace : depthTestCuller.getOccluded()){			
			// re-check for occlusion with updated zBuffer
			if (!depthTestCuller.isOccluded(renderFace)) {
				Shader shader = shadingEnabled ? defaultShader : null;
				if (shadingEnabled && renderFace.shader != null) {
					shader = renderFace.shader; // override with model specific
													// shader
				}
				
				if(shader!=null){
					shader.setDirectionalLight(directionalLight);
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
		Vector3D cam_z = camera.getZVector();
							
		for (Model3D model : segments[activeSegment]) {
			
			if(model.isVisible){			
				model.preDraw();
				fillPolyBuffers(model, new Point3D(), new Matrix3D(), camOrigin, cam_z, icammat, true);
			}
		}
				
		// clear zBuffer AFTER everything is put into scene so the information can be used during this stage for doing OC
		zBuffer.clear();			
			
		while(!polyHeap.isEmpty()){
			RenderFace renderFace = polyHeap.remove();
			Shader shader = shadingEnabled ? defaultShader : null;
			if(shadingEnabled && renderFace.shader!=null){
				shader = renderFace.shader; // override with model specific shader
			}			
			
			if(shader!=null){
				shader.setDirectionalLight(null); // Suppress execution of shadow mapping code during rasterization
				shader.preShade(renderFace);
			}
			
			rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader); 
		}
		
		// re-check all polys previously marked as occluded (this prevents visual glitches when camera moves fast)		
		for(RenderFace renderFace : depthTestCuller.getOccluded()){			
			// re-check for occlusion with updated zBuffer
			if(!depthTestCuller.isOccluded(renderFace)){
				Shader shader = shadingEnabled ? defaultShader : null;
				if(shadingEnabled && renderFace.shader!=null){
					shader = renderFace.shader; // override with model specific shader
				}	
				
				if(shader!=null){
					shader.setDirectionalLight(null); // Suppress execution of shadow mapping code during rasterization
					shader.preShade(renderFace);
				}
				
				rasterizer.rasterize(renderFace, colorBuffer, zBuffer, shader);
			}
		}
		depthTestCuller.clearOccluded();
			
	}
	
	
	protected void fillPolyBuffers(Model3D model, Point3D parentOrigin, Matrix3D parentMatrix, Point3D camOrigin,
			Vector3D cam_z, Matrix3D icammat, boolean zSortPolyHeap) {
		Point3D modelOrigin = parentMatrix.transform(model.getOrigin().copy());
		modelOrigin.add(parentOrigin);
		Matrix3D modelMatrix = parentMatrix.getTransformed(model.getMatrix());		
		Shader modelShader = modelShaders.get(model);
		
		for (Iterator<Face> it = model.getFaces().iterator(); it.hasNext();) {
			
			Face face = (Face)it.next();
			RenderFace renderFace = face.createRenderFace();						
							
			renderFace.preTransform(modelMatrix, modelOrigin);						
			if(backfaceCull(renderFace, camOrigin, icammat)){
				continue;
			}
			renderFace.postTransform(modelMatrix, modelOrigin);
			
			
//			renderFace = rasterizer.nearPlaneClipping(renderFace);
			
			renderFace.transformToCamSpace(camOrigin, icammat);
			
			
			if(!rasterizer.isOutsideScreen(renderFace)){
				
				renderFace.setShader(modelShader);
				
				if(!depthTestCuller.test(renderFace)){
					if(zSortPolyHeap){
					  polyHeap.add(renderFace);
					}
					else{
					  unsortedPolyHeap.add(renderFace);
					}
				}
			}			
		}
		
		
		for (Iterator it = model.getChildren().iterator(); it.hasNext();) {
			Model3D subModel = (Model3D) it.next();
			if(subModel.isVisible)				
				fillPolyBuffers(subModel, modelOrigin, modelMatrix, camOrigin, cam_z, icammat, zSortPolyHeap);
		}
	}
	
	
	public boolean backfaceCull(Face face, Point3D camOrigin, Matrix3D iCamMat){		

		// vector pointing from camera (view point) to center of face - hopefully its faster than initiating a vector3d object ;) 
		float vCamToFace_x = face.centroid.x - camOrigin.x;
		float vCamToFace_y = face.centroid.y - camOrigin.y;
		float vCamToFace_z = face.centroid.z - camOrigin.z;

		// compute dot product and compare sign
		boolean isBackfaced = (vCamToFace_x*face.normal_x + vCamToFace_y*face.normal_y + vCamToFace_z*face.normal_z) > 0;
		
		// TODO: here we do a second test! this should be reflected in the method's name!		
		// face lies in front of camera?
		boolean isInFront = iCamMat.transformZ(vCamToFace_x, vCamToFace_y, vCamToFace_z) > 0;		
		
		return isBackfaced || !isInFront;		
	}
	

	/*
	 * returns a factor depending on the z-value so that bigger z-values make
	 * Objects appear smaller --> factor decreases
	 */
	public static float zFactor(float z) {		//								
						
		if(z<=1){		
			return 800;
		} 		
		return 800 / z;
	}
		

	private void initSegments() {
		for (int i = 0; i < segments.length; i++) {
			segments[i] = new LinkedList<Model3D>();
//			segments[i] = Collections.synchronizedList(new ArrayList<Model3D>());
		}
	}

	private final Lightsource DEFAULTLIGHTSOURCE = new Lightsource(0, 0, 0, Color.white, Lightsource.NORM_INTENSITY);

	protected int activeSegment = 0;

	protected List<Model3D>[] segments;
	protected Camera camera;
	
	protected DirectionalLight directionalLight;
	protected Lightsource lightsource;
	
	//protected List<RenderFace> polyHeap;
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
