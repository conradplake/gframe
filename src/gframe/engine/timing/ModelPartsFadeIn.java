package gframe.engine.timing;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import gframe.engine.Engine3D;
import gframe.engine.Model3D;
import gframe.engine.generator.Model3DGenerator;

public class ModelPartsFadeIn implements Timed{
		
	private boolean done;
	private List<Model3D> parts;
	private float partsPerMillis;
	private float partCounter;				
	
	
	public ModelPartsFadeIn(Model3D model, long fadeInTimeInMillis, Engine3D engine) {		
		this.parts = new LinkedList<Model3D>(Model3DGenerator.splitToParts(model));
		for (Model3D part : parts) {
			part.getMatrix().transform(model.getMatrix());
			part.getOrigin().add(model.getOrigin());
			part.isVisible = false;
			engine.register(part);
		}
		partsPerMillis = parts.size() / (float)fadeInTimeInMillis;
		
		Collections.sort(parts, new Comparator<Model3D>(){
			@Override
			public int compare(Model3D o1, Model3D o2) {						
				if(o1.getBoundingSphereRadius() < o2.getBoundingSphereRadius()){
					return -1;
				}else{
					return 1;
				}					
			}});
		
//		System.out.println("#parts: "+parts.size()+", partsPerMillis: "+partsPerMillis);
	}
	
	
	@Override
	public void timePassedInMillis(long millis) {
		
		if(done)
			return;
		
		float partsToAdd = partsPerMillis * millis;
		partCounter += partsToAdd;
				
		if(partCounter>1){
			int numParts = (int)partCounter;
			for(int i=0;i<numParts && parts.size()>0;i++){
				Model3D part = parts.remove(0);
				part.isVisible = true;
			}				
			partCounter -= numParts;
		}
		
		done = parts.isEmpty();
					
	}

	@Override
	public boolean done() {		
		return this.done;
	}
	
}