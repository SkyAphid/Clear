package nokori.clear.vg.transition;

import nokori.clear.vg.widget.assembly.Widget;

/**
 * This transition is an extended SizeTransition geared toward resizing Widgets automatically.
 */
public class WidgetSizeTransition extends SizeTransition {
	
	private Widget widget;
	
	public WidgetSizeTransition(Widget widget, long durationInMillis, float targetWidth, float targetHeight) {
		super(durationInMillis, targetWidth, targetHeight);
		this.widget = widget;
	}

	protected float getCurrentWidth() {
		return widget.getWidth();
	}
	
	protected float getCurrentHeight() {
		return widget.getHeight();
	}
	
	protected void setWidth(float width) {
		widget.setWidth(width);
	}
	
	protected void setHeight(float height) {
		widget.setHeight(height);
	}
}
