package gframe.engine.timing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class Timer implements Runnable{

	private static Timer instance;
		
	private boolean stopRequested = false;
	
	private Collection<Timed> timedObjects;
	
	private long updateIntervall = 20;
	
	private long lastTime;
	
	
	private Timer(){		
		timedObjects = new ArrayList<Timed>();
	}
	
	
	public static synchronized Timer getInstance(){
		if(instance==null){
			instance = new Timer();
			Thread t = new Thread(instance);
			t.start();
		}
		return instance;
	}
	
	
	public void stopTimer(){
		stopRequested = true;
	}
	
	public void setUpdateIntervall(long updateIntervall){
		this.updateIntervall = updateIntervall;
	}
	
	
	public void registerTimedObject(Timed timed){
		synchronized (timedObjects) {
			timedObjects.add(timed);	
		}		
	}
	
	@Override
	public void run() {

		lastTime = new Date().getTime();
		
		while(!stopRequested){
						
			long currentTime = System.currentTimeMillis();
			long timePassed = currentTime - lastTime;
			lastTime = currentTime;
			
			synchronized (timedObjects) {
				Iterator<Timed> it = timedObjects.iterator();
				while(it.hasNext()){
					Timed timed = it.next();
					timed.timePassedInMillis(timePassed);
					if(timed.done()){
						it.remove();
					}				
				}	
			}
			
			try {
				Thread.sleep(updateIntervall);
			} catch (InterruptedException wakeup) {				
			}			
		}
	}

}
