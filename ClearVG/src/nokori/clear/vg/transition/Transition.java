package nokori.clear.vg.transition;

/**
 * Transitions are objects that allow the smooth animation of Nodes via the use of timestamps.
 * 
 * By creating multipliers of duration from 0 to 1, it's possible to create a variety of animations that can improve the polish of a Node.
 *
 */
public abstract class Transition {
	
	private long durationInMillis;
	
	private boolean isPlaying = false;
	private long startStamp;
	private long endStamp;
	
	TransitionCompletedCallback completedCallback = null;
	
	public Transition(long durationInMillis) {
		this.durationInMillis = durationInMillis;
	}
	
	/**
	 * Will start this transition. If it's already playing, it will be reset.
	 */
	public void play() {
		if (isPlaying) {
			stop();
		}
		
		startStamp = System.currentTimeMillis();
		endStamp = startStamp + durationInMillis;
		TransitionManager.add(this);
		isPlaying = true;
	}
	
	public void stop() {
		TransitionManager.remove(this);
		isPlaying = false;
	}
	
	/**
	 * Called by the TransitionManager regularly.
	 * 
	 * @param progress - the progress of the Transition to completion (between 0-1, where 1 is 100% complete)
	 */
	public abstract void tick(float progress);
	
	public void setOnCompleted(TransitionCompletedCallback completedCallback) {
		this.completedCallback = completedCallback;
	}
	
	public interface TransitionCompletedCallback {
		public void callback(Transition t);
	};
	
	/**
	 * @return a value from 0 to 1 based on the transition time.
	 */
	public float getProgress() {
		long currentTime = System.currentTimeMillis();
		
		double maxDistance = endStamp - startStamp;
		double distance = Math.max(endStamp - currentTime, 0);
		
		float progress = 1f - (float) (distance / maxDistance);
		
		//System.err.println(maxDistance + " " + distance + " -> " + progress);
		
		return progress;
	}
	
	public boolean isFinished() {
		return (isPlaying && System.currentTimeMillis() > endStamp);
	}
}
