package nokori.clear.vg.transition;

/**
 * This Transition can be used to track a value going from the start value to the end value via the normalized progress variable. 
 * It's useful for creating basic transitions.
 *
 */
public class TransitionImpl extends Transition {

	private float start, end;
	private float currentValue;
	
	private ProgressCallback callback = null;
	private CurveProcessor curveProcessor = null;
	
	public TransitionImpl(long durationInMillis, float start, float end, ProgressCallback callback, CurveProcessor curveProcessor) {
		this(durationInMillis, start, end, callback);
		this.curveProcessor = curveProcessor;
	}
	
	public TransitionImpl(long durationInMillis, float start, float end, ProgressCallback callback) {
		this(durationInMillis, start, end);
		this.callback = callback;
	}
	
	public TransitionImpl(long durationInMillis, float start, float end) {
		super(durationInMillis);
		this.start = start;
		this.end = end;
		currentValue = start;
	}

	public void setStartAndEnd(float start, float end) {
		this.start = start;
		this.end = end;
	}
	
	public float getStart() {
		return start;
	}

	public void setStart(float start) {
		this.start = start;
	}

	public float getEnd() {
		return end;
	}

	public void setEnd(float end) {
		this.end = end;
	}

	@Override
	public void tick(float progress) {
		currentValue = start + ((end - start) * progress);
		
		if (curveProcessor != null) {
			currentValue = curveProcessor.modifyValue(currentValue);
		}

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
	
	/**
	 * Sets a processor for the value produced by this Transition, allowing you to tweak its curve
	 * @param curveProcessor
	 */
	public void setCurveProcessor(CurveProcessor curveProcessor) {
		this.curveProcessor = curveProcessor;
	}
	
	public float getCurrent() {
		return currentValue;
	}
	
	public interface ProgressCallback {
		public void callback(float value);
	}
	
	public interface CurveProcessor {
		public float modifyValue(float value);
	};
}
