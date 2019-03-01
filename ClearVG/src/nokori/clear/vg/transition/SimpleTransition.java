package nokori.clear.vg.transition;

/**
 * This Transition can be used to track a value going from the start value to the end value via the normalized progress variable.
 *
 */
public class SimpleTransition extends Transition {

	private float start, end;
	private float currentValue;
	
	private ProgressCallback callback = null;
	
	public SimpleTransition(long durationInMillis, float start, float end) {
		super(durationInMillis);
		this.start = start;
		this.end = end;
		currentValue = start;
	}

	@Override
	public void tick(float progress) {
		currentValue = start + ((end - start) * progress);

		if (callback != null) {
			callback.callback(currentValue);
		}
	}
	
	/**
	 * Sets a callback that is called as the transitioning value changes. The value passed through the callback is the current value during its transition.
	 */
	public void setProgressCallback(ProgressCallback callback) {
		this.callback = callback;
	}
	
	public float getCurrent() {
		return currentValue;
	}
	
	public interface ProgressCallback {
		public void callback(float value);
	}
}
