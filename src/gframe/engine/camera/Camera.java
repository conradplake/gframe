package gframe.engine.camera;

import gframe.engine.Face;
import gframe.engine.Object3D;
import gframe.engine.Point3D;

public class Camera extends Object3D {

	private ViewCone viewCone;
	
	
	public Camera() {
		super();
	}

	public Camera(Point3D origin) {
		super(origin);
	}

	public void setViewCone(ViewCone viewCone) {
		this.viewCone = viewCone;
	}
	
	public ViewCone getViewCone(){
		return viewCone;
	}
	

	// TODO
	// 3d clipping: clip face against the 'frustum' by clipping each line
	// against the frustum
	// returns the face as seen by the camera
	public Face clip(Face face) {
		return face;
	}

}