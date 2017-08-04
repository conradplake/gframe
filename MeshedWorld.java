package simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import gframe.Space3D;
import gframe.engine.Engine3D;
import gframe.engine.FlatShader;
import gframe.engine.Matrix3D;
import gframe.engine.Model3D;
import gframe.engine.MotionEvent;
import gframe.engine.MotionListener;
import gframe.engine.Point3D;
import gframe.engine.Toolbox;
import gframe.engine.Vector3D;
import gframe.engine.generator.Model3DGenerator;

public class MeshedWorld extends Space3D implements WorldInteractions, MotionListener {

	public MeshedWorld(Engine3D engine) {
		super();
		this.engine = engine;
		objects = new ArrayList<WorldObject>();
	}

	public MeshedWorld(Engine3D engine, int xdimTiles, int ydimTiles) {
		super();
		this.xdimTiles = xdimTiles;
		this.ydimTiles = ydimTiles;
		this.objects = new ArrayList<WorldObject>();
		this.mesh = Model3DGenerator.buildQuadBasedMesh(xdimTiles, ydimTiles, TILESIZE, Color.white);

		mesh.getVertices().get(xdimTiles*ydimTiles/2).y = TILESIZE/4f;	
//		for (Point3D vertex : mesh.getVertices()) {
//			vertex.y = (float) (Math.random() * TILESIZE / 5f);
//		}

		this.engine = engine;

		engine.register(mesh, new FlatShader(engine.getLightsource()));
		// engine.register(mesh);

		setGravity(Space3D.EARTH_G);
	}

	public void processMotionEvent(MotionEvent me) {
		WorldObjectMotionEvent wo_me = (WorldObjectMotionEvent) me;
		WorldObject wo = wo_me.getWorldObject();		
		Point3D newPos = wo_me.getNewPos();
		Tile newTile = getTile(newPos);

		if (newTile == null) {					
			System.out.println("Obj moved outside world at pos: "+wo.getOrigin());
			return; // moved outside world!
		}
//		System.out.println(newTile.rowIndex+", "+newTile.columnIndex);

		Point3D oldPos = wo_me.getOldPos();
		Matrix3D woMat = wo.getMatrix();

		
		// collision detection
		// TODO: only check objects inside neighboring tiles; register objects
		// to tiles
//		Iterator<WorldObject> it = objects.iterator();
//		while (it.hasNext()) {
//			WorldObject otherWo = it.next();
//			if (wo != otherWo) {
//				float radSum = wo.getBoundingSphereRadius() + otherWo.getBoundingSphereRadius();
//				float dist = newPos.distance(otherWo.getOrigin());
//				float overlap = dist - (radSum - radSum / 3);
//				if (overlap < 0) { // collision!
//					Vector3D pathVec = new Vector3D(newPos, oldPos);
//					pathVec.scale(-overlap * 2 / dist);
//					newPos.move(pathVec.x, pathVec.y, pathVec.z);
//
//					Vector3D rel_v = wo.getVelocityVector();
//					rel_v.scale(-0.5f);
//
//					// if (rel_v.length() < 0.1) {
//					// rel_v.x = 0;
//					// rel_v.y = 0;
//					// rel_v.z = 0;
//					// }
//
//				}
//			}
//		}

		
		
		// touchdown detection
		// find the 4 spanning vertices of the given tile and compute there
		// average y
	
		float height = 0;

		int index0 = newTile.rowIndex + newTile.columnIndex * xdimTiles;
		int index1 = newTile.rowIndex + 1 + newTile.columnIndex * xdimTiles;
		int index2 = newTile.rowIndex + (newTile.columnIndex + 1) * xdimTiles;
		int index3 = newTile.rowIndex + 1 + (newTile.columnIndex + 1) * xdimTiles;

		int numberOfVertices = mesh.numberOfVertices();
		int neighbors = 0;
		
		Vector3D tileBaseX = null;
		Vector3D tileBaseZ = null;

		if (index0 >= 0 && index0 < numberOfVertices) {
			height += mesh.getVertices().get(index0).y;
			neighbors++;
		}
		if (index1 >= 0 && index1 < numberOfVertices) {
			height += mesh.getVertices().get(index1).y;
			neighbors++;
			
			tileBaseX = new Vector3D(mesh.getVertices().get(index0), mesh.getVertices().get(index1)); 
		}
		if (index2 >= 0 && index2 < numberOfVertices) {
			height += mesh.getVertices().get(index2).y;
			neighbors++;
			
			tileBaseZ = new Vector3D(mesh.getVertices().get(index0), mesh.getVertices().get(index2));
		}
		if (index3 >= 0 && index3 < numberOfVertices) {
			height += mesh.getVertices().get(index3).y;
			neighbors++;
		}
		
		height = height / (float)neighbors;
		
		float alt = newPos.y - height;		
//		System.out.println("altitute = "+alt+"; wo.y="+wo.getOrigin().y);
		
		if (alt < 0) { // touchdown?
			newPos.move(0, -alt, 0);

			// reset and orthogonalize object matrix
			Matrix3D newMat = new Matrix3D();			
			
			if(tileBaseX!=null && tileBaseZ!=null){				
				Vector3D tileBaseY = tileBaseX.crossProduct(tileBaseZ);
				tileBaseX.normalize();
				tileBaseZ.normalize();
				tileBaseY.normalize();
				
				Matrix3D tileBase = new Matrix3D();
				tileBase.setXAxis(tileBaseX);
				tileBase.setYAxis(tileBaseY);
				tileBase.setZAxis(tileBaseZ);
								
//				System.out.println("Mat: "+tileBase.toString());
//				System.out.println("Ori: "+wo.getOrigin());
				
				newMat = tileBase.getTransformed(newMat);
			}
			
			newMat.transform(Toolbox.getYrotMatrix(wo.yDegree));
								
			woMat.apply(newMat.getArray());

			Vector3D v = wo.getVelocityVector();
//			System.out.println(v.x+" "+v.y+" "+v.z);
			if (v.y < 0) {
				v.y = 0;
			}
			if (v.x != 0) {
				v.x = 0;
			}
		}

		// world-range check

	}

	private Tile getTile(Point3D pos) {
		int r = (int) (pos.x / TILESIZE);
		int c = (int) (pos.z / TILESIZE);

		if (r >= 0 && r < xdimTiles && c >= 0 && c < ydimTiles) {
			return new Tile(r, c);
		} else {
			return null; // pos is outside world
		}
	}

	public Collection<WorldObject> getObjectsWithinRange(Point3D pos, float range) {
		List<WorldObject> list = new LinkedList<WorldObject>();
		Iterator<WorldObject> it = objects.iterator();
		while (it.hasNext()) {
			WorldObject wo = it.next();
			if (pos.distanceSquared(wo.getOrigin()) <= range * range) {
				list.add(wo);
			}
		}
		return list;
	}

	public float altitudeOf(WorldObject wo) {
		float alt = 0;
		Tile tile = getTile(wo.getOrigin());
		if (tile != null) {
			return altitudeOf(tile, wo);
		}
		return alt;
	}

	private float altitudeOf(Tile tile, WorldObject wo) {

		// find the 4 spanning vertices of the given tile and compute there
		// average y
		float height = 0;

		int index0 = tile.rowIndex + tile.columnIndex * xdimTiles;
		int index1 = tile.rowIndex + 1 + tile.columnIndex * xdimTiles;
		int index2 = tile.rowIndex + (tile.columnIndex + 1) * xdimTiles;
		int index3 = tile.rowIndex + 1 + (tile.columnIndex + 1) * xdimTiles;

		int numberOfVertices = mesh.numberOfVertices();
		int neighbors = 0;

		if (index0 >= 0 && index0 < numberOfVertices) {
			height += mesh.getVertices().get(index0).y;
			neighbors++;
		}
		if (index1 >= 0 && index1 < numberOfVertices) {
			height += mesh.getVertices().get(index1).y;
			neighbors++;
		}
		if (index2 >= 0 && index2 < numberOfVertices) {
			height += mesh.getVertices().get(index2).y;
			neighbors++;
		}
		if (index3 >= 0 && index3 < numberOfVertices) {
			height += mesh.getVertices().get(index3).y;
			neighbors++;
		}

		height = height / (float) neighbors;

		return wo.getOrigin().y - height;
	}

	public void put(WorldObject wo) {
		put(wo, true);
	}

	public void put(WorldObject wo, boolean attachWorldMotionListener) {
		if (attachWorldMotionListener) {
			wo.attachMotionListener(this);
		}
		engine.register(wo, 0);
		objects.add(wo);
	}

	public void remove(WorldObject wo) {
		wo.detachMotionListener(this);
		engine.deregister(wo);
		objects.remove(wo);
	}

	public void update() {
		if (timestamp == 0) {
			update(0);
			timestamp = System.currentTimeMillis();
		} else {
			long newtime = System.currentTimeMillis();
			update(newtime - timestamp);
			timestamp = newtime;
		}
	}

	public void update(float secPassed) {
		Vector3D grav = getGravityVector();
		Iterator<WorldObject> it = objects.iterator();
		while (it.hasNext()) {
			WorldObject wo = it.next();
			Point3D oldOri = wo.getOrigin().copy();
			
//			 if( altitudeOf(wo) <= 0 ){
//				 simulate(wo, secPassed); // simulate w/o grav to not fall through the ground ;)
//			 }else{
				 simulate(wo, grav, secPassed);
//			 }

			wo.update(secPassed);
			wo.callAttachedListeners(new WorldObjectMotionEvent(wo, oldOri));

		}
	}

	private long timestamp;

	private List<WorldObject> objects;

	public static int TILESIZE = ONE_METER;

	protected int xdimTiles;
	protected int ydimTiles;

	Model3D mesh;

	private Engine3D engine;

	public class Tile {
		public Tile(int r, int c) {
			this.rowIndex = r;
			this.columnIndex = c;
		}

		int rowIndex;
		int columnIndex;
	}

}