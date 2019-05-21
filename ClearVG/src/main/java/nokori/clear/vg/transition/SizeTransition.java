package nokori.clear.vg.transition;

/**
 * This transition is used to resize objects to various sizes.
 */
public abstract class SizeTransition extends Transition {
	private float targetWidth, targetHeight;
	
	public SizeTransition(long durationInMillis, float targetWidth, float targetHeight) {
		super(durationInMillis);
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
	}

	@Override
	public void tick(float progress) {
		setWidth(changeSize(progress, getCurrentWidth(), targetWidth));
		setHeight(changeSize(progress, getCurrentHeight(), targetHeight));
	}
	
	private float changeSize(float progress, float current, float target) {
		if (current > target) {
			float d = (current- target);
			return (target + (d * (1f - progress)));
		} else {
			float d = (target - current);
			return (current + (d * progress));
		}
	}
	
	/**
	 * Override this to return the current width of whatever element is being affected by this transition so that the iteration works correctly.
	 */
	protected abstract float getCurrentWidth();
	
	/**
	 * Override this to return the current height of whatever element is being affected by this transition so that the iteration works correctly.
	 */
	protected abstract float getCurrentHeight();
	
	/**
	 * This is the callback for the width size changes.
	 */
	protected abstract void setWidth(float width);
	
	/**
	 * This is the callback for the height size changes.
	 */
	protected abstract void setHeight(float height);
}
