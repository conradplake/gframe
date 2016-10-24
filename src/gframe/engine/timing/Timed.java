package gframe.engine.timing;

/**
 * interface für objekte, shader etc die eine zeitliche varianz besitzen
 * */
public interface Timed {

	/**
	 * Informs this object about the amount of time passed since last call to this method
	 * */
	public void timePassedInMillis(long millis);

	public boolean done();
	
}
