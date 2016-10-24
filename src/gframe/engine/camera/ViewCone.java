package gframe.engine.camera;

import gframe.engine.Matrix3D;
import gframe.engine.Point3D;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;

public class ViewCone {

	public ViewCone(int phi, int nearplaneDist, int farplaneDist) {
		super();

		this.phi = phi;
		this.tan_phi = (float) Math.tan(Toolbox.degreeToRadiant(phi));
		this.nearplaneDist = nearplaneDist;

		if (farplaneDist < nearplaneDist) {
			farplaneDist = nearplaneDist;
		}
		this.farplaneDist = farplaneDist;

		System.out.println("ViewCone init with tan_phi=" + tan_phi);
	}


	public boolean inside(Point3D p, Point3D viewOrigin, Matrix3D inverseViewMatrix) {
		Vector3D dist = new Vector3D(viewOrigin, p);
		inverseViewMatrix.transform(dist);

		if (dist.z < nearplaneDist || dist.z > farplaneDist) {
			return false;
		}

		float max = dist.z * tan_phi;
		if ((Math.abs(dist.x) > max) || (Math.abs(dist.y) > max)) {
			return false;
		}

		return true;
	}
	

	int phi;
	float tan_phi;
	int nearplaneDist;
	int farplaneDist;

}