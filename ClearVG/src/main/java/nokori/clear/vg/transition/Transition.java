package nokori.clear.vg.transition;

import nokori.clear.windows.util.Stopwatch;

/**
 * Transitions are objects that allow the smooth animation of Nodes via the use of timestamps.
 * 
 * By creating multipliers of duration from 0 to 1, it's possible to create a variety of animations that can improve the polish of a Node.
 *
 */
public abstract class Transition {
	
	private long durationInMillis;
	
	private boolean isPlaying = false;
	private Stopwatch stopwatch = new Stopwatch();
	
	private Object linkedObject = null;
	
	TransitionCompletedCallback completedCallback = null;
	
	public Transition(long durationInMillis) {
		this.durationInMillis = durationInMillis;
	}
	
	public long getDurationInMillis() {
		return durationInMillis;
	}

	public void setDurationInMillis(long durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	/**
	 * Will start this transition. If it's already playing, it will be reset.
	 */
	public Transition play() {
		if (isPlaying) {
			stop();
		}
		
		TransitionManager.removeLinkedTransitions(this, linkedObject);
		
		stopwatch.timeInMilliseconds(durationInMillis);
		TransitionManager.add(this);
		isPlaying = true;
		
		return this;
	}
	
	/**
	 * Stops this Transition from playing. This function will not call the onCompletedCallback unless this transition has finished.
	 * @return
	 */
	public Transition stop() {
		if (isFinished() && completedCallback != null) {
			completedCallback.callback(this);
		}
		
		TransitionManager.remove(this);
		isPlaying = false;
		return this;
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
		return stopwatch.getNormalizedDistanceBetweenTime();
	}

	public boolean isFinished() {
		return (isPlaying && stopwatch.isCurrentTimePassedEndTime());
	}

	/**
	 * @return the linked Object
	 * @see <code>setLinkedObject(object)</code>
	 */
	public Object getLinkedObject() {
		return linkedObject;
	}

	/**
	 * Setting a linked object will cause this Transition to be associated with that object. If you attempt to make another Transition of the same class
	 * and play it when it has the same linkedObject, then this Transition will be stopped and deleted automatically by the TransitionManager. 
	 * 
	 * The purpose of this system is to prevent two contradicting Transitions (e.g. fading in/fading out) from playing at the same time, 
	 * so that the user doesn't have to manually check for this.
	 * 
	 * @param linkedObject
	 */
	public void setLinkedObject(Object linkedObject) {
		this.linkedObject = linkedObject;
	}
}
